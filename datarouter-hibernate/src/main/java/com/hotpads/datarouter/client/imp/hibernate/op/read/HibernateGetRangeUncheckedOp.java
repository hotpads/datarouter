package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.client.imp.hibernate.util.CriteriaTool;
import com.hotpads.datarouter.client.imp.hibernate.util.HibernateResultParser;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.util.core.collections.Range;

//TODO this is the jdbc implementation, so extend or abstract it
public class HibernateGetRangeUncheckedOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseHibernateOp<List<? extends FieldSet<?>>>{

	private final HibernateReaderNode<PK,D,F> node;
	private final HibernateResultParser resultParser;
	private final String opName;
	private final Collection<Range<PK>> ranges;
	private final boolean keysOnly;
	private final Config config;

	public HibernateGetRangeUncheckedOp(HibernateReaderNode<PK,D,F> node, HibernateResultParser resultParser,
			String opName, Collection<Range<PK>> ranges, boolean keysOnly, Config config){
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.resultParser = resultParser;
		this.opName = opName;
		this.ranges = ranges;
		this.keysOnly = keysOnly;
		this.config = config;
	}

	@Override
	public List<? extends FieldSet<?>> runOnce(){
		DRCounters.incClientNodeCustom(node.getClient().getType(), opName, node.getClientId().getName(),
				node.getName());
		Session session = getSession(node.getClientId().getName());
		Criteria criteria = node.getCriteriaForConfig(config, session);
		if(keysOnly){
			ProjectionList projectionList = Projections.projectionList();
			for(Field<?> field : node.getFieldInfo().getPrefixedPrimaryKeyFields()){
				projectionList.add(Projections.property(field.getPrefixedName()));
			}
			criteria.setProjection(projectionList);
		}
		node.addPrimaryKeyOrderToCriteria(criteria);
		Disjunction rangesDisjunction = Restrictions.disjunction();
		for(Range<PK> range : ranges){
			rangesDisjunction.add(CriteriaTool.makeRangeConjunction(range, node.getFieldInfo()));
		}
		criteria.add(rangesDisjunction);
		if(keysOnly){
			List<Object[]> rows = criteria.list();
			List<PK> result = new ArrayList<>(DrCollectionTool.size(rows));
			for(Object row : DrIterableTool.nullSafe(rows)){
				// hibernate will return a plain Object if it's a single col PK
				Object[] rowCells;
				if(row instanceof Object[]){
					rowCells = (Object[])row;
				}else{
					rowCells = new Object[]{row};
				}
				result.add(resultParser.fieldSetFromHibernateResultUsingReflection(
						node.getFieldInfo().getPrimaryKeyClass(), node.getFieldInfo().getPrimaryKeyFields(),
						rowCells));
			}
			DRCounters.incClientNodeCustom(node.getClient().getType(), opName + " rows", node.getClientId().getName(),
					node.getName(), DrCollectionTool.size(result));
			return result;
		}
		List<? extends FieldSet<?>> result = criteria.list();
		DRCounters.incClientNodeCustom(node.getClient().getType(), opName + " rows", node.getClientId().getName(),
				node.getName(), DrCollectionTool.size(result));
		return result;
	}

}
