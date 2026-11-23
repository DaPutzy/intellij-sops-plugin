package com.github.daputzy.intellijsopsplugin.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import javax.swing.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class SettingsConfigurable implements Configurable {

	private SettingsComponent settingsComponent;

	@Override
	@Nls(capitalization = Nls.Capitalization.Title)
	public String getDisplayName() {
		return "Simple Sops Settings";
	}

	@Override
	public @Nullable JComponent getPreferredFocusedComponent() {
		return settingsComponent.getPreferredFocusedComponent();
	}

	@Override
	public @Nullable JComponent createComponent() {
		settingsComponent = new SettingsComponent();

		return settingsComponent.getPanel();
	}

	@Override
	public boolean isModified() {
		final SettingsState settings = SettingsState.getInstance();

		return !Objects.equals(settings.sopsEnvironmentVariables, settingsComponent.getSopsEnvironmentVariables()) ||
			!Objects.equals(settings.sopsExecutable, settingsComponent.getSopsExecutable()) ||
			!Objects.equals(settings.sopsFilesReadOnly, settingsComponent.getSopsFilesReadOnly());
	}

	@Override
	public void apply() throws ConfigurationException {
		settingsComponent.stopEditing();

		final List<EnvironmentVariable> environmentVariables = settingsComponent.getSopsEnvironmentVariables();

		for (final EnvironmentVariable variable : environmentVariables) {
			if (variable.name.contains("=")) {
				throw new ConfigurationException(
					"Environment variable name must not contain '=': " + variable.name,
					"Invalid Sops Environment"
				);
			}
		}

		final SettingsState settings = SettingsState.getInstance();

		settings.sopsEnvironmentVariables = new ArrayList<>(environmentVariables);
		settings.sopsExecutable = settingsComponent.getSopsExecutable();
		settings.sopsFilesReadOnly = settingsComponent.getSopsFilesReadOnly();
	}

	@Override
	public void reset() {
		final SettingsState settings = SettingsState.getInstance();

		settingsComponent.setSopsEnvironmentVariables(settings.sopsEnvironmentVariables);
		settingsComponent.setSopsExecutable(settings.sopsExecutable);
		settingsComponent.setSopsFilesReadOnly(settings.sopsFilesReadOnly);
	}

	@Override
	public void disposeUIResources() {
		settingsComponent = null;
	}
}
