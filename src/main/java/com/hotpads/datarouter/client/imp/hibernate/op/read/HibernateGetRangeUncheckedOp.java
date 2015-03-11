package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.client.imp.hibernate.util.CriteriaTool;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;

//TODO this is the jdbc implementation, so extend or abstract it
public class HibernateGetRangeUncheckedOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<List<? extends FieldSet<?>>>{
		
	private HibernateReaderNode<PK,D,F> node;
	private String opName;
	private Range<PK> range;
	private boolean keysOnly;
	private Config config;
	
	public HibernateGetRangeUncheckedOp(HibernateReaderNode<PK,D,F> node, String opName, Range<PK> range, 
			boolean keysOnly, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.range = range;
		this.keysOnly = keysOnly;
		this.config = config;
	}
	
	@Override
	public List<? extends FieldSet<?>> runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		Session session = getSession(node.getClientName());
		Criteria criteria = node.getCriteriaForConfig(config, session);
		if(keysOnly){
			ProjectionList projectionList = Projections.projectionList();
			for(Field<?> field : node.getFieldInfo().getPrefixedPrimaryKeyFields()){
				projectionList.add(Projections.property(field.getPrefixedName()));
			}
			criteria.setProjection(projectionList);
		}
		node.addPrimaryKeyOrderToCriteria(criteria);
		CriteriaTool.addRangesToCriteria(criteria, range, node.getFieldInfo());
		if(keysOnly){
			List<Object[]> rows = criteria.list();
			List<PK> result = DrListTool.createArrayList(DrCollectionTool.size(rows));
			for(Object row : DrIterableTool.nullSafe(rows)){
				// hibernate will return a plain Object if it's a single col PK
				Object[] rowCells;
				if(row instanceof Object[]){
					rowCells = (Object[])row;
				}else{
					rowCells = new Object[]{row};
				}
				result.add(FieldSetTool.fieldSetFromHibernateResultUsingReflection(
						node.getFieldInfo().getPrimaryKeyClass(), node.getFieldInfo().getPrimaryKeyFields(), 
						rowCells));
			}
			DRCounters.incSuffixClientNode(node.getClient().getType(), opName+" rows", node.getClientName(), node.getName(), 
					DrCollectionTool.size(result));
			return result;
		}
		List<? extends FieldSet<?>> result = criteria.list();
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName+" rows", node.getClientName(), node.getName(), 
				DrCollectionTool.size(result));
		return result;
	}
	
}
