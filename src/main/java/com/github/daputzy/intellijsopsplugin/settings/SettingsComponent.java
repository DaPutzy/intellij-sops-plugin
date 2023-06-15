package com.github.daputzy.intellijsopsplugin.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import javax.swing.*;

public class SettingsComponent {

	private final JPanel main;
	private final JBTextField sopsExecutableTextField = new JBTextField();
	private final JBCheckBox sopsFilesReadOnlyCheckBox = new JBCheckBox();

	public SettingsComponent() {
		main = FormBuilder.createFormBuilder()
			.addLabeledComponent(new JBLabel("Sops executable: "), sopsExecutableTextField, 1, false)
			.addLabeledComponent(new JBLabel("Set Sops files read only"), sopsFilesReadOnlyCheckBox, 2, false)
			.addComponentFillVertically(new JPanel(), 0)
			.getPanel();
	}

	public JPanel getPanel() {
		return main;
	}

	public JComponent getPreferredFocusedComponent() {
		return sopsExecutableTextField;
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
