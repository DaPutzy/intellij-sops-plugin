package com.github.daputzy.intellijsopsplugin;

import java.util.function.Function;

import com.github.daputzy.intellijsopsplugin.handler.EditActionHandler;
import com.github.daputzy.intellijsopsplugin.handler.ReplaceActionHandler;
import com.github.daputzy.intellijsopsplugin.sops.DetectionUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SopsNotificationProvider implements EditorNotificationProvider {

	@Override
	public @NotNull Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(
		@NotNull final Project project,
		@NotNull final VirtualFile file
	) {
		if (!DetectionUtil.getInstance().sopsFileDetected(project, file)) {
			return __ -> null;
		}

		return __ -> {
			final EditorNotificationPanel panel = new EditorNotificationPanel();

			panel.setText("Sops file detected");
			panel.createActionLabel("Edit", new EditActionHandler(project, file)::handle);
			panel.createActionLabel("Replace", new ReplaceActionHandler(project, file)::handle);

			return panel;
		};
	}
}
