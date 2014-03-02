package com.hotpads.datarouter.client.imp.hibernate.op.read;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;

public class HibernateGetFirstKeyOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<PK>{
		
	private HibernateReaderNode<PK,D,F> node;
	private String opName;
	private Config config;
	
	public HibernateGetFirstKeyOp(HibernateReaderNode<PK,D,F> node, String opName, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.config = config;
	}
	
	@Override
	public PK runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		Session session = getSession(node.getClientName());
		String entityName = node.getPackagedTableName();
		Criteria criteria = session.createCriteria(entityName);
		ProjectionList projectionList = Projections.projectionList();
		int numFields = 0;
		for(Field<?> field : node.getFieldInfo().getPrefixedPrimaryKeyFields()){
			projectionList.add(Projections.property(field.getPrefixedName()));
			++numFields;
		}
		criteria.setProjection(projectionList);
		node.addPrimaryKeyOrderToCriteria(criteria);
		criteria.setMaxResults(1);
		Object rows = criteria.uniqueResult();
		if(rows==null){ return null; }
		if(numFields==1){
			rows = new Object[]{rows};
		}
		PK pk = (PK)FieldSetTool.fieldSetFromHibernateResultUsingReflection(
				node.getFieldInfo().getPrimaryKeyClass(), node.getFieldInfo().getPrimaryKeyFields(), rows);
		return pk;
	}
	
}
