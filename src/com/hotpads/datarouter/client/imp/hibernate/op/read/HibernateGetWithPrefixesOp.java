package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
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

public class HibernateGetWithPrefixesOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<List<D>>{
		
	private HibernateReaderNode<PK,D,F> node;
	private String opName;
	private Collection<PK> prefixes;
	private boolean wildcardLastField;
	private Config config;
	
	public HibernateGetWithPrefixesOp(HibernateReaderNode<PK,D,F> node, String opName, 
			Collection<PK> prefixes, boolean wildcardLastField, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.prefixes = prefixes;
		this.wildcardLastField = wildcardLastField;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		if(CollectionTool.isEmpty(prefixes)){ return new LinkedList<D>(); }
		ClientType clientType = node.getFieldInfo().getFieldAware() ? ClientType.jdbc : ClientType.hibernate;
		DRCounters.incSuffixClientNode(clientType, opName, node.getClientName(), node.getName());
		try{
			TraceContext.startSpan(node.getName()+" "+opName);
			Session session = getSession(node.getClientName());
			if(node.getFieldInfo().getFieldAware()){
				String sql = SqlBuilder.getWithPrefixes(config, node.getTableName(), node.getFieldInfo().getFields(), prefixes, 
						wildcardLastField, node.getFieldInfo().getPrimaryKeyFields());
				List<D> result = JdbcTool.selectDatabeans(session.connection(), node.getFieldInfo(), sql);
				return result;
			}else{
				Criteria criteria = node.getCriteriaForConfig(config, session);
				Disjunction prefixesDisjunction = Restrictions.disjunction();
				if(prefixesDisjunction != null){
					for(Key<PK> prefix : prefixes){
						Conjunction prefixConjunction = node.getPrefixConjunction(true, prefix, wildcardLastField);
						if(prefixConjunction == null){
							throw new IllegalArgumentException("cannot do a null prefix match.  Use getAll() " +
									"instead");
						}
						prefixesDisjunction.add(prefixConjunction);
					}
					criteria.add(prefixesDisjunction);
				}
				List<D> result = criteria.list();
				Collections.sort(result);//todo, make sure the datastore scans in order so we don't need to sort here
				return result;
			}
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
