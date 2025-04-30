package com.github.daputzy.intellijsopsplugin.sops;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EnvironmentUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExecutionUtil {

	@Getter(lazy = true)
	private static final ExecutionUtil instance = new ExecutionUtil();

	/**
	 * decrypts given file
	 *
	 * @param project        project
	 * @param file           file
	 * @param successHandler called on success with decrypted content
	 */
	public void decrypt(final Project project, File file, final Consumer<String> successHandler) {
		final GeneralCommandLine command = buildCommand(file.getParent());

		command.addParameter("-d");
		command.addParameter(file.getName());

		run(
			command,
			new ErrorNotificationProcessListener(project),
			new ProcessAdapter() {
				private final StringBuffer stdout = new StringBuffer();

				@Override
				public void processTerminated(@NotNull final ProcessEvent event) {
					if (event.getExitCode() == 0) {
						successHandler.accept(stdout.toString());
					}
				}

				@Override
				public void onTextAvailable(@NotNull final ProcessEvent event, @NotNull final Key outputType) {
					if (ProcessOutputType.isStdout(outputType) && event.getText() != null) {
						stdout.append(event.getText());
					}
				}
			}
		);
	}

	/**
	 * encrypts given file
	 *
	 * @param project        project
	 * @param file           file
	 * @param successHandler called on success
	 * @param failureHandler called on failure
	 */
	public void encrypt(
		final Project project,
		final VirtualFile file,
		final Runnable successHandler,
		final Runnable failureHandler
	) {
		final GeneralCommandLine command = buildCommand(file.getParent().getPath());

		command.addParameter("-e");
		command.addParameter("-i");
		command.addParameter(file.getName());

		run(
			command,
			new ErrorNotificationProcessListener(project),
			new ProcessAdapter() {
				@Override
				public void processTerminated(@NotNull final ProcessEvent event) {
					if (event.getExitCode() == 0) {
						successHandler.run();
					} else {
						failureHandler.run();
					}
				}
			}
		);
	}

	/**
	 * edits encrypted file with given content
	 *
	 * @param project        project
	 * @param file           file
	 * @param newContent     new content
	 * @param successHandler called on success
	 * @param failureHandler called on failure
	 */
	public void edit(
		final Project project,
		final VirtualFile file,
		final String newContent,
		final Runnable successHandler,
		final Runnable failureHandler
	) {
		final ScriptUtil.ScriptFiles scriptFiles = ScriptUtil.getInstance().createScriptFiles();

		final GeneralCommandLine command = buildCommand(file.getParent().getPath());

		final String editorPath = scriptFiles.script().toAbsolutePath().toString()
			// escape twice for windows because of ENV variable parsing
			.replace("\\", "\\\\")
			// escape whitespaces
			.replace(" ", "\\ ");

		command.withEnvironment("EDITOR", editorPath);
		command.addParameter(file.getName());

		run(
			command,
			new ErrorNotificationProcessListener(project),
			new ProcessAdapter() {
				private final AtomicBoolean failed = new AtomicBoolean(false);

				@Override
				public void processTerminated(@NotNull final ProcessEvent event) {
					// clean up the temporary files
					FileUtils.deleteQuietly(scriptFiles.directory().toFile());

					if (event.getExitCode() == 0 && !failed.get()) {
						successHandler.run();
					} else {
						failureHandler.run();
					}
				}

				@Override
				@SneakyThrows(IOException.class)
				public void onTextAvailable(@NotNull final ProcessEvent event, @NotNull final Key outputType) {
					if (null != event.getText() && ScriptUtil.INPUT_START_IDENTIFIER.equals(event.getText().trim())) {
						IOUtils.write(newContent, event.getProcessHandler().getProcessInput(), file.getCharset());
						event.getProcessHandler().getProcessInput().close();
					}

					if (ProcessOutputType.isStderr(outputType)) {
						event.getProcessHandler().destroyProcess();
						// destroy process is apparently perfectly fine and exit code is 0
						failed.set(true);
					}
				}
			}
		);
	}

	private void run(final GeneralCommandLine command, final ProcessListener... listener) {
		final OSProcessHandler processHandler;
		try {
			processHandler = new OSProcessHandler(command);
		} catch (final ExecutionException e) {
			throw new RuntimeException("Could not execute sops command", e);
		}

		Arrays.stream(listener).forEach(processHandler::addProcessListener);

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

		// https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_execution_policies
		command.withEnvironment("PSExecutionPolicyPreference", "Bypass");

		return command;
	}
}
