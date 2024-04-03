package com.github.daputzy.intellijsopsplugin.action;

import java.util.Arrays;

import com.github.daputzy.intellijsopsplugin.file.DecryptedSopsFileWithReference;
import com.github.daputzy.intellijsopsplugin.sops.ExecutionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ViewSopsAction extends SopsAction {

	@Override
	public void handle(final Project project, final VirtualFile file) {
		ExecutionUtil.getInstance().decrypt(project, file, decryptedContent -> {
			final ViewActionVirtualFile inMemoryFile = new ViewActionVirtualFile(file, decryptedContent);
			inMemoryFile.setWritable(false);

			ApplicationManager.getApplication().invokeLater(() -> {
				final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

				Arrays.stream(fileEditorManager.getOpenFiles())
					.filter(ViewActionVirtualFile.class::isInstance)
					.map(ViewActionVirtualFile.class::cast)
					.filter(virtualFile -> virtualFile.getFilePath().equals(file.getPath()))
					.findFirst()
					.ifPresentOrElse(
						virtualFile -> fileEditorManager.openFile(virtualFile, true),
						() -> fileEditorManager.openFile(inMemoryFile, true)
					);
			});
		});
	}

	static class ViewActionVirtualFile extends DecryptedSopsFileWithReference {

		public ViewActionVirtualFile(final VirtualFile original, final String content) {
			super(original, content, "[view]");
		}
	}
}
