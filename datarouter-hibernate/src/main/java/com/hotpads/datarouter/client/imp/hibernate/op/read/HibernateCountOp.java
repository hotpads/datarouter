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
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class HibernateCountOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<Long>{
		
	private final HibernateReaderNode<PK,D,F> node;
	private final Lookup<PK> lookup;
	private final Config config;
	
	public HibernateCountOp(HibernateReaderNode<PK,D,F> node, Lookup<PK> lookup, Config config) {
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.lookup = lookup;
		this.config = config;
	}
	
	@Override
	public Long runOnce(){
		Session session = getSession(node.getClientId().getName());
		Criteria criteria = node.getCriteriaForConfig(config, session);
		criteria.setProjection(Projections.rowCount());

		for(Field<?> field : DrCollectionTool.nullSafe(lookup.getFields())){
			criteria.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
		}
		
		Number number = (Number)criteria.uniqueResult();
		return number.longValue();
	}
	
}
