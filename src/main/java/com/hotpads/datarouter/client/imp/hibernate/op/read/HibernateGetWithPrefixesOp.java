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

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;

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
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.prefixes = prefixes;
		this.wildcardLastField = wildcardLastField;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		if(DrCollectionTool.isEmpty(prefixes)){ return new LinkedList<D>(); }
		DRCounters.incClientNodeCustom(node.getClient().getType(), opName, node.getClientName(), node.getName());
		Session session = getSession(node.getClientName());
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
	
}
