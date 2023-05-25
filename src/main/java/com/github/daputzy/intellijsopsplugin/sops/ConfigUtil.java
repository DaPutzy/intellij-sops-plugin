package com.github.daputzy.intellijsopsplugin.sops;

import java.util.stream.Stream;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigUtil {

	@Getter(lazy = true)
	private static final ConfigUtil instance = new ConfigUtil();

	private static final String SOPS_CONFIG_FILE = ".sops.yaml";

	public boolean sopsConfigExists(@NotNull final Project project, @NotNull final VirtualFile file) {
		final VirtualFile projectDir = ProjectUtil.guessProjectDir(project);

		if (projectDir != null) {
			VirtualFile current = file;

			do {
				current = current.getParent();

				if (current == null) {
					return false;
				}

				if (Stream.of(current.getChildren()).map(VirtualFile::getName).anyMatch(SOPS_CONFIG_FILE::equals)) {
					return true;
				}
			} while (!projectDir.equals(current));
		}

		return false;
	}
}
