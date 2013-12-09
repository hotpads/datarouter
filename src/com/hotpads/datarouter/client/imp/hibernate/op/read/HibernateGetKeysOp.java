package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.BatchTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class HibernateGetKeysOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseParallelHibernateTxnApp<List<PK>>{
		
	private HibernateReaderNode<PK,D,F> node;
	private String opName;
	private Collection<PK> keys;
	private Config config;
	
	public HibernateGetKeysOp(HibernateReaderNode<PK,D,F> node, String opName, Collection<PK> keys, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.keys = keys;
		this.config = config;
	}
	
	@Override
	public List<PK> runOnce(){
		if(node.getFieldInfo().getFieldAware()){ throw new NotImplementedException(); }
		ClientType clientType = node.getFieldInfo().getFieldAware() ? ClientType.jdbc : ClientType.hibernate;
		DRCounters.incSuffixClientNode(clientType, opName, node.getClientName(), node.getName());
		try{
			TraceContext.startSpan(node.getName()+" "+opName);
			Session session = getSession(node.getClientName());
			int batchSize = HibernateNode.DEFAULT_ITERATE_BATCH_SIZE;
			if(config!=null && config.getIterateBatchSize()!=null){
				batchSize = config.getIterateBatchSize();
			}
			List<? extends Key<PK>> sortedKeys = ListTool.createArrayList(keys);
			Collections.sort(sortedKeys);
			int numBatches = BatchTool.getNumBatches(sortedKeys.size(), batchSize);
			List<PK> all = ListTool.createArrayList(keys.size());
			for(int batchNum=0; batchNum < numBatches; ++batchNum){
				List<? extends Key<PK>> keyBatch = BatchTool.getBatch(sortedKeys, batchSize, batchNum);
				Criteria criteria = node.getCriteriaForConfig(config, session);
				//projection list
				ProjectionList projectionList = Projections.projectionList();
				int numFields = 0;
				for(Field<?> field : node.getFieldInfo().getPrefixedPrimaryKeyFields()){
					projectionList.add(Projections.property(field.getPrefixedName()));
					++numFields;
				}
				criteria.setProjection(projectionList);
				//where clause
				Disjunction orSeparatedIds = Restrictions.disjunction();
				for(Key<PK> key : CollectionTool.nullSafe(keyBatch)){
					Conjunction possiblyCompoundId = Restrictions.conjunction();
					List<Field<?>> fields = FieldTool.prependPrefixes(node.getFieldInfo().getKeyFieldName(), key.getFields());
					for(Field<?> field : fields){
						possiblyCompoundId.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
					}
					orSeparatedIds.add(possiblyCompoundId);
				}
				criteria.add(orSeparatedIds);
				List<Object[]> rows = criteria.list();
				for(Object[] row : IterableTool.nullSafe(rows)){
					all.add(FieldSetTool.fieldSetFromHibernateResultUsingReflection(
							node.getFieldInfo().getPrimaryKeyClass(), node.getFieldInfo().getPrimaryKeyFields(), row));
				}
			}
			return all;
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
