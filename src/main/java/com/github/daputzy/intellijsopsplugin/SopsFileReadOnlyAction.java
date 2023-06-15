package com.github.daputzy.intellijsopsplugin;

import java.io.IOException;

import com.github.daputzy.intellijsopsplugin.settings.SettingsState;
import com.github.daputzy.intellijsopsplugin.sops.DetectionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class SopsFileReadOnlyAction implements FileEditorManagerListener {

	@Override
	public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
		if (!SettingsState.getInstance().sopsFilesReadOnly) {
			return;
		}

		if (DetectionUtil.getInstance().sopsFileDetected(source.getProject(), file)) {
			ApplicationManager.getApplication().invokeAndWait(() -> {
				try {
					WriteAction.runAndWait(() -> file.setWritable(false));
				} catch (final IOException e) {
					throw new RuntimeException("Could not set file to read only", e);
				}
			});
		}
	}
}
