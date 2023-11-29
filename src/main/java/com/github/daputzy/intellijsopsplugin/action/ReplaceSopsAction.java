package com.github.daputzy.intellijsopsplugin.action;

import com.github.daputzy.intellijsopsplugin.handler.ReplaceActionHandler;

public class ReplaceSopsAction extends SopsAction {

	public ReplaceSopsAction() {
		super(ReplaceActionHandler::new);
	}
}
