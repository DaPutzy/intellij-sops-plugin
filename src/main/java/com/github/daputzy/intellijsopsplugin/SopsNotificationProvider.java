package com.github.daputzy.intellijsopsplugin;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SopsNotificationProvider implements EditorNotificationProvider {

	private static final List<String> SOPS_KEYWORDS = List.of(
		"sops",
		"lastmodified",
		"version"
	);

	@Override
	public @NotNull Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(
		@NotNull Project project,
		@NotNull VirtualFile file
	) {
		return Optional.of(file)
			// check if there is a sops config file
			.filter(__ -> ConfigUtil.sopsConfigExists(project, file))
			// get content of file
			.map(FileUtil::getContent)
			// check if all keywords exist in content
			.filter(content -> SOPS_KEYWORDS.stream().allMatch(content::contains))
			.map(__ -> createNotification(project, file))
			.orElse(__ -> null);
	}

	private Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> createNotification(
		@NotNull Project project,
		@NotNull VirtualFile file
	) {
		return __ -> {
			final EditorNotificationPanel panel = new EditorNotificationPanel();

			panel.setText("Sops file detected");
			panel.createActionLabel("Edit", new EditActionHandler(project, file)::handle);

			return panel;
		};
	}
}
