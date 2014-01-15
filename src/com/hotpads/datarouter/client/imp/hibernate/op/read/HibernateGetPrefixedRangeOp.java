package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.Collection;
import java.util.Collections;
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
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.BatchTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class HibernateGetPrefixedRangeOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<List<D>>{
		
	private HibernateReaderNode<PK,D,F> node;
	private String opName;
	private PK prefix;
	private boolean wildcardLastField;
	private PK start;
	private boolean startInclusive;
	private Config config;
	
	public HibernateGetPrefixedRangeOp(HibernateReaderNode<PK,D,F> node, String opName, PK prefix, 
			boolean wildcardLastField, PK start, boolean startInclusive, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.prefix = prefix;
		this.wildcardLastField = wildcardLastField;
		this.start = start;
		this.startInclusive = startInclusive;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		ClientType clientType = node.getFieldInfo().getFieldAware() ? ClientType.jdbc : ClientType.hibernate;
		DRCounters.incSuffixClientNode(clientType, opName, node.getClientName(), node.getName());
		try{
			TraceContext.startSpan(node.getName()+" "+opName);
			Session session = getSession(node.getClientName());
			if(node.getFieldInfo().getFieldAware()){
				String sql = SqlBuilder.getWithPrefixInRange(config, node.getTableName(), node.getFieldInfo().getFields(), 
						prefix, wildcardLastField, start, startInclusive, null, false, 
						node.getFieldInfo().getPrimaryKeyFields());
				List<D> result = JdbcTool.selectDatabeans(session.connection(), node.getFieldInfo(), sql);
				return result;
			}else{
				Criteria criteria = node.getCriteriaForConfig(config, session);
				node.addPrimaryKeyOrderToCriteria(criteria);
				Conjunction prefixConjunction = node.getPrefixConjunction(true, prefix, wildcardLastField);
				if(prefixConjunction != null){
					criteria.add(prefixConjunction);
				}		
				if(start != null && CollectionTool.notEmpty(start.getFields())){
					List<Field<?>> startFields = FieldTool.prependPrefixes(node.getFieldInfo().getKeyFieldName(), 
							start.getFields());
					int numNonNullStartFields = FieldTool.countNonNullLeadingFields(startFields);
					Disjunction d = Restrictions.disjunction();
					for(int i=numNonNullStartFields; i > 0; --i){
						Conjunction c = Restrictions.conjunction();
						for(int j=0; j < i; ++j){
							Field<?> startField = startFields.get(j);
							if(j < (i-1)){
								c.add(Restrictions.eq(startField.getPrefixedName(), startField.getValue()));
							}else{
								if(startInclusive && i==numNonNullStartFields){
									c.add(Restrictions.ge(startField.getPrefixedName(), startField.getValue()));
								}else{
									c.add(Restrictions.gt(startField.getPrefixedName(), startField.getValue()));
								}
							}
						}
						d.add(c);
					}
					criteria.add(d);
				}
				List<D> result = (List<D>)criteria.list();
				return result;
			}
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
