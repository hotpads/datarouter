package com.hotpads.datarouter.client.imp.jdbc.field.codec.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BaseListJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.util.core.bytes.DoubleByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class DoubleArrayJdbcFieldCodec
extends BaseListJdbcFieldCodec<Double,List<Double>,Field<List<Double>>>{

	public DoubleArrayJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}

	public DoubleArrayJdbcFieldCodec(Field<List<Double>> field){
		super(field);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return new SqlColumn(field.getKey().getColumnName(), MySqlColumnType.LONGBLOB, Integer.MAX_VALUE,
				allowNullable && field.getKey().isNullable(), false);
	}

	@Override
	public List<Double> parseJdbcValueButDoNotSet(Object col){
		throw new NotImplementedException("code needs testing");
	}

	@Override
	public List<Double> fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			byte[] bytes = rs.getBytes(field.getKey().getColumnName());
			if(DrArrayTool.isEmpty(bytes)){
				return new ArrayList<>();
			}
			return DoubleByteTool.fromDoubleByteArray(bytes, 0);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex,
					field.getValue() == null ? null : DoubleByteTool.getDoubleByteArray(field.getValue()));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
}
