package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.sql.Connection;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.collections.Range;

public class JdbcManagedIndexScanOp
	<PK extends PrimaryKey<PK>,
	D extends Databean<PK,D>,
	IK extends PrimaryKey<IK>,
	IE extends IndexEntry<IK, IE, PK, D>,
	IF extends DatabeanFielder<IK, IE>>
extends BaseJdbcOp<List<IE>>{

	private Range<IK> range;
	private PhysicalNode<PK, D> node;
	private Config config;
	private String traceName;
	private DatabeanFieldInfo<IK, IE, IF> fieldInfo;
	
	public JdbcManagedIndexScanOp(PhysicalNode<PK, D> node, ManagedNode<IK, IE, IF> managedNode, Range<IK> range,
			Config config, String traceName){
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.range = range;
		this.node = node;
		this.config = config;
		this.traceName = traceName;
		this.fieldInfo = managedNode.getFieldInfo();
	}
	
	@Override
	public List<IE> runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), traceName, node.getClientName(), node.getName());
		
		String sql = SqlBuilder.getInRange(config, node.getTableName(), fieldInfo.getFields(), range.getStart(),
				range.getStartInclusive(), range.getEnd(), range.getEndInclusive(), fieldInfo.getPrimaryKeyFields());
		Connection connection = getConnection(node.getClientName());
		List<IE> result = JdbcTool.selectDatabeans(connection, fieldInfo, sql);
		return result;
	}

}
