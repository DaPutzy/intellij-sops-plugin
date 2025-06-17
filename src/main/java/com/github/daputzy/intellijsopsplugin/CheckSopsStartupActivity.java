package com.github.daputzy.intellijsopsplugin;

import com.github.daputzy.intellijsopsplugin.sops.ExecutionUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessNotCreatedException;
import com.intellij.notification.BrowseNotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.util.text.SemVer;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * checks if required sops version is installed on project startup
 */
public class CheckSopsStartupActivity implements ProjectActivity {

    private static final SemVer MINIMUM_REQUIRED_VERSION = SemVer.parseFromText("3.10.0");

    private static final String ERROR_MESSAGE =
        """
        <p>Sops >= 3.10.0 is required for this plugin.</p>
        <p>Latest stable release:</p>
        """;

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        final GeneralCommandLine command = ExecutionUtil.getInstance().buildCommand(ProjectUtil.guessProjectDir(project))
            .withParameters("--version");

        final OSProcessHandler processHandler;
        try {
            processHandler = new OSProcessHandler(command);
            processHandler.addProcessListener(new VersionCheckProcessListener(project, MINIMUM_REQUIRED_VERSION, this::handleError));
            processHandler.startNotify();
        } catch (final ProcessNotCreatedException e) {
            handleError(project);
        } catch (final ExecutionException e) {
            throw new IllegalStateException("Could not check if sops is installed", e);
        }

        return null;
    }

    private void handleError(final Project project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("com.github.daputzy.intellijsopsplugin")
            .createNotification("Sops plugin ", ERROR_MESSAGE, NotificationType.WARNING)
            .addAction(new BrowseNotificationAction(
	            "Official github release page",
                "https://github.com/getsops/sops/releases/latest"
            ))
            .setImportant(true)
            .notify(project);
    }
}
