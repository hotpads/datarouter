package com.hotpads.datarouter.test.node.basic.manyfield;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;

@Singleton
public class ManyFieldTestRouter extends BaseRouter{

	@Inject
	public ManyFieldTestRouter(Datarouter datarouter, DatarouterSettings datarouterSettings, NodeFactory nodeFactory,
			ClientId clientId, boolean useFielder){
		super(datarouter, DrTestConstants.CONFIG_PATH, ManyFieldTestRouter.class.getSimpleName(), nodeFactory,
				datarouterSettings);

		Class<ManyFieldTypeBeanFielder> fielderClass = useFielder ? ManyFieldTypeBeanFielder.class : null;
		this.manyFieldTypeBeanNode = register(nodeFactory.create(clientId, ManyFieldBean.class, fielderClass,
				new Random().nextInt(), this, false));

	}

	/********************************** nodes **********************************/

	protected MapStorageNode<ManyFieldBeanKey,ManyFieldBean> manyFieldTypeBeanNode;


	/*************************** get/set ***********************************/

	public MapStorageNode<ManyFieldBeanKey,ManyFieldBean> manyFieldTypeBean() {
		return manyFieldTypeBeanNode;
	}

}