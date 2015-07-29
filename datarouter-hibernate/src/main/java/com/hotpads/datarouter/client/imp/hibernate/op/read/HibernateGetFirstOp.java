package com.hotpads.datarouter.client.imp.hibernate.op.read;

import org.hibernate.Criteria;
import org.hibernate.Session;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class HibernateGetFirstOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<D>{
		
	private final HibernateReaderNode<PK,D,F> node;
	private final Config config;
	
	public HibernateGetFirstOp(HibernateReaderNode<PK,D,F> node, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.config = config;
	}
	
	@Override
	public D runOnce(){
		Session session = getSession(node.getClientId().getName());
		Criteria criteria = node.getCriteriaForConfig(config, session);
		criteria.setMaxResults(1);
		D result = (D)criteria.uniqueResult();
		return result;
	}
	
}
