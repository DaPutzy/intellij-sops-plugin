package com.github.daputzy.intellijsopsplugin.handler;

import com.github.daputzy.intellijsopsplugin.file.FileUtil;
import com.github.daputzy.intellijsopsplugin.file.DecryptedSopsFileWithReference;
import com.github.daputzy.intellijsopsplugin.sops.ExecutionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class EditActionHandler extends ActionHandler {

	public EditActionHandler(@NotNull Project project, @NotNull VirtualFile file) {
		super(project, file);
	}

	public void handle() {
		ExecutionUtil.getInstance().decrypt(project, file, decryptedContent -> {
			final String originalContent = FileUtil.getInstance().getContent(file);

			final VirtualFile inMemoryFile = new EditActionVirtualFile(file, decryptedContent);

			ApplicationManager.getApplication().invokeLater(() -> {
				final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

				final Optional<EditActionVirtualFile> openFile = Arrays.stream(fileEditorManager.getOpenFiles())
					.filter(EditActionVirtualFile.class::isInstance)
					.map(EditActionVirtualFile.class::cast)
					.filter(virtualFile -> virtualFile.getFilePath().equals(file.getPath()))
					.findFirst();

				if (openFile.isPresent()) {
					fileEditorManager.openFile(openFile.get(), true);
				} else {
					fileEditorManager.openFile(inMemoryFile, true);

					final MessageBusConnection connection = project.getMessageBus().connect();
					connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
						@Override
						public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile closedFile) {
							if (inMemoryFile.equals(closedFile)) {
								// check if it is our file first, other files may not have a document
								final String closedFileContent = FileUtil.getInstance().getDocument(closedFile).getText();

								if (!closedFileContent.equals(decryptedContent)) {
									ExecutionUtil.getInstance().edit(
										project,
										file,
										closedFileContent,
										// success
										() -> file.refresh(false, false),
										// failure
										() -> FileUtil.getInstance().writeContentBlocking(file, originalContent)
									);

									connection.disconnect();
								}
							}
						}
					});
				}
			});
		});
	}

	static class EditActionVirtualFile extends DecryptedSopsFileWithReference {

		public EditActionVirtualFile(final VirtualFile original, final String content) {
			super(original, content, "[edit]");
		}
	}
}
