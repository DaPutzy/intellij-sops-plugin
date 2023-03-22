package com.github.daputzy.intellijsopsplugin;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
			.filter(document -> ConfigUtil.sopsConfigExists(project, file))
			.map(FileDocumentManager.getInstance()::getDocument)
			// check if all keywords exist in the file
			.filter(document -> SOPS_KEYWORDS.stream().allMatch(document.getText()::contains))
			.map(document -> createNotification(file, document))
			.orElse(__ -> null);
	}

	private Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> createNotification(
		@NotNull VirtualFile file,
		@NotNull Document document
	) {
		final VirtualFile parent = file.getParent();

		return __ -> {
			final EditorNotificationPanel panel = new EditorNotificationPanel();
			panel.setText("Sops file detected");
			panel.createActionLabel("Edit", () -> {
				document.setReadOnly(true);

				ExecutionUtil.execute(parent.getPath(), file.getName(), () -> {
					document.setReadOnly(false);
					file.refresh(false, false);
				});
			});
			return panel;
		};
	}
}
