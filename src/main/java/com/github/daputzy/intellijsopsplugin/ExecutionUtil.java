package com.github.daputzy.intellijsopsplugin;

import java.nio.charset.StandardCharsets;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class ExecutionUtil {

	private static final String SOPS_COMMAND = "sops";

	private static final String EDITOR_ENV_KEY = "EDITOR";
	private static final String EDITOR_ENV_VALUE = "idea --wait";

	@SneakyThrows
	public void execute(final String directory, final String fileName, Runnable afterCommandFinished) {
		final GeneralCommandLine command = new GeneralCommandLine(SOPS_COMMAND);
		command.setWorkDirectory(directory);
		command.withEnvironment(EDITOR_ENV_KEY, EDITOR_ENV_VALUE);
		command.addParameters(fileName);
		command.setCharset(StandardCharsets.UTF_8);

		final OSProcessHandler processHandler = new OSProcessHandler(command);

		processHandler.addProcessListener(new ProcessAdapter() {
			@Override
			public void processTerminated(@NotNull ProcessEvent event) {
				afterCommandFinished.run();
			}
		});

		processHandler.startNotify();
	}
}
