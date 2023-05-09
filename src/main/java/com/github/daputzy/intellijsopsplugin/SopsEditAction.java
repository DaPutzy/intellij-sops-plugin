package com.github.daputzy.intellijsopsplugin;

import com.github.daputzy.intellijsopsplugin.handler.EditActionHandler;
import com.github.daputzy.intellijsopsplugin.sops.DetectionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class SopsEditAction extends AnAction {

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		final Project project = e.getProject();
		final VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

		if (project == null || file == null) {
			throw new IllegalStateException();
		}

		final EditActionHandler actionHandler = new EditActionHandler(project, file);
		actionHandler.handle();
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabled(false);

		final Project project = e.getProject();
		final VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

		if (project == null || file == null) {
			return;
		}

		if (DetectionUtil.getInstance().sopsFileDetected(project, file)) {
			e.getPresentation().setEnabled(true);
		}
	}
}
