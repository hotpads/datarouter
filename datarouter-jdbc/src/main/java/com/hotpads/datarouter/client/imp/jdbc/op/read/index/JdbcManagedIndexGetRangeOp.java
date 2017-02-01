package com.hotpads.datarouter.client.imp.jdbc.op.read.index;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

public class JdbcManagedIndexGetRangeOp<
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

	public JdbcManagedIndexGetRangeOp(PhysicalNode<PK,D> node, JdbcFieldCodecFactory fieldCodecFactory,
			DatabeanFieldInfo<IK, IE, IF> fieldInfo, Range<IK> range, Config config){
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.range = range;
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.config = config;
		this.fieldInfo = fieldInfo;
	}

	@Override
	public List<IE> runOnce(){
		Optional<String> indexName = Optional.of(fieldInfo.getTableName());
		String sql = SqlBuilder.getInRanges(fieldCodecFactory, config, node.getTableName(), fieldInfo.getFields(),
				Arrays.asList(range), fieldInfo.getPrimaryKeyFields(), indexName,
				Optional.of(fieldInfo.getCharacterSet()), Optional.of(fieldInfo.getCollation()));//TODO test right info?
		System.out.println(sql);
		Connection connection = getConnection(node.getClientId().getName());
		List<IE> result = JdbcTool.selectDatabeans(fieldCodecFactory, connection, fieldInfo, sql);
		return result;
	}

}
