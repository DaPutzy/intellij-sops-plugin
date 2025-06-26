package com.github.daputzy.intellijsopsplugin;

import javax.swing.JComponent;
import java.util.List;
import java.util.function.Function;

import com.github.daputzy.intellijsopsplugin.action.SopsAction;
import com.github.daputzy.intellijsopsplugin.action.EditSopsAction;
import com.github.daputzy.intellijsopsplugin.action.ReplaceSopsAction;
import com.github.daputzy.intellijsopsplugin.action.ViewSopsAction;
import com.github.daputzy.intellijsopsplugin.sops.DetectionUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import org.jetbrains.annotations.NotNull;

public class SopsNotificationProvider implements EditorNotificationProvider {

	private static final List<SopsAction> SOPS_ACTIONS = List.of(
		new ViewSopsAction(),
		new EditSopsAction(),
		new ReplaceSopsAction()
	);

	@Override
	public @NotNull Function<? super FileEditor, ? extends JComponent> collectNotificationData(
		@NotNull final Project project,
		@NotNull final VirtualFile file
	) {
		if (!DetectionUtil.getInstance().sopsFileDetected(file)) {
			return __ -> null;
		}

		return __ -> {
			final EditorNotificationPanel panel = new EditorNotificationPanel();

			panel.setText("Sops file detected");

			SOPS_ACTIONS.stream()
				.filter(action -> action.supports(file))
				.forEach(action -> panel.createActionLabel(
					action.getName(),
					() -> action.handle(project, file)
				));

			return panel;
		};
	}
}
