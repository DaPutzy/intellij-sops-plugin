package com.github.daputzy.intellijsopsplugin;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.util.Consumer;
import com.intellij.util.text.SemVer;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * checks if semver can be found in process output
 * and executes error handler in case version is too old or cannot be found
 */
@RequiredArgsConstructor
class VersionCheckProcessListener implements ProcessListener {

    // from: https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
    private static final Pattern SEMVER_REGEX = Pattern.compile("(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?");

    private final StringBuffer output = new StringBuffer();

    private final Project project;

    private final SemVer minimumVersionRequired;

    private final Consumer<Project> errorHandler;

    @Override
    public void processTerminated(@NotNull final ProcessEvent event) {
        SEMVER_REGEX.matcher(output.toString()).results().findFirst()
            .map(MatchResult::group)
            .map(SemVer::parseFromText)
            .filter(v -> v.isGreaterOrEqualThan(minimumVersionRequired))
            .ifPresentOrElse(
                __ -> {},
                () -> errorHandler.consume(project)
            );
    }

    @Override
    public void onTextAvailable(@NotNull final ProcessEvent event, @NotNull final Key outputType) {
        output.append(event.getText());
    }
}
