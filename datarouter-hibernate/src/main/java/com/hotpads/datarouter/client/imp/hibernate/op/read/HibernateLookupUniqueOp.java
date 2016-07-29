package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.ArrayList;
import java.util.Collection;
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
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.iterable.BatchingIterable;

public class HibernateLookupUniqueOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseHibernateOp<List<D>>{

	private final HibernateReaderNode<PK,D,F> node;
	private final Collection<? extends UniqueKey<PK>> uniqueKeys;
	private final Config config;

	public HibernateLookupUniqueOp(HibernateReaderNode<PK,D,F> node, Collection<? extends UniqueKey<PK>> uniqueKeys,
			Config config) {
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.uniqueKeys = uniqueKeys;
		this.config = config;
	}

	@Override
	public List<D> runOnce(){
		Session session = getSession(node.getClientId().getName());

		int batchSize = HibernateReaderNode.DEFAULT_ITERATE_BATCH_SIZE;
		if(config!=null && config.getIterateBatchSize()!=null){
			batchSize = config.getIterateBatchSize();
		}
		List<D> all = new ArrayList<>(uniqueKeys.size());
		for(List<? extends Key<PK>> keyBatch : new BatchingIterable<>(uniqueKeys, batchSize)){
			Criteria criteria = node.getCriteriaForConfig(config, session);
			Disjunction orSeparatedIds = Restrictions.disjunction();
			for(Key<PK> key : DrCollectionTool.nullSafe(keyBatch)){
				Conjunction possiblyCompoundId = Restrictions.conjunction();
				List<Field<?>> fields = key.getFields();
				for(Field<?> field : fields){
					possiblyCompoundId.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
				}
				orSeparatedIds.add(possiblyCompoundId);
			}
			criteria.add(orSeparatedIds);
			@SuppressWarnings("unchecked")
			List<D> batch = criteria.list();
			all.addAll(DrCollectionTool.nullSafe(batch));
		}
		return all;
	}

}
