package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.java.ReflectionTool;

public class JdbcGetIndexOp<PK extends PrimaryKey<PK>,
							D extends Databean<PK, D>,
							IK extends PrimaryKey<IK>,
							IE extends IndexEntry<IK, IE, PK, D>,
							IF extends DatabeanFielder<IK,IE>>
							extends BaseJdbcOp<List<IE>>{
	
	private Config config;
	private PhysicalNode<PK, D> mainNode;
	private Class<IE> indexEntryClass;
	private DatabeanFielder<IK, IE> indexFielder;
	private IE indexEntry;
	private Collection<IK> uniqueKeys;

	public JdbcGetIndexOp(PhysicalNode<PK, D> node, Config config, Class<IE> indexEntryClass,
			Class<IF> indexFielderClass, Collection<IK> uniqueKeys){
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.mainNode = node;
		this.config = config;
		this.indexEntryClass = indexEntryClass;
		this.uniqueKeys = uniqueKeys;
		this.indexFielder = ReflectionTool.create(indexFielderClass);
		this.indexEntry = ReflectionTool.create(indexEntryClass);
	}

	@Override
	public List<IE> runOnce(){
		List<? extends Key<IK>> keys = ListTool.createArrayList(uniqueKeys);
		String sql = SqlBuilder.getMulti(config, mainNode.getTableName(), indexFielder.getFields(indexEntry), keys);
		Connection connection = getConnection(mainNode.getClientName());
		List<IE> databeans = ListTool.createArrayList();
		try{
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.execute();
			ResultSet rs = ps.getResultSet();
			while(rs.next()){
				IE databean = FieldSetTool.fieldSetFromJdbcResultSetUsingReflection(indexEntryClass, indexFielder.getFields(indexEntry), rs, false);
				databeans.add(databean);
			}
		}catch(Exception e){
			String message = "error executing sql:"+sql.toString();
			throw new DataAccessException(message, e);
		}
		return databeans;
	}
};