package com.github.daputzy.intellijsopsplugin.settings;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * single environment variable passed to the sops process
 */
@Tag("variable")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString(exclude = "value")
public class EnvironmentVariable {

	@Attribute("name")
	public String name = "";

	@Attribute("value")
	public String value = "";

	public EnvironmentVariable copy() {
		return new EnvironmentVariable(name, value);
	}
}
