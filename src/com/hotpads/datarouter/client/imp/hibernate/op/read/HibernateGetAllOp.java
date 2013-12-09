package com.hotpads.datarouter.client.imp.hibernate.op.read;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;

public class HibernateGetAllOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseParallelHibernateTxnApp<List<D>>{
		
	private HibernateReaderNode<PK,D,F> node;
	private String opName;
	private Config config;
	
	public HibernateGetAllOp(HibernateReaderNode<PK,D,F> node, String opName, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
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
				DRCounters.incSuffixClientNode(ClientType.jdbc, opName, node.getClientName(), node.getName());
				String sql = SqlBuilder.getAll(config, node.getTableName(), node.getFieldInfo().getFields(), null, 
						node.getFieldInfo().getPrimaryKeyFields());
				List<D> result = JdbcTool.selectDatabeans(session, node.getFieldInfo(), sql);
				return result;
			}else{
				DRCounters.incSuffixClientNode(ClientType.hibernate, opName, node.getClientName(), node.getName());
				Criteria criteria = node.getCriteriaForConfig(config, session);
				List<D> databeans = criteria.list();
				return databeans;//assume they come back sorted due to innodb
			}
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
