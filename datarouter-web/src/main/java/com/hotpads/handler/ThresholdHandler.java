package com.hotpads.handler;

import java.util.List;

import javax.inject.Inject;

import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.util.node.NodeWrapper;

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
		System.out.println("dkjshfkjdhskjfhdkjshfdkjs****************");
		Mav mav = new Mav(JSP_ThresholdSettings);
		mav.put("serverName", datarouter.getServerName());
		mav.put("administratorEmail", datarouter.getAdministratorEmail());
		mav.put("routers", datarouter.getRouters());
		mav.put("lazyClientProviderByName", datarouterClients.getLazyClientProviderByName());
		mav.put("uninitializedClientNames", datarouterClients.getClientNamesByInitialized().get(false));
		return mav;
	}

	@Handler
	Mav setThreshold(){
		System.out.println("*************************lala****************");
		Mav mav = new Mav(JSP_ThresholdSettings);


		for(Router router : datarouter.getRouters()){

			List<NodeWrapper> nodeWrappers = NodeWrapper.getNodeWrappers(router);
			System.out.println(router+"-"+nodeWrappers.size());
			for(NodeWrapper node : nodeWrappers){
				System.out.println("node"+node.getNode().getName());
			}
			mav.put("nodeWrappers", nodeWrappers);
		}
		mav.put("routers", datarouter.getRouters());

	//	System.out.println("routers "+datarouter.getRouters());
		return mav;
	}

}
