package com.github.daputzy.intellijsopsplugin.handler;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public abstract class ActionHandler {

	@NotNull
	protected final Project project;

	@NotNull
	protected final VirtualFile file;

	public abstract void handle();
}
