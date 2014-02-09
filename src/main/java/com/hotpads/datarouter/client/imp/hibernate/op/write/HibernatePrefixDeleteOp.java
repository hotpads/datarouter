package com.hotpads.datarouter.client.imp.hibernate.op.write;

import org.hibernate.Session;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.ListTool;

@Deprecated//use Jdbc op
public class HibernatePrefixDeleteOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<Long>{
		
	private HibernateNode<PK,D,F> node;
	private String opName;
	private PK prefix;
	private boolean wildcardLastField;
	private Config config;
	
	public HibernatePrefixDeleteOp(HibernateNode<PK,D,F> node, String opName, PK prefix, boolean wildcardLastField,
			Config config){
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.prefix = prefix;
		this.wildcardLastField = wildcardLastField;
		this.config = config;
	}
	
	@Override
	public Long runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		try{
			TraceContext.startSpan(node.getName()+" "+opName);
			Session session = getSession(node.getClientName());
			String sql = SqlBuilder.deleteWithPrefixes(config, node.getTableName(), ListTool.wrap(prefix),
					wildcardLastField);
			long numModified = JdbcTool.update(session.connection(), sql.toString());
			return numModified;
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
