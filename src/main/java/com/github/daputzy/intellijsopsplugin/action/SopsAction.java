package com.github.daputzy.intellijsopsplugin.action;

import com.github.daputzy.intellijsopsplugin.sops.DetectionUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public abstract class SopsAction extends AnAction {

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

	public abstract void handle(final Project project, final VirtualFile file);

	@Override
	public void actionPerformed(@NotNull final AnActionEvent e) {
		final Project project = e.getProject();
		final VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

		if (project == null || file == null) {
			throw new IllegalStateException();
		}

		handle(project, file);
	}

	@Override
	public void update(@NotNull final AnActionEvent e) {
		e.getPresentation().setEnabled(false);

		final Project project = e.getProject();
		final VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

		if (project == null || file == null) {
			return;
		}

		if (DetectionUtil.getInstance().sopsFileDetected(file)) {
			e.getPresentation().setEnabled(true);
		}
	}
}
