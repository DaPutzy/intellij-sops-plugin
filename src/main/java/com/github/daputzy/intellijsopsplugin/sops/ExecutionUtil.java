package com.github.daputzy.intellijsopsplugin.sops;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.daputzy.intellijsopsplugin.settings.SettingsState;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EnvironmentUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExecutionUtil {

	@Getter(lazy = true)
	private static final ExecutionUtil instance = new ExecutionUtil();

	private static final String DEPRECATION_WARNING = "Deprecation Warning";

	/**
	 * decrypts given file
	 *
	 * @param project        project
	 * @param file           file
	 * @param successHandler called on success with decrypted content
	 */
	public void decrypt(final Project project, VirtualFile file, final Consumer<String> successHandler) {
		final GeneralCommandLine command = buildCommand(file.getParent().getPath());

		command.addParameter("-d");
		command.addParameter(file.getName());

		final StringBuffer stdout = new StringBuffer();
		final StringBuffer stderr = new StringBuffer();

		run(command, new ProcessAdapter() {
			@Override
			public void processTerminated(@NotNull ProcessEvent event) {
				notifyOnError(project, stderr);

				if (event.getExitCode() != 0) {
					return;
				}

				successHandler.accept(stdout.toString());
			}

			@Override
			public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
				if (ProcessOutputType.isStderr(outputType) && event.getText() != null) {
					stderr.append(event.getText());
				}

				if (ProcessOutputType.isStdout(outputType) && event.getText() != null) {
					stdout.append(event.getText());
				}
			}
		});
	}

	/**
	 * encrypts given file
	 *
	 * @param project        project
	 * @param file           file
	 * @param successHandler called on success
	 * @param failureHandler called on failure
	 */
	public void encrypt(final Project project, final VirtualFile file, final Runnable successHandler, final Runnable failureHandler) {
		final GeneralCommandLine command = buildCommand(file.getParent().getPath());

		command.addParameter("-e");
		command.addParameter("-i");
		command.addParameter(file.getName());

		final StringBuffer stderr = new StringBuffer();

		run(command, new ProcessAdapter() {
			@Override
			public void processTerminated(@NotNull ProcessEvent event) {
				notifyOnError(project, stderr);

				if (event.getExitCode() != 0) {
					failureHandler.run();
					return;
				}

				successHandler.run();
			}

			@Override
			public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
				if (
					ProcessOutputType.isStderr(outputType) &&
						event.getText() != null &&
						!event.getText().contains(DEPRECATION_WARNING)
				) {
					stderr.append(event.getText());
				}
			}
		});
	}

	/**
	 * edits encrypted file with given content
	 *
	 * @param project        project
	 * @param file           file
	 * @param newContent     new content
	 * @param successHandler called on success
	 */
	@SneakyThrows(IOException.class)
	public void edit(
		final Project project,
		final VirtualFile file,
		final String newContent,
		final Runnable successHandler
	) {
		// get script suffix
		final String scriptSuffix = SystemUtils.IS_OS_WINDOWS ? ".cmd" : ".sh";

		// create temp files
		final Path tempDirectory = Files.createTempDirectory("simple-sops-edit");
		final Path scriptFile = Files.createTempFile(tempDirectory, "script", scriptSuffix);

		// make sure temp directory is cleaned on application exit
		FileUtils.forceDeleteOnExit(tempDirectory.toFile());

		// make sure script is executable
		if (!scriptFile.toFile().setExecutable(true)) {
			throw new IllegalStateException("Could not make script file executable");
		}

		final List<String> scriptFileContent = SystemUtils.IS_OS_WINDOWS ?
			List.of("@powershell.exe -NoProfile -Command \"$env:SOPS_CONTENT | Out-File \\\"%1\\\"\"") :
			List.of(
				"#!/usr/bin/env sh",
				"set -eu",
				"echo \"$SOPS_CONTENT\" > \"$1\""
			);

		Files.write(scriptFile, scriptFileContent, file.getCharset(), StandardOpenOption.APPEND);

		final GeneralCommandLine command = buildCommand(file.getParent().getPath());

		// escape twice for windows because of ENV variable parsing
		final String editorPath = scriptFile.toAbsolutePath().toString().replace("\\", "\\\\");

		command.withEnvironment("EDITOR", editorPath);
		command.withEnvironment("SOPS_CONTENT", newContent);
		command.addParameter(file.getName());

		final StringBuffer stderr = new StringBuffer();

		run(command, new ProcessAdapter() {
			@Override
			public void processTerminated(@NotNull ProcessEvent event) {
				notifyOnError(project, stderr);

				// clean up the temporary files
				FileUtils.deleteQuietly(tempDirectory.toFile());

				if (event.getExitCode() != 0) {
					return;
				}

				successHandler.run();
			}

			@Override
			public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
				if (
					ProcessOutputType.isStderr(outputType) &&
					event.getText() != null &&
					!event.getText().contains(DEPRECATION_WARNING)
				) {
					stderr.append(event.getText());
				}
			}
		});
	}

	private void notifyOnError(final Project project, final StringBuffer stderr) {
		final String error = stderr.toString();

		if (!error.isBlank()) {
			NotificationGroupManager.getInstance()
				.getNotificationGroup("com.github.daputzy.intellijsopsplugin")
				.createNotification("Sops error", error, NotificationType.ERROR)
				.notify(project);
		}
	}

	private void run(final GeneralCommandLine command, final ProcessListener listener) {
		final OSProcessHandler processHandler;
		try {
			processHandler = new OSProcessHandler(command);
		} catch (final ExecutionException e) {
			throw new RuntimeException("Could not execute sops command", e);
		}

		processHandler.addProcessListener(listener);
		processHandler.startNotify();
	}

	private GeneralCommandLine buildCommand(final String cwd) {
		final GeneralCommandLine command = new GeneralCommandLine(SettingsState.getInstance().sopsExecutable)
			.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
			.withCharset(StandardCharsets.UTF_8)
			.withWorkDirectory(cwd);

		final String[] environmentString = SettingsState.getInstance().sopsEnvironment.split("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

		final List<String> environmentList = Arrays.stream(environmentString)
			.map(String::trim)
			.filter(Predicate.not(String::isBlank))
			.toList();

		command.withEnvironment(
			EnvironmentUtil.parseEnv(environmentList.toArray(String[]::new))
		);

		return command;
	}
}
