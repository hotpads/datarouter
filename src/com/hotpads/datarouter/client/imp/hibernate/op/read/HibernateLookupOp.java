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
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.BatchTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class HibernateLookupOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseParallelHibernateTxnApp<List<D>>{
		
	private HibernateReaderNode<PK,D,F> node;
	private String opName;
	private Collection<? extends Lookup<PK>> lookups;
	private boolean wildcardLastField;
	private Config config;
	
	public HibernateLookupOp(HibernateReaderNode<PK,D,F> node, String opName, 
			Collection<? extends Lookup<PK>> lookups, boolean wildcardLastField, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.lookups = lookups;
		this.wildcardLastField = wildcardLastField;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		ClientType clientType = node.getFieldInfo().getFieldAware() ? ClientType.jdbc : ClientType.hibernate;
		DRCounters.incSuffixClientNode(clientType, opName, node.getClientName(), node.getName());
		try{
			TraceContext.startSpan(node.getName()+" "+opName);
			Session session = getSession(node.getClientName());
			//TODO undefined behavior on trailing nulls
			if(node.getFieldInfo().getFieldAware()){
				String sql = SqlBuilder.getWithPrefixes(config, node.getTableName(), node.getFieldInfo().getFields(), lookups, 
						wildcardLastField, node.getFieldInfo().getPrimaryKeyFields());
				List<D> result = JdbcTool.selectDatabeans(session, node.getFieldInfo(), sql);
				return result;
			}else{
				Criteria criteria = node.getCriteriaForConfig(config, session);
				Disjunction or = Restrictions.disjunction();
				for(Lookup<PK> lookup : lookups){
					Conjunction prefixConjunction = node.getPrefixConjunction(false, lookup, wildcardLastField);
					if(prefixConjunction==null){
						throw new IllegalArgumentException("Lookup with all null fields would return entire " +
								"table.  Please use getAll() instead.");
					}
					or.add(prefixConjunction);
				}
				criteria.add(or);
				List<D> result = criteria.list();
				Collections.sort(result);//TODO, make sure the datastore scans in order so we don't need to sort here
				return result;
			}
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
