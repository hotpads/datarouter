package com.hotpads.handler.user;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserFielder;
import com.hotpads.handler.user.authenticate.api.ApiRequest;
import com.hotpads.handler.user.authenticate.api.ApiRequest.ApiRequestFielder;
import com.hotpads.handler.user.authenticate.api.ApiRequestKey;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSession.DatarouterSessionFielder;
import com.hotpads.handler.user.session.DatarouterSessionKey;

@Singleton
public class DatarouterUserRouter extends BaseRouter implements DatarouterUserNodes{

	public static class DatarouterUserRouterParams{
		private final String configFileLocation;
		private final ClientId clientId;

		public DatarouterUserRouterParams(String configFileLocation, ClientId clientId){
			this.configFileLocation = configFileLocation;
			this.clientId = clientId;
		}
	}

	private static final String NAME = "datarouterUser";

	private final IndexedSortedMapStorageNode<DatarouterUserKey,DatarouterUser> user;
	private final MapStorageNode<DatarouterSessionKey,DatarouterSession> session;
	private final IndexedSortedMapStorageNode<ApiRequestKey,ApiRequest> apiRequest;

	@Inject
	public DatarouterUserRouter(Datarouter datarouter, NodeFactory nodeFactory, DatarouterSettings datarouterSettings,
			DatarouterUserRouterParams params){
		super(datarouter, params.configFileLocation, NAME, nodeFactory, datarouterSettings);
		user = createAndRegister(params.clientId, DatarouterUser::new, DatarouterUserFielder::new);
		session = createAndRegister(params.clientId, DatarouterSession::new, DatarouterSessionFielder::new);
		apiRequest = createAndRegister(params.clientId, ApiRequest::new, ApiRequestFielder::new);
	}

	@Override
	public IndexedSortedMapStorageNode<DatarouterUserKey,DatarouterUser> getUserNode(){
		return user;
	}

	@Override
	public MapStorageNode<DatarouterSessionKey,DatarouterSession> getSessionNode(){
		return session;
	}

	@Override
	public IndexedSortedMapStorageNode<ApiRequestKey,ApiRequest> getApiRequestNode(){
		return apiRequest;
	}

}
