package com.github.daputzy.intellijsopsplugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
	name = "com.github.daputzy.intellijsopsplugin.settings.SettingsState",
	storages = @Storage("SopsSettingsPlugin.xml")
)
public class SettingsState implements PersistentStateComponent<SettingsState> {

	public String sopsExecutable = "sops";
	public boolean sopsFilesReadOnly = false;
	public String sopsEnvironment = "";

	public static SettingsState getInstance() {
		return ApplicationManager.getApplication().getService(SettingsState.class);
	}

	@Override
	public @Nullable SettingsState getState() {
		return this;
	}

	@Override
	public void loadState(@NotNull SettingsState state) {
		XmlSerializerUtil.copyBean(state, this);
	}
}
