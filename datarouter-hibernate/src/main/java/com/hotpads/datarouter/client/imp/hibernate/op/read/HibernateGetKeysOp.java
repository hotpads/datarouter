package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.ArrayList;
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

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.client.imp.hibernate.util.HibernateResultParser;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;

public class HibernateGetKeysOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<List<PK>>{
		
	private final HibernateReaderNode<PK,D,F> node;
	private final HibernateResultParser resultParser;
	private final Collection<PK> keys;
	private final Config config;
	
	public HibernateGetKeysOp(HibernateReaderNode<PK,D,F> node, HibernateResultParser resultParser,
			Collection<PK> keys, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.resultParser = resultParser;
		this.keys = keys;
		this.config = config;
	}
	
	@Override
	public List<PK> runOnce(){
		Session session = getSession(node.getClientName());
		List<? extends Key<PK>> sortedKeys = DrListTool.createArrayList(keys);
		Collections.sort(sortedKeys);//is this sorting at all beneficial?
		List<PK> result = new ArrayList<>(keys.size());
		Criteria criteria = node.getCriteriaForConfig(config, session);
		//projection list
		ProjectionList projectionList = Projections.projectionList();
		int numFields = 0;
		for(Field<?> field : node.getFieldInfo().getPrefixedPrimaryKeyFields()){
			projectionList.add(Projections.property(field.getKey().getPrefixedName()));
			++numFields;
		}
		criteria.setProjection(projectionList);
		//where clause
		Disjunction orSeparatedIds = Restrictions.disjunction();
		for(Key<PK> key : DrCollectionTool.nullSafe(keys)){
			Conjunction possiblyCompoundId = Restrictions.conjunction();
			List<Field<?>> fields = FieldTool.prependPrefixes(node.getFieldInfo().getKeyFieldName(), key.getFields());
			for(Field<?> field : fields){
				possiblyCompoundId.add(Restrictions.eq(field.getKey().getPrefixedName(), field.getValue()));
			}
			orSeparatedIds.add(possiblyCompoundId);
		}
		criteria.add(orSeparatedIds);
		List<Object[]> rows = criteria.list();
		for(Object[] row : DrIterableTool.nullSafe(rows)){
			result.add(resultParser.fieldSetFromHibernateResultUsingReflection(
					node.getFieldInfo().getPrimaryKeyClass(), node.getFieldInfo().getPrimaryKeyFields(), row));
		}
		return result;
	}
	
}
