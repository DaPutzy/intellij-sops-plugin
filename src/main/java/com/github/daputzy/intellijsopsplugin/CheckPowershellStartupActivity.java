package com.github.daputzy.intellijsopsplugin;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessNotCreatedException;
import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.util.text.SemVer;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

/**
 * checks if required powershell version is installed on project startup
 */
public class CheckPowershellStartupActivity implements ProjectActivity {

    private static final SemVer MINIMUM_REQUIRED_VERSION = SemVer.parseFromText("7.0.0");

    private static final String ERROR_MESSAGE =
        """
        <p>PowerShell >= 7 is required for this plugin.</p>
        <p>Please follow the installation instructions:</p>
        """;

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        if (!SystemUtils.IS_OS_WINDOWS) return null;

        final GeneralCommandLine command = new GeneralCommandLine("pwsh.exe")
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withCharset(StandardCharsets.UTF_8)
            .withParameters("-Version");

        final OSProcessHandler processHandler;
        try {
            processHandler = new OSProcessHandler(command);
            processHandler.addProcessListener(new VersionCheckProcessListener(project, MINIMUM_REQUIRED_VERSION, this::handleError));
            processHandler.startNotify();
        } catch (final ProcessNotCreatedException e) {
            handleError(project);
        } catch (final ExecutionException e) {
            throw new IllegalStateException("Could not check if powershell is installed", e);
        }

        return null;
    }

    private void handleError(final Project project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("com.github.daputzy.intellijsopsplugin")
            .createNotification("Sops plugin ", ERROR_MESSAGE, NotificationType.WARNING)
            .addAction(new BrowseNotificationAction(
                "Official installation instructions",
                "https://learn.microsoft.com/en-us/powershell/scripting/install/installing-powershell-on-windows"
            ))
            .setImportant(true)
            .notify(project);
    }
}
