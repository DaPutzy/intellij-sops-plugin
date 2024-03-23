package com.github.daputzy.intellijsopsplugin.sops;

import java.util.List;
import java.util.Optional;

import com.github.daputzy.intellijsopsplugin.file.FileUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DetectionUtil {

	@Getter(lazy = true)
	private static final DetectionUtil instance = new DetectionUtil();

	// TODO: were all these keywords available in older versions of sops?
	public static final List<String> SOPS_KEYWORDS = List.of(
		"sops",
		"lastmodified",
		"version"
	);

	public static final List<String> SOPS_INLINE_CONFIG_KEYWORDS = List.of(
		"sops_pgp",
		"sops_kms",
		"sops_gcp_kms",
		"sops_azure_kv",
		"sops_hc_vault",
		"sops_age"
	);

	private boolean sopsFileWithInlineConfigDetected(@NotNull final String content) {
		//check if the file is a sops file with inline config
		return SOPS_KEYWORDS.stream().allMatch(content::contains)
			&& SOPS_INLINE_CONFIG_KEYWORDS.stream().anyMatch(content::contains);
	}

	private boolean sopsFileWithConfigFileDetected(@NotNull final Project project, @NotNull final VirtualFile file) {
		return Optional.of(file)
			// check if there is a sops config file
			.filter(__ -> ConfigUtil.getInstance().sopsConfigExists(project, file))
			// get content of file
			.map(FileUtil.getInstance()::getContent)
			// check if all keywords exist in content
			.filter(content -> SOPS_KEYWORDS.stream().allMatch(content::contains))
			.isPresent();
	}

	public boolean sopsFileDetected(@NotNull final Project project, @NotNull final VirtualFile file) {
		return this.sopsFileWithConfigFileDetected(project, file) ||
			this.sopsFileWithInlineConfigDetected(FileUtil.getInstance().getContent(file));
	}
}
