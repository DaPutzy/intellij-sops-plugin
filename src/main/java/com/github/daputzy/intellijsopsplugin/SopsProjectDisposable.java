package com.github.daputzy.intellijsopsplugin;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * project level parent disposable of the plugin, disposed on project close and plugin unload
 * (the project itself must not be used as parent disposable by plugins)
 */
@Service(Service.Level.PROJECT)
public final class SopsProjectDisposable implements Disposable {

	public static @NotNull Disposable getInstance(@NotNull final Project project) {
		return project.getService(SopsProjectDisposable.class);
	}

	@Override
	public void dispose() {
		// nothing to do, children are disposed by the platform
	}
}
