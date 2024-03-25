package com.github.daputzy.intellijsopsplugin.handler;

import com.github.daputzy.intellijsopsplugin.file.FileUtil;
import com.github.daputzy.intellijsopsplugin.file.LightVirtualFileWithCustomName;
import com.github.daputzy.intellijsopsplugin.sops.ExecutionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ReplaceActionHandler extends ActionHandler {

	public ReplaceActionHandler(@NotNull Project project, @NotNull VirtualFile file) {
		super(project, file);
	}

	public void handle() {
		final String originalContent = FileUtil.getInstance().getContent(file);

		final VirtualFile inMemoryFile = new LightVirtualFileWithCustomName(
			file,
			FileUtil.getInstance().getFileType(file),
			StringUtils.EMPTY
		);

		ApplicationManager.getApplication().invokeLater(() -> {
			final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

			Arrays.stream(fileEditorManager.getOpenFiles())
					.filter(vFile -> vFile instanceof LightVirtualFileWithCustomName)
					.map(vFile -> (LightVirtualFileWithCustomName) vFile)
					.filter(vFile -> vFile.getFilePath().equals(file.getPath()))
					.findFirst().ifPresentOrElse(
							existingFile -> fileEditorManager.openFile(existingFile, true),
							() -> fileEditorManager.openEditor(new OpenFileDescriptor(project, inMemoryFile), true)
					);
		});

		final MessageBusConnection connection = project.getMessageBus().connect();
		connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
			@Override
			public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile closedFile) {
				if (inMemoryFile.equals(closedFile)) {
					// check if it is our file first, other files may not have a document
					final String closedFileContent = FileUtil.getInstance().getDocument(closedFile).getText();

					if (!closedFileContent.isEmpty()) {
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
	}
}
