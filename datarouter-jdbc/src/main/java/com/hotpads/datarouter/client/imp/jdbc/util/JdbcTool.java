package com.hotpads.datarouter.client.imp.jdbc.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.java.ReflectionTool;

public class JdbcTool {

	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String TABLE_CATALOG = "TABLE_CAT";

	public static Connection openConnection(String hostname, int port, String database, String user,
			String password){
		Connection conn = null;
		try {
			Class.forName(JDBC_DRIVER).newInstance();//not quite sure why we need this
			String url = "jdbc:mysql://" + hostname + ":" + port + "/"
			+ DrStringTool.nullSafe(database) + "?user=" + user + "&password=" + password;
			conn = DriverManager.getConnection(url);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
		return conn;
	}

	public static void closeConnection(Connection connection){
		try {
			if(connection==null){ return; }
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<String> showTables(Connection connection){
		try {
			List<String> tableNames = new ArrayList<>();
			String tableName;
			ResultSet rs = connection.getMetaData().getTables(null, null, "%", null);
			while(rs.next()){
				tableName = rs.getString(3);
				tableNames.add(tableName);
			}
			return tableNames;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	public static List<String> showDatabases(Connection connection){
		try{
			ResultSet rs = connection.getMetaData().getCatalogs();
			List<String> catalogs = new ArrayList<>();
			while(rs.next()){
				catalogs.add(rs.getString(TABLE_CATALOG));
			}
			return catalogs;
		}catch(SQLException e){
			throw new RuntimeException(e);
		}

	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	List<PK> selectPrimaryKeys(JdbcFieldCodecFactory fieldCodecFactory, Connection connection,
			DatabeanFieldInfo<PK,D,F> fieldInfo, String sql){
		try{
			PreparedStatement ps = connection.prepareStatement(sql.toString());
			ps.execute();
			ResultSet rs = ps.getResultSet();
			List<PK> primaryKeys = new ArrayList<>();
			while(rs.next()){
				PK primaryKey = fieldSetFromJdbcResultSetUsingReflection(fieldCodecFactory,
						fieldInfo.getPrimaryKeyClass(), fieldInfo.getPrimaryKeyFields(), rs, true);
				primaryKeys.add(primaryKey);
			}
			return primaryKeys;
		}catch(Exception e){
			throw new DataAccessException(e);
		}
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	List<D> selectDatabeans(JdbcFieldCodecFactory fieldCodecFactory, Connection connection,
			DatabeanFieldInfo<PK,D,F> fieldInfo, String sql){
		try{
			PreparedStatement ps = connection.prepareStatement(sql.toString());
			ps.execute();
			ResultSet rs = ps.getResultSet();
			List<D> databeans = new ArrayList<>();
			while(rs.next()){
				D databean = fieldSetFromJdbcResultSetUsingReflection(fieldCodecFactory,
						fieldInfo.getDatabeanClass(), fieldInfo.getFields(), rs, false);
				databeans.add(databean);
			}
			return databeans;
		}catch(Exception e){
			String message = "error executing sql:"+sql.toString();
			throw new DataAccessException(message, e);
		}
	}

	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IK> selectIndexEntryKeys(JdbcFieldCodecFactory fieldCodecFactory, Connection connection,
			DatabeanFieldInfo<IK,IE,IF> fieldInfo, String sql){
		try{
			PreparedStatement ps = connection.prepareStatement(sql.toString());
			ps.execute();
			ResultSet rs = ps.getResultSet();
			List<IK> keys = new ArrayList<>();
			while(rs.next()){
				IK key = fieldSetFromJdbcResultSetUsingReflection(fieldCodecFactory,
						fieldInfo.getPrimaryKeyClass(), fieldInfo.getPrimaryKeyFields(), rs, false);
				keys.add(key);
			}
			return keys;
		}catch(Exception e){
			String message = "error executing sql:"+sql.toString();
			throw new DataAccessException(message, e);
		}
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,PKLookup extends Lookup<PK>>List<PKLookup>
	selectLookups(JdbcFieldCodecFactory fieldCodecFactory, Connection connection, List<Field<?>> selectedFields,
			Class<PKLookup> lookupClass, String sql, Class<PK> keyClass){
		try{
			PreparedStatement ps = connection.prepareStatement(sql.toString());
			ps.execute();
			ResultSet rs = ps.getResultSet();
			List<PKLookup> lookups = new ArrayList<>();
			while(rs.next()){
				PKLookup lookup = lookupFromJdbcResultSetUsingReflection(fieldCodecFactory, lookupClass,
						selectedFields, rs, keyClass);
				lookups.add(lookup);
			}
			return lookups;
		}catch(Exception e){
			String message = "error executing sql:"+sql.toString();
			throw new DataAccessException(message, e);
		}
	}

	private static <PK extends PrimaryKey<PK>, PKLookup extends Lookup<PK>>
	PKLookup lookupFromJdbcResultSetUsingReflection(JdbcFieldCodecFactory fieldCodecFactory, Class<PKLookup> cls,
			List<Field<?>> fields, ResultSet rs, Class<PK> keyClass){
		PKLookup targetFieldSet = ReflectionTool.create(cls);
		targetFieldSet.setPrimaryKey(ReflectionTool.create(keyClass));
		for(JdbcFieldCodec<?,?> field : fieldCodecFactory.createCodecs(fields)){
			field.fromJdbcResultSetUsingReflection(targetFieldSet, rs);
		}
		return targetFieldSet;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	List<D> selectDatabeansCleaned(JdbcFieldCodecFactory fieldCodecFactory, Connection connection,
			DatabeanFieldInfo<PK,D,F> fieldInfo, String sql, String...values){
		try{
			PreparedStatement ps = connection.prepareStatement(sql.toString());
			for(int i = 0; i < values.length; i++){
				ps.setString(i+1, values[i]);
			}
			ps.execute();
			ResultSet rs = ps.getResultSet();
			List<D> databeans = new ArrayList<>();
			while(rs.next()){
				D databean = fieldSetFromJdbcResultSetUsingReflection(fieldCodecFactory,
						fieldInfo.getDatabeanClass(), fieldInfo.getFields(), rs, false);
				databeans.add(databean);
			}
			return databeans;
		}catch(Exception e){
			String message = "error executing sql:"+sql.toString();
			throw new DataAccessException(message, e);
		}
	}

	public static Long count(Connection connection, String sql) {
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.execute();
			ResultSet rs = ps.getResultSet();
			rs.next();
			Long count = rs.getLong(1);
			return count;
		} catch (Exception e) {
			String message = "error executing sql:"+sql.toString();
			throw new DataAccessException(message, e);
		}
	}

	public static int update(Connection conn, String sql){
		try{
			PreparedStatement stmt = conn.prepareStatement(sql);
			return stmt.executeUpdate();
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	public static int tryUpdate(Connection conn, String sql){
		try{
			PreparedStatement stmt = conn.prepareStatement(sql);
			return stmt.executeUpdate();
		}catch(SQLException e){
			//System.out.println(e.getMessage());
			return 0;
		}
	}

	public static int updateAndInsertIfMissing(Connection conn, List<String> updates, List<String> inserts){
		if(DrCollectionTool.isEmpty(updates)){ return 0; }
		if(DrCollectionTool.differentSize(updates, inserts)){ throw new IllegalArgumentException("updates vs inserts size mismatch"); }

		int[] updateSuccessFlags = bulkUpdate(conn, updates);
		List<String> neededInserts = new LinkedList<>();
		int index = -1;
		for(String insert : inserts){
			++index;
			if(updateSuccessFlags[index]==0){
				neededInserts.add(insert);
			}
		}
		bulkUpdate(conn, neededInserts);
		return neededInserts.size();
	}

	public static int[] bulkUpdate(Connection conn, List<String> sql){
		if(conn==null || DrCollectionTool.isEmpty(sql)){ return new int[]{}; }
		return bulkUpdate(conn, sql.toArray(new String[sql.size()]));
	}

	public static int[] bulkUpdate(Connection conn, String[] sql){
		try{
			if(conn==null || DrArrayTool.isEmpty(sql)){ return new int[]{}; }
			int numStatements = DrArrayTool.nullSafeLength(sql);
			if(numStatements < 1){ return null; }
			PreparedStatement stmt = conn.prepareStatement(sql[0]);
			stmt.addBatch();
			if(numStatements > 1){
				for(int i=1; i < sql.length; ++i){
					stmt.addBatch(sql[i]);
				}
			}
			int[] rowsUpdated = stmt.executeBatch();
			return rowsUpdated;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	public static void appendCsvQuestionMarks(StringBuilder sb, int num){
		for(int i=0; i < num; ++i){
			if(i>0){ sb.append(","); }
			sb.append("?");
		}
	}

	public static <F>F fieldSetFromJdbcResultSetUsingReflection(JdbcFieldCodecFactory fieldCodecFactory,
			Class<F> cls, List<Field<?>> fields, ResultSet rs, boolean ignorePrefix){
		F targetFieldSet = ReflectionTool.create(cls);
		for(JdbcFieldCodec<?,?> field : fieldCodecFactory.createCodecs(fields)){
			field.fromJdbcResultSetUsingReflection(targetFieldSet, rs);
		}
		return targetFieldSet;
	}


	/********************** tests ***************************/

	public static class TestJdbcTool{
		@Test public void showTablesTest(){
			Connection conn = JdbcTool.openConnection("localhost", 3306, "property", "root", "");
			List<String> l = showTables(conn);
			for(String s : l){
				System.out.println(s);
			}
		}
	}

}
