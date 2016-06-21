package com.hotpads.handler;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.handler.mav.Mav;

public class ThresholdHandler extends BaseHandler {

	public static final String
	JSP_ThresholdSettings = "/jsp/admin/datarouter/ThresholdSettings.jsp";



	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterClients datarouterClients;



	@Override
	@Handler
	protected Mav handleDefault(){
		Mav mav = new Mav(JSP_ThresholdSettings);
		mav.put("serverName", datarouter.getServerName());
		mav.put("administratorEmail", datarouter.getAdministratorEmail());
		mav.put("routers", datarouter.getRouters());
		mav.put("lazyClientProviderByName", datarouterClients.getLazyClientProviderByName());
		mav.put("uninitializedClientNames", datarouterClients.getClientNamesByInitialized().get(false));
		return mav;
	}


	public static class ThresholdTests{

		@Inject
		private Datarouter datarouter;

		@Test
		public void testthreshold(){
			int cnt =0;
			System.out.println(datarouter.getRouters());
			for(Router router:datarouter.getRouters()){
				cnt++;
				System.out.println(cnt+" router "+router);
			}

		}
	}


}
