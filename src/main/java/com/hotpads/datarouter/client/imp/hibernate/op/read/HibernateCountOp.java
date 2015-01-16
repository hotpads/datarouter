package com.hotpads.datarouter.client.imp.hibernate.op.read;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.CollectionTool;

public class HibernateCountOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<Long>{
		
	private HibernateReaderNode<PK,D,F> node;
	private String opName;
	private Lookup<PK> lookup;
	private Config config;
	
	public HibernateCountOp(HibernateReaderNode<PK,D,F> node, String opName, Lookup<PK> lookup, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.lookup = lookup;
		this.config = config;
	}
	
	@Override
	public Long runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		Session session = getSession(node.getClientName());
		Criteria criteria = node.getCriteriaForConfig(config, session);
		criteria.setProjection(Projections.rowCount());

		for(Field<?> field : CollectionTool.nullSafe(lookup.getFields())){
			criteria.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
		}
		
		Number n = (Number)criteria.uniqueResult();						
		return n.longValue();
	}
	
}
