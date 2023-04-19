package com.github.daputzy.intellijsopsplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
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
		final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
		final FileType fileType = FileTypeRegistry.getInstance().getFileTypeByFileName(file.getName());
		final String originalContent = FileUtil.getContent(file);

		ExecutionUtil.decrypt(project, file, decryptedContent -> {
			final LightVirtualFile inMemoryFile = new LightVirtualFile(file.getName(), fileType, decryptedContent);

			ApplicationManager.getApplication()
				.invokeLater(() -> fileEditorManager.openFile(inMemoryFile, true));

			final MessageBusConnection connection = project.getMessageBus().connect();
			connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
				@Override
				public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile closedFile) {
					final String closedFileContent = FileUtil.getDocument(closedFile).getText();

					if (inMemoryFile.equals(closedFile) && !closedFileContent.equals(decryptedContent)) {
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
			});
		});
	}
}
