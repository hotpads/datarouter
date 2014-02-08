package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.List;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;

public class JdbcGetPrefixedRangeOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<List<D>>{
		
	private JdbcReaderNode<PK,D,F> node;
	private String opName;
	private PK prefix;
	private boolean wildcardLastField;
	private PK start;
	private boolean startInclusive;
	private Config config;
	
	public JdbcGetPrefixedRangeOp(JdbcReaderNode<PK,D,F> node, String opName, PK prefix, 
			boolean wildcardLastField, PK start, boolean startInclusive, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.prefix = prefix;
		this.wildcardLastField = wildcardLastField;
		this.start = start;
		this.startInclusive = startInclusive;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		DRCounters.incSuffixClientNode(ClientType.jdbc, opName, node.getClientName(), node.getName());
		try{
			TraceContext.startSpan(node.getName()+" "+opName);
			String sql = SqlBuilder.getWithPrefixInRange(config, node.getTableName(), node.getFieldInfo().getFields(), 
					prefix, wildcardLastField, start, startInclusive, null, false, 
					node.getFieldInfo().getPrimaryKeyFields());
			List<D> result = JdbcTool.selectDatabeans(getConnection(node.getClientName()), node.getFieldInfo(), sql);
			return result;
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
