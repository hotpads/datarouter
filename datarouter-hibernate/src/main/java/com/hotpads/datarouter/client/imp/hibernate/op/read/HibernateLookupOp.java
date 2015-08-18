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
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class HibernateLookupOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<List<D>>{
		
	private final HibernateReaderNode<PK,D,F> node;
	private final Collection<? extends Lookup<PK>> lookups;
	private final boolean wildcardLastField;
	private final Config config;
	
	public HibernateLookupOp(HibernateReaderNode<PK,D,F> node, Collection<? extends Lookup<PK>> lookups, 
			boolean wildcardLastField, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.lookups = lookups;
		this.wildcardLastField = wildcardLastField;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		if(DrCollectionTool.isEmpty(lookups)){
			return Collections.emptyList();
		}
		Session session = getSession(node.getClientId().getName());
		//TODO undefined behavior on trailing nulls
		Criteria criteria = node.getCriteriaForConfig(config, session);
		Disjunction or = Restrictions.disjunction();
		for(Lookup<PK> lookup : lookups){
			Conjunction prefixConjunction = node.getPrefixConjunction(false, lookup, wildcardLastField);
			if(prefixConjunction==null){
				throw new IllegalArgumentException("Lookup with all null fields would return entire "
						+ "table.  Please use getAll() instead.");
			}
			or.add(prefixConjunction);
		}
		criteria.add(or);
		@SuppressWarnings("unchecked")
		List<D> result = criteria.list();
		return result;
	}
	
}
