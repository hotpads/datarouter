package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.trace.TraceContext;

public class HibernateGetOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<List<D>>{
		
	private final HibernateReaderNode<PK,D,F> node;
	private final Collection<PK> keys;
	private final Config config;
	
	public HibernateGetOp(HibernateReaderNode<PK,D,F> node, Collection<PK> keys, Config config) {
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.keys = keys;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		Session session = getSession(node.getClientId().getName());
		List<? extends Key<PK>> sortedKeys = DrListTool.createArrayList(keys);
		Collections.sort(sortedKeys);//is this sorting at all beneficial?
		Criteria criteria = node.getCriteriaForConfig(config, session);
		Disjunction orSeparatedIds = Restrictions.disjunction();
		for(Key<PK> key : DrCollectionTool.nullSafe(sortedKeys)){
			Conjunction possiblyCompoundId = Restrictions.conjunction();
			List<Field<?>> fields = FieldTool.prependPrefixes(node.getFieldInfo().getKeyFieldName(), 
					key.getFields());
			for(Field<?> field : fields){
				possiblyCompoundId.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
			}
			orSeparatedIds.add(possiblyCompoundId);
		}
		criteria.add(orSeparatedIds);
		List<D> result = criteria.list();
		TraceContext.appendToSpanInfo("[got "+DrCollectionTool.size(result)+"/"+DrCollectionTool.size(keys)+"]");
		return result;
	}
	
}
