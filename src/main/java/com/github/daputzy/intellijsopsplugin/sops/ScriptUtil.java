package com.github.daputzy.intellijsopsplugin.sops;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScriptUtil {

	@Getter(lazy = true)
	private static final ScriptUtil instance = new ScriptUtil();

	public static final String INPUT_START_IDENTIFIER = "8aa203fd-cc7d-4e00-9c2f-af32b872e4c3";

	private static final String PWSH_SCRIPT =
        """
		param ($file)
		Write-Output "%s"
		$stdin = [System.Console]::In
		$content = $stdin.ReadToEnd()
		$content | Out-File "$file"
		""".formatted(INPUT_START_IDENTIFIER);

	private static final String SHELL_SCRIPT =
	    """
		#!/usr/bin/env sh
		set -eu
		printf "%s"
		cat - > "$1"
		""".formatted(INPUT_START_IDENTIFIER);

	@SneakyThrows(IOException.class)
	public ScriptFiles createScriptFiles() {
		// create temp directory
		final Path tempDirectory = Files.createTempDirectory("simple-sops-edit");

		// make sure temp directory is cleaned on application exit
		FileUtils.forceDeleteOnExit(tempDirectory.toFile());

		if (SystemUtils.IS_OS_WINDOWS) {
			final Path cmdFile = Files.createTempFile(tempDirectory, null, ".cmd");
			final Path pwshFile = Files.createTempFile(tempDirectory, null, ".ps1");

			makeExecutable(cmdFile, pwshFile);

			Files.writeString(pwshFile, PWSH_SCRIPT, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

			final String cmdFileContent = "@powershell.exe -NoProfile -File \"" + pwshFile.toAbsolutePath() + "\" %1";
			Files.writeString(cmdFile, cmdFileContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

			return ScriptFiles.builder().directory(tempDirectory).script(cmdFile).build();
		} else {
			final Path shellFile = Files.createTempFile(tempDirectory, null, ".sh");

			makeExecutable(shellFile);

			Files.writeString(shellFile, SHELL_SCRIPT, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

			return ScriptFiles.builder().directory(tempDirectory).script(shellFile).build();
		}
	}

	private void makeExecutable(final Path... files) {
		if (!Arrays.stream(files).map(Path::toFile).allMatch(f -> f.setExecutable(true))) {
			throw new IllegalStateException("Could not make scripts executable");
		}
	}

	@Builder
	public record ScriptFiles(Path directory, Path script) {}
}
