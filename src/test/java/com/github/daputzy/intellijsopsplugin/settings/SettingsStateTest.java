package com.github.daputzy.intellijsopsplugin.settings;

import java.util.List;

import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("deprecation")
public class SettingsStateTest {

	@Test
	public void parseLegacyEnvironmentSplitsOnWhitespace() {
		assertEquals(
			List.of(
				new EnvironmentVariable("SOPS_AGE_KEY_FILE", "/Users/bockwurst/keys.txt"),
				new EnvironmentVariable("VAULT_ADDR", "http://127.0.0.1:8200"),
				new EnvironmentVariable("VAULT_TOKEN", "toor")
			),
			SettingsState.parseLegacyEnvironment("SOPS_AGE_KEY_FILE=/Users/bockwurst/keys.txt  VAULT_ADDR=http://127.0.0.1:8200\tVAULT_TOKEN=toor ")
		);
	}

	@Test
	public void parseLegacyEnvironmentKeepsEqualsSignsInValue() {
		assertEquals(
			List.of(new EnvironmentVariable("FOO", "a=b=c")),
			SettingsState.parseLegacyEnvironment("FOO=a=b=c")
		);
	}

	@Test
	public void parseLegacyEnvironmentRemovesSurroundingQuotes() {
		assertEquals(
			List.of(
				new EnvironmentVariable("SOPS_AGE_KEY_CMD", "keyring get sops-age default"),
				new EnvironmentVariable("SOPS_AGE_KEY_FILE", "/Users/bock wurst/keys.txt"),
				new EnvironmentVariable("EMPTY", "")
			),
			SettingsState.parseLegacyEnvironment("SOPS_AGE_KEY_CMD=\"keyring get sops-age default\" SOPS_AGE_KEY_FILE=\"/Users/bock wurst/keys.txt\" EMPTY=")
		);
	}

	@Test
	public void parseLegacyEnvironmentAppendsPartsWithoutEqualsToPreviousValue() {
		assertEquals(
			List.of(
				new EnvironmentVariable("SOPS_AGE_KEY_CMD", "keyring get sops-age default"),
				new EnvironmentVariable("TERM", "xterm")
			),
			SettingsState.parseLegacyEnvironment("orphan SOPS_AGE_KEY_CMD=keyring get sops-age default TERM=xterm =broken")
		);
	}

	@Test
	public void parseLegacyEnvironmentHandlesBlankInput() {
		assertTrue(SettingsState.parseLegacyEnvironment("").isEmpty());
		assertTrue(SettingsState.parseLegacyEnvironment("   ").isEmpty());
	}

	@Test
	public void loadStateMigratesLegacyEnvironment() throws Exception {
		final SettingsState settings = load("""
			<component name="com.github.daputzy.intellijsopsplugin.settings.SettingsState">
			  <option name="sopsEnvironment" value="SOPS_AGE_KEY_FILE=/Users/bockwurst/keys.txt TERM=xterm" />
			  <option name="sopsExecutable" value="/opt/sops" />
			  <option name="sopsFilesReadOnly" value="true" />
			</component>
			""");

		assertEquals(
			List.of(
				new EnvironmentVariable("SOPS_AGE_KEY_FILE", "/Users/bockwurst/keys.txt"),
				new EnvironmentVariable("TERM", "xterm")
			),
			settings.sopsEnvironmentVariables
		);
		assertEquals("", settings.sopsEnvironment);
		assertEquals("/opt/sops", settings.sopsExecutable);
		assertTrue(settings.sopsFilesReadOnly);
	}

	@Test
	public void loadStateDoesNotOverwriteExistingVariables() throws Exception {
		final SettingsState original = new SettingsState();
		original.sopsEnvironmentVariables.add(new EnvironmentVariable("NEW", "value"));
		original.sopsEnvironment = "OLD=value";

		final SettingsState settings = new SettingsState();
		settings.loadState(XmlSerializer.deserialize(XmlSerializer.serialize(original), SettingsState.class));

		assertEquals(List.of(new EnvironmentVariable("NEW", "value")), settings.sopsEnvironmentVariables);
		assertEquals("", settings.sopsEnvironment);
	}

	@Test
	public void serializedStateRoundTrips() throws Exception {
		final SettingsState original = new SettingsState();
		original.sopsEnvironmentVariables.add(new EnvironmentVariable("SOPS_AGE_KEY_CMD", "keyring get \"sops age\" default"));
		original.sopsEnvironmentVariables.add(new EnvironmentVariable("EMPTY", ""));

		final Element element = XmlSerializer.serialize(original);

		final SettingsState settings = load(JDOMUtil.write(element));

		assertEquals(original.sopsEnvironmentVariables, settings.sopsEnvironmentVariables);
	}

	private static SettingsState load(final String xml) throws Exception {
		final SettingsState settings = new SettingsState();
		settings.loadState(XmlSerializer.deserialize(JDOMUtil.load(xml), SettingsState.class));
		return settings;
	}
}
