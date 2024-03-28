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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class ReplaceActionHandler extends ActionHandler {

	public ReplaceActionHandler(@NotNull Project project, @NotNull VirtualFile file) {
		super(project, file);
	}

	public void handle() {
		final VirtualFile inMemoryFile = new LightVirtualFile(
			file.getName(),
			FileUtil.getInstance().getFileType(file),
			StringUtils.EMPTY
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

					if (!closedFileContent.isEmpty()) {
						ExecutionUtil.getInstance().encrypt(
							project,
							file,
							closedFileContent,
							// success
							() -> file.refresh(true, false)
						);

						connection.disconnect();
					}
				}
			}
		});
	}
}
