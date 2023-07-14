package com.github.daputzy.intellijsopsplugin.sops;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExecutionUtil {

	@Getter(lazy = true)
	private static final ExecutionUtil instance = new ExecutionUtil();

	private static final String DEPRECATION_WARNING = "Deprecation Warning";

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

	public void encrypt(final Project project, VirtualFile file, final Runnable successHandler, final Runnable failureHandler) {
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
			.collect(Collectors.toList());

		command.withEnvironment(
			EnvironmentUtil.parseEnv(environmentList.toArray(String[]::new))
		);

		return command;
	}
}
