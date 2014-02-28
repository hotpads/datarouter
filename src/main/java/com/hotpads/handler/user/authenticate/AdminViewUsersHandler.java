package com.hotpads.handler.user.authenticate;

import java.util.List;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.util.core.IterableTool;

public class AdminViewUsersHandler extends BaseHandler {

	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private DatarouterUserNodes userNodes;

	@Override
	protected Mav handleDefault() {
		Mav mav = new Mav("/jsp/admin/viewUsers.jsp");
		List<DatarouterUser> userList = IterableTool.createArrayListFromIterable(userNodes.getUserNode().getAll(null));
		mav.put("userList", userList);
		mav.put("authenticationConfig", authenticationConfig);
		return mav;
	}

}
