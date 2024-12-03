package com.github.daputzy.intellijsopsplugin.settings;

import com.intellij.execution.wsl.WSLDistribution;
import com.intellij.execution.wsl.WSLUtil;
import com.intellij.execution.wsl.WslDistributionManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Stream;

public class SettingsComponent {

	private final JPanel main;
	private final JBTextField sopsEnvironmentTextField = new JBTextField();
	private final JBTextField sopsExecutableTextField = new JBTextField();
	private final JBCheckBox sopsUseWSL = new JBCheckBox();
	private final ComboBox<String> sopsWslDistributionName;
	private final JBCheckBox sopsFilesReadOnlyCheckBox = new JBCheckBox();

	public SettingsComponent() {
		final JLabel environmentHint = new JLabel(AllIcons.Actions.IntentionBulbGrey);
		environmentHint.setToolTipText("<html><h2>Here you can set custom environment variables</h2><h3>Make sure you do NOT escape the values</h3><h4>Example:</h4><pre>SOPS_AGE_KEY_FILE=/Users/bockwurst/Documents/sops/age/keys.txt VAULT_ADDR=http://127.0.0.1:8200 VAULT_TOKEN=toor</pre></html>");

		final JPanel environmentLabel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		environmentLabel.add(new JLabel("Sops environment"));
		environmentLabel.add(environmentHint);

		final JLabel executableHint = new JLabel(AllIcons.Actions.IntentionBulbGrey);
		executableHint.setToolTipText("<html><h2>Here you can set a custom executable path</h2><h4>Default:</h4><pre>sops</pre><h4>Example:</h4><pre>/Users/bockwurst/bin/sops-wrapper.sh</pre></html>");

		final JPanel executableLabel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		executableLabel.add(new JLabel("Sops executable"));
		executableLabel.add(executableHint);

		final JPanel useWSL = new JPanel(new FlowLayout(FlowLayout.LEFT));
		useWSL.add(new JLabel("Use WSL (if available)"));
		useWSL.add(sopsUseWSL);


		if (!WSLUtil.isSystemCompatible()) {
			sopsUseWSL.setEnabled(false);
			sopsUseWSL.setSelected(false);

			sopsWslDistributionName = new ComboBox<>();
			sopsWslDistributionName.setEnabled(false);
		} else {
			Stream<String> distros = WslDistributionManager.getInstance().getInstalledDistributions().stream().map(WSLDistribution::getPresentableName);
			sopsWslDistributionName = new ComboBox<>(distros.toArray(String[]::new));
			sopsWslDistributionName.setSelectedIndex(0);
		}
		final JPanel wslDistributionName = new JPanel(new FlowLayout(FlowLayout.LEFT));
		wslDistributionName.add(new JLabel("Wsl distribution"));
		wslDistributionName.add(sopsWslDistributionName);

		final JLabel readonlyHint = new JLabel(AllIcons.Actions.IntentionBulbGrey);
		readonlyHint.setToolTipText("<html><h2>Here you can specify if encrypted sops files should be read only</h2><h3>i.e. you can only edit them via the plugin</h3><h4>Default:</h4><pre>false</pre></html>");

		final JPanel readonlyLabel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		readonlyLabel.add(new JLabel("Sops files read only?"));
		readonlyLabel.add(readonlyHint);

		main = FormBuilder.createFormBuilder()
			.addLabeledComponent(environmentLabel, sopsEnvironmentTextField, 1, false)
			.addLabeledComponent(executableLabel, sopsExecutableTextField, 2, false)
			.addLabeledComponent(useWSL, sopsUseWSL, 3, false)
			.addLabeledComponent(wslDistributionName, sopsWslDistributionName, 4, false)
			.addLabeledComponent(readonlyLabel, sopsFilesReadOnlyCheckBox, 5, false)
			.addComponentFillVertically(new JPanel(), 0)
			.getPanel();
	}

	public JPanel getPanel() {
		return main;
	}

	public JComponent getPreferredFocusedComponent() {
		return sopsEnvironmentTextField;
	}

	public String getSopsEnvironment() {
		return sopsEnvironmentTextField.getText();
	}

	public void setSopsEnvironment(final String newText) {
		sopsEnvironmentTextField.setText(newText);
	}

	public String getSopsExecutable() {
		return sopsExecutableTextField.getText();
	}

	public void setSopsExecutable(final String newText) {
		sopsExecutableTextField.setText(newText);
	}

	public boolean getSopsUseWSL() {
		return sopsUseWSL.isSelected();
	}

	public void setSopsUseWSL(final boolean selected) {
		sopsUseWSL.setSelected(selected);
	}

	public String getSopsWslDistributionName() {
		return sopsWslDistributionName.getItem();
	}

	public void setSopsWslDistributionName(final String newValue) {
		if (newValue != null) {
			sopsWslDistributionName.setItem(newValue);
		}
	}

	public boolean getSopsFilesReadOnly() {
		return sopsFilesReadOnlyCheckBox.isSelected();
	}

	public void setSopsFilesReadOnly(final boolean selected) {
		sopsFilesReadOnlyCheckBox.setSelected(selected);
	}
}
