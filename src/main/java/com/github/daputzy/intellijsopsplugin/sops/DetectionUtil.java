package com.github.daputzy.intellijsopsplugin.sops;

import java.util.List;

import com.github.daputzy.intellijsopsplugin.file.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DetectionUtil {

	@Getter(lazy = true)
	private static final DetectionUtil instance = new DetectionUtil();

	public static final List<String> SOPS_KEYWORDS = List.of(
		"sops",
		"lastmodified",
		"version"
	);

	/**
	 * Only check if we can detect a sops file, do not check if a sops config exists!
	 * <p>
	 * Sops should always return a useful error messages and with the right environment
	 * you can view, edit and replace without a sops config, e.g.:
	 * SOPS_AGE_RECIPIENTS=foo SOPS_AGE_KEY_FILE=bar
	 *
	 * @param file file
	 * @return if a sops file was detected
	 */
	public boolean sopsFileDetected(@NotNull final VirtualFile file) {
		final String content = FileUtil.getInstance().getContent(file);

		return SOPS_KEYWORDS.stream().allMatch(content::contains);
	}
}
