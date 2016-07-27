package com.hotpads.joblet.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.enums.JobletStatus;

public class RestartJobletRequests extends BaseJdbcOp<Integer>{

	private final String tableName;
	private final JobletStatus currentStatus;

	public RestartJobletRequests(Datarouter datarouter, JobletNodes jobletNodes, JobletStatus currentStatus) {
		super(datarouter, jobletNodes.jobletRequest().getMaster().getClientNames());
		this.tableName = jobletNodes.jobletRequest().getMaster().getPhysicalNodeIfApplicable().getTableName();
		this.currentStatus = currentStatus;
	}

	@Override
	public Integer mergeResults(Integer fromOnce, Collection<Integer> fromEachClient){
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public Integer runOncePerClient(Client client){
		String sql;
		if(currentStatus.getPersistentString().equals(JobletStatus.timedOut.getPersistentString())){
			sql = "update " + tableName + " set status=?, numFailures=0" + " where status=? and type != 'FeedImport'";
		}else{
			sql = "update " + tableName + " set status=?, numFailures=0 where status=?";
		}

		Connection connection = getConnection(client.getName());
		try{
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, JobletStatus.created.getPersistentString());
			statement.setString(2, currentStatus.getPersistentString());
			return statement.executeUpdate();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
	}

}
