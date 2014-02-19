package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.CollectionTool;

public class HibernateGetFirstOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<D>{
		
	private HibernateReaderNode<PK,D,F> node;
	private String opName;
	private Config config;
	
	public HibernateGetFirstOp(HibernateReaderNode<PK,D,F> node, String opName, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.config = config;
	}
	
	@Override
	public D runOnce(){
		ClientType clientType = node.getFieldInfo().getFieldAware() ? ClientType.jdbc : ClientType.hibernate;
		DRCounters.incSuffixClientNode(clientType, opName, node.getClientName(), node.getName());
		try{
			TraceContext.startSpan(node.getName()+" "+opName);
			Session session = getSession(node.getClientName());
			if(node.getFieldInfo().getFieldAware()){
				Config nullSafeConfig = Config.nullSafe(config);
				nullSafeConfig.setLimit(1);
				String sql = SqlBuilder.getAll(config, node.getTableName(), node.getFieldInfo().getFields(), null, 
						node.getFieldInfo().getPrimaryKeyFields());
				List<D> result = JdbcTool.selectDatabeans(session, node.getFieldInfo(), sql);
				return CollectionTool.getFirst(result);
			}else{
				Criteria criteria = node.getCriteriaForConfig(config, session);
				criteria.setMaxResults(1);
				D result = (D)criteria.uniqueResult();
				return result;
			}
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
