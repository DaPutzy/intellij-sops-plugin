package com.github.daputzy.intellijsopsplugin;

import java.nio.charset.StandardCharsets;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class ExecutionUtil {

	private static final String SOPS_COMMAND = "sops";

	private static final String EDITOR_ENV_KEY = "EDITOR";
	private static final String EDITOR_ENV_VALUE = "idea --wait";

	@SneakyThrows
	public void execute(final Project project, final String directory, final String fileName, Runnable afterCommandFinished) {
		final GeneralCommandLine command = new GeneralCommandLine(SOPS_COMMAND);
		command.setWorkDirectory(directory);
		command.withEnvironment(EDITOR_ENV_KEY, EDITOR_ENV_VALUE);
		command.addParameters(fileName);
		command.setCharset(StandardCharsets.UTF_8);

		final OSProcessHandler processHandler = new OSProcessHandler(command);

		final StringBuffer sb = new StringBuffer();

		processHandler.addProcessListener(new ProcessAdapter() {
			@Override
			public void processTerminated(@NotNull ProcessEvent event) {
				final String error = sb.toString();

				if (event.getExitCode() != 0 || !error.isBlank()) {
					NotificationGroupManager.getInstance()
						.getNotificationGroup("com.github.daputzy.intellijsopsplugin")
						.createNotification("Sops error", error, NotificationType.ERROR)
						.notify(project);
				}

				afterCommandFinished.run();
			}

			@Override
			public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
				if (ProcessOutputType.isStderr(outputType) && event.getText() != null) {
					sb.append(event.getText());
				}
			}
		});

		processHandler.startNotify();
	}
}
