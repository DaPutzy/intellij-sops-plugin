package com.github.daputzy.intellijsopsplugin.settings;

import com.intellij.execution.wsl.WSLDistribution;
import com.intellij.execution.wsl.WslDistributionManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@State(
	name = "com.github.daputzy.intellijsopsplugin.settings.SettingsState",
	storages = @Storage("SopsSettingsPlugin.xml")
)
public class SettingsState implements PersistentStateComponent<SettingsState> {

	public String sopsExecutable = "sops";
	public boolean sopsUseWSL = false;
	public boolean sopsFilesReadOnly = false;
	public String sopsEnvironment = "";
	public String sopsWslDistributionName = null;
	@Transient
	private WSLDistribution sopsWslDistribution = null;

	public Optional<WSLDistribution> tryGetWslDistribution() {
		if (sopsWslDistribution != null) return Optional.of(sopsWslDistribution);
		Optional<WSLDistribution> distribution = WslDistributionManager.getInstance().getInstalledDistributions().stream()
				.filter(d -> d.getPresentableName().equals(sopsWslDistributionName))
				.findFirst();
		distribution.ifPresent(d -> sopsWslDistribution = d);
		return distribution;
	}

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
