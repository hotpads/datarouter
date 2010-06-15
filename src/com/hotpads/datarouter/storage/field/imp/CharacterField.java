package com.hotpads.datarouter.storage.field.imp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.bytes.StringByteTool;

public class CharacterField extends PrimitiveField<Character>{

	public CharacterField(String name, Character value){
		super(name, value);
	}

	public CharacterField(String prefix, String name, Character value){
		super(prefix, name, value);
	}

	@Override
	public Character parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Character)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.VARCHAR);
			}else{
				ps.setString(parameterIndex, this.value==null?null:this.value+"");
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Character fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			String s = rs.getString(this.name);
			return s==null?null:s.charAt(0);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public byte[] getBytes(){
		return StringByteTool.getByteArray(this.value.toString(), StringByteTool.CHARSET_UTF8);
	}

}
