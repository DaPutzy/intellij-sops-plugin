package com.github.daputzy.intellijsopsplugin.action;

import com.github.daputzy.intellijsopsplugin.handler.ActionHandler;
import com.github.daputzy.intellijsopsplugin.sops.DetectionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

@RequiredArgsConstructor
public abstract class SopsAction extends AnAction {

	protected final BiFunction<Project, VirtualFile, ActionHandler> actionHandlerSupplier;

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		final Project project = e.getProject();
		final VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

		if (project == null || file == null) {
			throw new IllegalStateException();
		}

		final ActionHandler actionHandler = actionHandlerSupplier.apply(project, file);
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
