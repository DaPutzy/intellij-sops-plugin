package com.github.daputzy.intellijsopsplugin.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import javax.swing.*;

public class SettingsComponent {

	private final JPanel main;
	private final JBTextField sopsEnvironmentTextField = new JBTextField();
	private final JBTextField sopsExecutableTextField = new JBTextField();
	private final JBCheckBox sopsFilesReadOnlyCheckBox = new JBCheckBox();

	public SettingsComponent() {
		main = FormBuilder.createFormBuilder()
			.addLabeledComponent(new JBLabel("Sops environment"), sopsEnvironmentTextField, 1, false)
			.addLabeledComponent(new JBLabel("Sops executable: "), sopsExecutableTextField, 2, false)
			.addLabeledComponent(new JBLabel("Set Sops files read only"), sopsFilesReadOnlyCheckBox, 3, false)
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

	public boolean getSopsFilesReadOnly() {
		return sopsFilesReadOnlyCheckBox.isSelected();
	}

	public void setSopsFilesReadOnly(final boolean selected) {
		sopsFilesReadOnlyCheckBox.setSelected(selected);
	}
}
