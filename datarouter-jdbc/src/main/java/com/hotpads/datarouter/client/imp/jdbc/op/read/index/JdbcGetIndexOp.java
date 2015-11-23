package com.hotpads.datarouter.client.imp.jdbc.op.read.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.iterable.BatchingIterable;

public class JdbcGetIndexOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseJdbcOp<List<IE>>{

	private final Config config;
	private final PhysicalNode<PK, D> mainNode;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final DatabeanFielder<IK, IE> indexFielder;
	private final IE indexEntry;
	private final Collection<IK> uniqueKeys;
	private final Supplier<IE> indexEntrySupplier;

	public JdbcGetIndexOp(PhysicalNode<PK,D> node, JdbcFieldCodecFactory fieldCodecFactory, Config config,
			Supplier<IE> indexEntrySupplier, Supplier<IF> indexFielderSupplier, Collection<IK> uniqueKeys){
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.mainNode = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.config = config;
		this.indexEntrySupplier = indexEntrySupplier;
		this.uniqueKeys = uniqueKeys;
		this.indexFielder = indexFielderSupplier.get();
		this.indexEntry = indexEntrySupplier.get();
	}

	@Override
	public List<IE> runOnce(){
		Connection connection = getConnection(mainNode.getClientId().getName());
		List<IE> databeans = new ArrayList<>();
		for(List<IK> batch : new BatchingIterable<>(uniqueKeys, JdbcNode.DEFAULT_ITERATE_BATCH_SIZE)){
			List<? extends Key<IK>> keys = DrListTool.createArrayList(batch);
			String sql = SqlBuilder.getMulti(fieldCodecFactory, config, mainNode.getTableName(), indexFielder.getFields(
					indexEntry), keys);
			try{
				PreparedStatement ps = connection.prepareStatement(sql);
				ps.execute();
				ResultSet rs = ps.getResultSet();
				while(rs.next()){
					IE databean = JdbcTool.fieldSetFromJdbcResultSetUsingReflection(fieldCodecFactory,
							indexEntrySupplier, indexFielder.getFields(indexEntry), rs);
					databeans.add(databean);
				}
			}catch(Exception e){
				String message = "error executing sql:"+sql.toString();
				throw new DataAccessException(message, e);
			}
		}
		return databeans;
	}
};