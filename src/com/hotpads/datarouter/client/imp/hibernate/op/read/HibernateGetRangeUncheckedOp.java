package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.util.CriteriaTool;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.Range;

public class HibernateGetRangeUncheckedOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseParallelHibernateTxnApp<List<? extends FieldSet<?>>>{
		
	private HibernateReaderNode<PK,D,F> node;
	private String opName;
	private Range<PK> range;
	private boolean keysOnly;
	private Config config;
	
	public HibernateGetRangeUncheckedOp(HibernateReaderNode<PK,D,F> node, String opName, Range<PK> range, 
			boolean keysOnly, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.range = range;
		this.keysOnly = keysOnly;
		this.config = config;
	}
	
	@Override
	public List<? extends FieldSet<?>> runOnce(){
		ClientType clientType = node.getFieldInfo().getFieldAware() ? ClientType.jdbc : ClientType.hibernate;
		DRCounters.incSuffixClientNode(clientType, opName, node.getClientName(), node.getName());
		try{
			TraceContext.startSpan(node.getName()+" "+opName);
			Session session = getSession(node.getClientName());
			if(node.getFieldInfo().getFieldAware()){
				List<Field<?>> fieldsToSelect = keysOnly ? node.getFieldInfo().getPrimaryKeyFields() 
						: node.getFieldInfo().getFields();
				String sql = SqlBuilder.getInRange(config, node.getTableName(), fieldsToSelect, range, 
						node.getFieldInfo().getPrimaryKeyFields());
				List<? extends FieldSet<?>> result;
				if(keysOnly){
					result = JdbcTool.selectPrimaryKeys(session, node.getFieldInfo(), sql);
				}else{
					result = JdbcTool.selectDatabeans(session, node.getFieldInfo(), sql);
				}
				return result;
			}else{
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
					List<PK> result = ListTool.createArrayList(CollectionTool.size(rows));
					for(Object row : IterableTool.nullSafe(rows)){
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
					return result;
				}else{
					List<? extends FieldSet<?>> result = (List<? extends FieldSet<?>>)criteria.list();
					return result;
				}
			}
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
