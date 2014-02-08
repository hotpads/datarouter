package com.hotpads.datarouter.client.imp.hibernate.op.write;

import org.hibernate.Session;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.ListTool;

@Deprecated//use Jdbc op
public class HibernateIndexDeleteOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<Long>{
		
	private HibernateNode<PK,D,F> node;
	private String opName;
	private Lookup<PK> lookup;
	private Config config;
	
	public HibernateIndexDeleteOp(HibernateNode<PK,D,F> node, String opName, Lookup<PK> lookup, Config config){
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.lookup = lookup;
		this.config = config;
	}
	
	@Override
	public Long runOnce(){
		ClientType clientType = node.getFieldInfo().getFieldAware() ? ClientType.jdbc : ClientType.hibernate;
		DRCounters.incSuffixClientNode(clientType, opName, node.getClientName(), node.getName());
		try{
			TraceContext.startSpan(node.getName()+" "+opName);
			Session session = getSession(node.getClientName());
			String sql = SqlBuilder.deleteMulti(config, node.getTableName(), ListTool.wrap(lookup));
			long numModified = JdbcTool.update(session.connection(), sql.toString());
			return numModified;
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
