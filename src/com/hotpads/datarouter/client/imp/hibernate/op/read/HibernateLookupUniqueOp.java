package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.BatchTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class HibernateLookupUniqueOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseParallelHibernateTxnApp<List<D>>{
		
	private HibernateReaderNode<PK,D,F> node;
	private String opName;
	private Collection<? extends UniqueKey<PK>> uniqueKeys;
	private Config config;
	
	public HibernateLookupUniqueOp(HibernateReaderNode<PK,D,F> node, String opName, 
			Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.uniqueKeys = uniqueKeys;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
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
			List<? extends UniqueKey<PK>> sortedKeys = ListTool.createArrayList(uniqueKeys);
			Collections.sort(sortedKeys);
			int numBatches = BatchTool.getNumBatches(sortedKeys.size(), batchSize);
			List<D> all = ListTool.createArrayList(uniqueKeys.size());
			for(int batchNum=0; batchNum < numBatches; ++batchNum){
				List<? extends Key<PK>> keyBatch = BatchTool.getBatch(sortedKeys, batchSize, batchNum);
				List<D> batch;
				if(node.getFieldInfo().getFieldAware()){
					String sql = SqlBuilder.getMulti(config, node.getTableName(), node.getFieldInfo().getFields(), 
							uniqueKeys);
					List<D> result = JdbcTool.selectDatabeans(session, node.getFieldInfo(), sql);
					return result;
				}else{
					Criteria criteria = node.getCriteriaForConfig(config, session);
					Disjunction orSeparatedIds = Restrictions.disjunction();
					for(Key<PK> key : CollectionTool.nullSafe(keyBatch)){
						Conjunction possiblyCompoundId = Restrictions.conjunction();
						List<Field<?>> fields = key.getFields();
						for(Field<?> field : fields){
							possiblyCompoundId.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
						}
						orSeparatedIds.add(possiblyCompoundId);
					}
					criteria.add(orSeparatedIds);
					batch = criteria.list();
				}
				ListTool.nullSafeArrayAddAll(all, batch);
			}
			return all;	
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
