package com.github.daputzy.intellijsopsplugin.sops;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ErrorNotificationProcessListener implements ProcessListener {

	private static final String DEPRECATION_WARNING = "Deprecation Warning";

	private final StringBuffer stderr = new StringBuffer();

	private final Project project;

	@Override
	public void processTerminated(@NotNull final ProcessEvent event) {
		final String error = stderr.toString();

		if (!error.isBlank()) {
			NotificationGroupManager.getInstance()
				.getNotificationGroup("com.github.daputzy.intellijsopsplugin")
				.createNotification("Sops error", error, NotificationType.ERROR)
				.notify(project);
		}
	}

	@Override
	public void onTextAvailable(@NotNull final ProcessEvent event, @NotNull final Key outputType) {
		if (
			ProcessOutputType.isStderr(outputType) &&
			event.getText() != null &&
			!event.getText().contains(DEPRECATION_WARNING)
		) {
			stderr.append(event.getText());
		}
	}
}
