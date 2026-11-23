package com.github.daputzy.intellijsopsplugin.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
	name = "com.github.daputzy.intellijsopsplugin.settings.SettingsState",
	storages = @Storage("SopsSettingsPlugin.xml")
)
public class SettingsState implements PersistentStateComponent<SettingsState> {

	/**
	 * whitespace that is not inside double quotes, i.e. the separator used by the legacy single line environment setting
	 */
	private static final Pattern LEGACY_ENVIRONMENT_SEPARATOR = Pattern.compile("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

	public String sopsExecutable = "sops";
	public boolean sopsFilesReadOnly = false;

	@XCollection(style = XCollection.Style.v2)
	public List<EnvironmentVariable> sopsEnvironmentVariables = new ArrayList<>();

	/**
	 * legacy single line environment setting (e.g. {@code FOO=bar BAZ=qux}), only kept to migrate existing settings
	 *
	 * @deprecated use {@link #sopsEnvironmentVariables}
	 */
	@Deprecated
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
		migrateLegacyEnvironment();
	}

	/**
	 * moves the legacy single line environment setting into {@link #sopsEnvironmentVariables}
	 */
	@SuppressWarnings("deprecation")
	void migrateLegacyEnvironment() {
		if (sopsEnvironment == null || sopsEnvironment.isBlank()) {
			sopsEnvironment = "";
			return;
		}

		if (sopsEnvironmentVariables == null) {
			sopsEnvironmentVariables = new ArrayList<>();
		}

		// only migrate once, if there are already entries the user has configured them with the new setting
		if (sopsEnvironmentVariables.isEmpty()) {
			sopsEnvironmentVariables.addAll(parseLegacyEnvironment(sopsEnvironment));
		}

		sopsEnvironment = "";
	}

	/**
	 * parses the legacy single line environment setting
	 * <ul>
	 *     <li>entries are separated by whitespace outside of double quotes</li>
	 *     <li>one pair of double quotes surrounding a value is removed (they used to be passed on to sops unchanged)</li>
	 *     <li>a part without {@code =} is appended to the value of the previous entry, e.g.
	 *     {@code SOPS_AGE_KEY_CMD=keyring get sops-age default} (this used to fail)</li>
	 *     <li>a part without a name (e.g. {@code =foo}) or without a previous entry is dropped (this used to fail)</li>
	 * </ul>
	 *
	 * @param environment legacy environment setting
	 * @return parsed environment variables
	 */
	static List<EnvironmentVariable> parseLegacyEnvironment(@NotNull final String environment) {
		final List<EnvironmentVariable> result = new ArrayList<>();

		for (final String rawPart : LEGACY_ENVIRONMENT_SEPARATOR.split(environment)) {
			final String part = rawPart.trim();

			if (part.isEmpty()) {
				continue;
			}

			final int separator = part.indexOf('=');

			// no name, e.g. "=foo", this used to fail, there is nothing sensible to migrate
			if (separator == 0) {
				continue;
			}

			if (separator < 0) {
				if (!result.isEmpty()) {
					final EnvironmentVariable previous = result.getLast();
					previous.value = previous.value.isEmpty() ? part : previous.value + " " + part;
				}
				continue;
			}

			result.add(new EnvironmentVariable(
				part.substring(0, separator),
				unquote(part.substring(separator + 1))
			));
		}

		return result;
	}

	private static String unquote(final String value) {
		if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
			return value.substring(1, value.length() - 1);
		}

		return value;
	}
}
