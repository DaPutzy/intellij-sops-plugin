package com.github.daputzy.intellijsopsplugin.handler;

import com.github.daputzy.intellijsopsplugin.file.FileUtil;
import com.github.daputzy.intellijsopsplugin.sops.ExecutionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class EditActionHandler {

	@NotNull
	private final Project project;

	@NotNull
	private final VirtualFile file;

	public void handle() {
		final String originalContent = FileUtil.getInstance().getContent(file);

		ExecutionUtil.getInstance().decrypt(project, file, decryptedContent -> {
			final VirtualFile inMemoryFile = new LightVirtualFile(
				file.getName(),
				FileUtil.getInstance().getFileType(file),
				decryptedContent
			);

			ApplicationManager.getApplication()
				.invokeLater(() -> FileEditorManager.getInstance(project).openFile(inMemoryFile, true));

			final MessageBusConnection connection = project.getMessageBus().connect();
			connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
				@Override
				public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile closedFile) {
					if (inMemoryFile.equals(closedFile)) {
						// check if it is our file first, other files may not have a document
						final String closedFileContent = FileUtil.getInstance().getDocument(closedFile).getText();

						if (!closedFileContent.equals(decryptedContent)) {
							FileUtil.getInstance().writeContentBlocking(file, closedFileContent);

							ExecutionUtil.getInstance().encrypt(
								project,
								file,
								// success
								() -> file.refresh(true, false),
								// failure
								() -> FileUtil.getInstance().writeContentBlocking(file, originalContent)
							);

							connection.disconnect();
						}
					}
				}
			});
		});
	}
}
