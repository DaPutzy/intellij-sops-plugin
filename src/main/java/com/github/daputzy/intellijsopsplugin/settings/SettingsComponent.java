package com.github.daputzy.intellijsopsplugin.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import javax.swing.*;

public class SettingsComponent {

	private final JPanel main;
	private final JBTextField sopsExecutableTextField = new JBTextField();

	public SettingsComponent() {
		main = FormBuilder.createFormBuilder()
			.addLabeledComponent(new JBLabel("Sops executable: "), sopsExecutableTextField, 1, false)
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
}
