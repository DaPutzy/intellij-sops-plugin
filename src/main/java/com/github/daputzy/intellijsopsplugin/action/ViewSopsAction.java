package com.github.daputzy.intellijsopsplugin.action;

import com.github.daputzy.intellijsopsplugin.handler.ViewActionHandler;

public class ViewSopsAction extends SopsAction {

	public ViewSopsAction() {
		super(ViewActionHandler::new);
	}
}
