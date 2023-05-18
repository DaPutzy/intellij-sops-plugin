package com.github.daputzy.intellijsopsplugin.settings;

import com.intellij.openapi.options.Configurable;
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

		return !settings.sopsExecutable.equals(settingsComponent.getSopsExecutable());
	}

	@Override
	public void apply() {
		final SettingsState settings = SettingsState.getInstance();

		settings.sopsExecutable = settingsComponent.getSopsExecutable();
	}

	@Override
	public void reset() {
		final SettingsState settings = SettingsState.getInstance();

		settingsComponent.setSopsExecutable(settings.sopsExecutable);
	}

	@Override
	public void disposeUIResources() {
		settingsComponent = null;
	}
}
