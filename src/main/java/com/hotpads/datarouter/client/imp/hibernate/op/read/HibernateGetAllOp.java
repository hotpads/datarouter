package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class HibernateGetAllOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<List<D>>{
		
	private final HibernateReaderNode<PK,D,F> node;
	private final Config config;
	
	public HibernateGetAllOp(HibernateReaderNode<PK,D,F> node, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		Session session = getSession(node.getClientName());
		Criteria criteria = node.getCriteriaForConfig(config, session);
		List<D> databeans = criteria.list();
		return databeans;//assume they come back sorted due to innodb
	}
	
}
