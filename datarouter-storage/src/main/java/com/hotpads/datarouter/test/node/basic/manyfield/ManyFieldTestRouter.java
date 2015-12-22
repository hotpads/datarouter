package com.hotpads.datarouter.test.node.basic.manyfield;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;

@Singleton
public class ManyFieldTestRouter extends BaseRouter{

	private final List<ClientId> clientIds;

	@Inject
	public ManyFieldTestRouter(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId,
			boolean useFielder){
		super(datarouter, DrTestConstants.CONFIG_PATH, ManyFieldTestRouter.class.getSimpleName());

		this.clientIds = new ArrayList<>();
		this.clientIds.add(clientId);

		Class<ManyFieldTypeBeanFielder> fielderClass = useFielder ? ManyFieldTypeBeanFielder.class : null;
		this.manyFieldTypeBeanNode = register(nodeFactory.create(clientId, ManyFieldBean.class, fielderClass,
				new Random().nextInt(), this, false));

	}

	/********************************** config **********************************/

	@Override
	public List<ClientId> getClientIds(){
		return clientIds;
	}

	/********************************** nodes **********************************/

	protected MapStorageNode<ManyFieldBeanKey,ManyFieldBean> manyFieldTypeBeanNode;


	/*************************** get/set ***********************************/

	public MapStorageNode<ManyFieldBeanKey,ManyFieldBean> manyFieldTypeBean() {
		return manyFieldTypeBeanNode;
	}

}