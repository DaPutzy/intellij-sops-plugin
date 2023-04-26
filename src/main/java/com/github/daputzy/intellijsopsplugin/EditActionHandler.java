package com.github.daputzy.intellijsopsplugin;

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
		final String originalContent = FileUtil.getContent(file);

		ExecutionUtil.decrypt(project, file, decryptedContent -> {
			final VirtualFile inMemoryFile = new LightVirtualFile(
				file.getName(),
				FileUtil.getFileType(file),
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
						final String closedFileContent = FileUtil.getDocument(closedFile).getText();

						if (!closedFileContent.equals(decryptedContent)) {
							FileUtil.writeContentBlocking(file, closedFileContent);

							ExecutionUtil.encrypt(
								project,
								file,
								// success
								() -> file.refresh(true, false),
								// failure
								() -> FileUtil.writeContentBlocking(file, originalContent)
							);

							connection.disconnect();
						}
					}
				}
			});
		});
	}
}
