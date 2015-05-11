package com.hotpads.datarouter.client.imp.jdbc.op.read.index;

import java.sql.Connection;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

public class JdbcManagedIndexScanOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK, IE>>
extends BaseJdbcOp<List<IE>>{

	private final Range<IK> range;
	private final PhysicalNode<PK, D> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Config config;
	private final DatabeanFieldInfo<IK, IE, IF> fieldInfo;
	
	public JdbcManagedIndexScanOp(PhysicalNode<PK,D> node, JdbcFieldCodecFactory fieldCodecFactory,
			ManagedNode<IK,IE,IF> managedNode, Range<IK> range, Config config){
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.range = range;
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.config = config;
		this.fieldInfo = managedNode.getFieldInfo();
	}
	
	@Override
	public List<IE> runOnce(){
		String sql = SqlBuilder.getInRange(fieldCodecFactory, config, node.getTableName(), fieldInfo.getFields(), range
				.getStart(), range.getStartInclusive(), range.getEnd(), range.getEndInclusive(), fieldInfo
				.getPrimaryKeyFields());
		Connection connection = getConnection(node.getClientName());
		List<IE> result = JdbcTool.selectDatabeans(fieldCodecFactory, connection, fieldInfo, sql);
		return result;
	}

}
