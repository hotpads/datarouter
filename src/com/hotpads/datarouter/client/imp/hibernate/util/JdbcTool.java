package com.hotpads.datarouter.client.imp.hibernate.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.Session;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class JdbcTool {
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	List<PK> selectPrimaryKeys(Session session, DatabeanFieldInfo<PK,D,F> fieldInfo, String sql){
//		System.out.println(sql);
		try{
			PreparedStatement ps = session.connection().prepareStatement(sql.toString());
			ps.execute();
			ResultSet rs = ps.getResultSet();
			List<PK> primaryKeys = ListTool.createArrayList();
			while(rs.next()){
				PK primaryKey = (PK)FieldSetTool.fieldSetFromJdbcResultSetUsingReflection(
						fieldInfo.getPrimaryKeyClass(), fieldInfo.getPrimaryKeyFields(), rs, true);
				primaryKeys.add(primaryKey);
			}
			return primaryKeys;
		}catch(Exception e){
			throw new DataAccessException(e);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	List<D> selectDatabeans(Session session, DatabeanFieldInfo<PK,D,F> fieldInfo, String sql){
//		System.out.println(sql);
		try{
			PreparedStatement ps = session.connection().prepareStatement(sql.toString());
			ps.execute();
			ResultSet rs = ps.getResultSet();
			List<D> databeans = ListTool.createArrayList();
			while(rs.next()){
				D databean = (D)FieldSetTool.fieldSetFromJdbcResultSetUsingReflection(
						fieldInfo.getDatabeanClass(), fieldInfo.getFields(), rs, false);
				databeans.add(databean);
			}
			return databeans;
		}catch(Exception e){
			String message = "error executing sql:"+sql.toString();
			throw new DataAccessException(message, e);
		}
	}
	
	public static Long count(Session session, String sql) {
		try {
			PreparedStatement ps = session.connection().prepareStatement(sql);
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
	
	@SuppressWarnings("deprecation")
	public static int update(Session session, String sql){
//		System.out.println(sql);
		try{
			PreparedStatement stmt = session.connection().prepareStatement(sql);
			return stmt.executeUpdate();
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	public static int update(Connection conn, String sql){
//		System.out.println(sql);
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
		if(CollectionTool.isEmpty(updates)){ return 0; }
		if(CollectionTool.differentSize(updates, inserts)){ throw new IllegalArgumentException("updates vs inserts size mismatch"); }
		
		int[] updateSuccessFlags = bulkUpdate(conn, updates);
		List<String> neededInserts = ListTool.createLinkedList();
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
		if(conn==null || CollectionTool.isEmpty(sql)){ return new int[]{}; }
		return bulkUpdate(conn, sql.toArray(new String[sql.size()]));
	}
	
	public static int[] bulkUpdate(Connection conn, String[] sql){
		try{
			if(conn==null || ArrayTool.isEmpty(sql)){ return new int[]{}; }
			int numStatements = ArrayTool.nullSafeLength(sql);
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
}
