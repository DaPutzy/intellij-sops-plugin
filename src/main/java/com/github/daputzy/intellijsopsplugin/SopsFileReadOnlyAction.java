package com.github.daputzy.intellijsopsplugin;

import java.util.List;

import com.github.daputzy.intellijsopsplugin.settings.SettingsState;
import com.github.daputzy.intellijsopsplugin.sops.DetectionUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.ex.FileEditorWithProvider;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class SopsFileReadOnlyAction implements FileEditorManagerListener {

	@Override
	public void fileOpenedSync(@NotNull FileEditorManager source, @NotNull VirtualFile file, @NotNull List<FileEditorWithProvider> editorsWithProviders) {
		if (!SettingsState.getInstance().sopsFilesReadOnly) {
			return;
		}

		if (DetectionUtil.getInstance().sopsFileDetected(source.getProject(), file)) {
			editorsWithProviders.stream()
				.map(FileEditorWithProvider::getFileEditor)
				.filter(TextEditor.class::isInstance)
				.map(TextEditor.class::cast)
				.map(TextEditor::getEditor)
				.map(Editor::getDocument)
				.forEach(document -> document.setReadOnly(true));
		}
	}
}
