package com.github.daputzy.intellijsopsplugin;

import java.util.stream.Stream;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConfigUtil {

	private static final String SOPS_CONFIG_FILE = ".sops.yaml";

	public boolean sopsConfigExists(final Project project, final VirtualFile file) {
		final VirtualFile projectDir = ProjectUtil.guessProjectDir(project);

		if (projectDir != null) {
			VirtualFile parent = file.getParent();

			while (parent != null && !projectDir.equals(parent)) {
				if (Stream.of(parent.getChildren()).map(VirtualFile::getName).anyMatch(SOPS_CONFIG_FILE::equals)) {
					return true;
				}

				parent = parent.getParent();
			}
		}

		return false;
	}
}
