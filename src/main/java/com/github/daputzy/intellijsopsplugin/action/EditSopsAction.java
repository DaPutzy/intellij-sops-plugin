package com.github.daputzy.intellijsopsplugin.action;

import com.github.daputzy.intellijsopsplugin.handler.EditActionHandler;

public class EditSopsAction extends SopsAction {

	public EditSopsAction() {
		super(EditActionHandler::new);
	}
}
