package com.github.daputzy.intellijsopsplugin.settings;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.intellij.icons.AllIcons;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.TableUtil;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListTableModel;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SettingsComponent {

	private final JPanel main;
	private final ListTableModel<EnvironmentVariable> sopsEnvironmentModel = new ListTableModel<>(
		new EnvironmentVariableColumn("Name") {
			@Override
			public @Nullable String valueOf(final EnvironmentVariable variable) {
				return variable.name;
			}

			@Override
			public void setValue(final EnvironmentVariable variable, final String value) {
				variable.name = Objects.requireNonNullElse(value, "");
			}
		},
		new EnvironmentVariableColumn("Value") {
			@Override
			public @Nullable String valueOf(final EnvironmentVariable variable) {
				return variable.value;
			}

			@Override
			public void setValue(final EnvironmentVariable variable, final String value) {
				variable.value = Objects.requireNonNullElse(value, "");
			}
		}
	);
	private final TableView<EnvironmentVariable> sopsEnvironmentTable = new TableView<>(sopsEnvironmentModel);
	private final JBTextField sopsExecutableTextField = new JBTextField();
	private final JBCheckBox sopsFilesReadOnlyCheckBox = new JBCheckBox();

	public SettingsComponent() {
		final JLabel environmentHint = new JLabel(AllIcons.Actions.IntentionBulbGrey);
		environmentHint.setToolTipText("<html><h2>Here you can set custom environment variables for sops</h2><h3>Values are passed to sops as they are, so do NOT quote or escape them</h3><h4>Example:</h4><pre>SOPS_AGE_KEY_FILE   /Users/bockwurst/Documents/sops/age/keys.txt<br>SOPS_AGE_KEY_CMD    security find-generic-password -s sops-age -a default -w<br>VAULT_ADDR          http://127.0.0.1:8200</pre></html>");

		final JPanel environmentLabel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		environmentLabel.add(new JLabel("Sops environment"));
		environmentLabel.add(environmentHint);

		sopsEnvironmentTable.getEmptyText().setText("No environment variables");
		sopsEnvironmentTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		sopsEnvironmentTable.setShowGrid(false);

		final JPanel environmentPanel = ToolbarDecorator.createDecorator(sopsEnvironmentTable)
			.setAddAction(button -> addEnvironmentVariable())
			.setRemoveAction(button -> TableUtil.removeSelectedItems(sopsEnvironmentTable))
			.disableUpDownActions()
			.setPreferredSize(JBUI.size(500, 150))
			.createPanel();

		final JLabel executableHint = new JLabel(AllIcons.Actions.IntentionBulbGrey);
		executableHint.setToolTipText("<html><h2>Here you can set a custom executable path</h2><h4>Default:</h4><pre>sops</pre><h4>Example:</h4><pre>/Users/bockwurst/bin/sops-wrapper.sh</pre></html>");

		final JPanel executableLabel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		executableLabel.add(new JLabel("Sops executable"));
		executableLabel.add(executableHint);

		final JLabel readonlyHint = new JLabel(AllIcons.Actions.IntentionBulbGrey);
		readonlyHint.setToolTipText("<html><h2>Here you can specify if encrypted sops files should be read only</h2><h3>i.e. you can only edit them via the plugin</h3><h4>Default:</h4><pre>false</pre></html>");

		final JPanel readonlyLabel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		readonlyLabel.add(new JLabel("Sops files read only?"));
		readonlyLabel.add(readonlyHint);

		main = FormBuilder.createFormBuilder()
			.addLabeledComponent(environmentLabel, environmentPanel, 1, true)
			.addLabeledComponent(executableLabel, sopsExecutableTextField, 2, false)
			.addLabeledComponent(readonlyLabel, sopsFilesReadOnlyCheckBox, 3, false)
			.addComponentFillVertically(new JPanel(), 0)
			.getPanel();
	}

	public JPanel getPanel() {
		return main;
	}

	public JComponent getPreferredFocusedComponent() {
		return sopsEnvironmentTable;
	}

	/**
	 * @return copy of the configured environment variables, entries without a name are skipped
	 */
	public List<EnvironmentVariable> getSopsEnvironmentVariables() {
		return sopsEnvironmentModel.getItems().stream()
			.map(variable -> new EnvironmentVariable(variable.name.trim(), variable.value))
			.filter(variable -> !variable.name.isEmpty())
			.toList();
	}

	public void setSopsEnvironmentVariables(final List<EnvironmentVariable> variables) {
		TableUtil.stopEditing(sopsEnvironmentTable);

		final List<EnvironmentVariable> copies = new ArrayList<>();
		variables.forEach(variable -> copies.add(variable.copy()));

		sopsEnvironmentModel.setItems(copies);
	}

	/**
	 * commits a value that is currently being edited in the environment table
	 */
	public void stopEditing() {
		TableUtil.stopEditing(sopsEnvironmentTable);
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

	private void addEnvironmentVariable() {
		TableUtil.stopEditing(sopsEnvironmentTable);

		sopsEnvironmentModel.addRow(new EnvironmentVariable());

		final int row = sopsEnvironmentModel.getRowCount() - 1;
		TableUtil.selectRows(sopsEnvironmentTable, new int[]{row});
		TableUtil.scrollSelectionToVisible(sopsEnvironmentTable);
		TableUtil.editCellAt(sopsEnvironmentTable, row, 0);
	}

	private abstract static class EnvironmentVariableColumn extends ColumnInfo<EnvironmentVariable, String> {

		EnvironmentVariableColumn(@NotNull final String name) {
			super(name);
		}

		@Override
		public boolean isCellEditable(final EnvironmentVariable variable) {
			return true;
		}
	}
}
