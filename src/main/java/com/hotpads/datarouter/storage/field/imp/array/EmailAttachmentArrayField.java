package com.hotpads.datarouter.storage.field.imp.array;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.email.generic.EmailAttachment;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.exception.NotImplementedException;
import com.hotpads.util.core.serialize.SerializeTool;

public class EmailAttachmentArrayField extends BaseListField<EmailAttachment,List<EmailAttachment>>{
    private static final Logger logger = LoggerFactory.getLogger(EmailAttachmentArrayField.class);
    
	public EmailAttachmentArrayField(String name, List<EmailAttachment> value){
		super(name, value);
	}
	
	public EmailAttachmentArrayField(String prefix, String name, List<EmailAttachment> value){
		super(prefix, name, value);
	}
	
	
	/*********************** StringEncodedField 
	 * @throws UnsupportedEncodingException ***********************/
	
	@Override
	public String getStringEncodedValue() {
		byte[] bytes = getBytes();
		if ( bytes != null ) {
			return StringByteTool.fromUtf8Bytes(bytes);
		}
		return null;
	}
	
	@Override
	public List<EmailAttachment> parseStringEncodedValueButDoNotSet(String s){
		if ( s == null ) {
			return null;
		}
		byte[] bytes = StringByteTool.getUtf8Bytes(s);
		return fromBytes(bytes);
	}
	

	/*********************** ByteEncodedField ***********************/
	
	@Override
	public byte[] getBytes(){
		return toBytes(value);
	}

	public static byte[] toBytes( List<EmailAttachment> attachments ) {
		if( attachments == null ) {
			attachments = new ArrayList<EmailAttachment>( 0 );
		}
		return SerializeTool.serialize( attachments );
	}
	
	
	@Override
	public List<EmailAttachment> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		if(byteOffset == 0){ return fromBytes(bytes); }
		if(byteOffset >= bytes.length || byteOffset < 0){ throw new IllegalArgumentException("Illegal byte offset "
				+ byteOffset); }
		int newSize = bytes.length - byteOffset;
		byte[] newBytes = new byte[newSize];
		ArrayTool.copyInto(newBytes, bytes, byteOffset);
		return fromBytes(newBytes);
	}

	@SuppressWarnings("unchecked")
	public List<EmailAttachment> fromBytes(byte[] bytes){
		if ( bytes == null || bytes.length == 0 ) {
			return null;
		}
		Object o = SerializeTool.deserialize( bytes );
		try {
			return (List<EmailAttachment>) o;
		} catch ( Exception e ) {
			String msg = "Deserialized object has type " + o.getClass().getName();
			if ( o instanceof List ) {
				List<Object> list = (List<Object>) o;
				if ( list.size() > 0 ) {
					Object member = list.get(0);
					msg += "<" + member.getClass().getName() + ">";
				}
			}
			msg += " should be a List<EmailAttachment>";
			logger.error( msg );
			throw new IllegalArgumentException(msg, e);
		}
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		if (bytes == null) { return 0; }
		return bytes.length - byteOffset;
	}
	

	/*********************** SqlEncodedField ***********************/

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.LONGBLOB, Integer.MAX_VALUE , nullable, false);
	}

	@Override
	public List<EmailAttachment> parseJdbcValueButDoNotSet(Object col){
		throw new NotImplementedException("code needs testing");
	}
	
	@Override
	public List<EmailAttachment> fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			byte[] bytes = rs.getBytes(columnName);
			if(ArrayTool.isEmpty(bytes)){ return ListTool.create(); }
			return fromBytes(bytes);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, toBytes(this.value));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	public static class TestEmailAttachmentFields{
		private static EmailAttachment make1(String name, String bytes, String contentType){
			EmailAttachment result = new EmailAttachment();
			result.setName(name);
			if(bytes != null){
				result.setBytes(bytes.getBytes());
			}
			result.setContentType(contentType);
			return result;
		}

		@Test
		public void testSerialize(){
			EmailAttachment em1 = make1("Attach 1", "First data values or other here",
					EmailAttachment.CONTENT_TYPE_PLAIN_TEXT);
			EmailAttachment em2 = make1("Attach 2", "<html><body>Second lot of data values<body><html>",
					EmailAttachment.CONTENT_TYPE_HTML);
			EmailAttachmentArrayField testField = new EmailAttachmentArrayField("mytest", ListTool.create(em1, em2));
			byte[] bytes = testField.getBytes();
			List<EmailAttachment> results = testField.fromBytesButDoNotSet(bytes, 0);

			Assert.assertTrue( results.size() == 2 );
			for( int i = 0; i < results.size(); i++ ) {
				EmailAttachment atch = results.get( i );
				System.out.println( "Checking this EmailAttachment:\n" + atch.show());
				switch ( i) {
				case 0: Assert.assertTrue( em1.compareTo( atch ) == 0 ); break;
				case 1: Assert.assertTrue( em2.compareTo( atch ) == 0 ); break;
				default: Assert.fail( "There should only be two results" ); break;
				}
			}
		}
	}
}
