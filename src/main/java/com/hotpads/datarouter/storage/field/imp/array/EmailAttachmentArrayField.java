package com.hotpads.datarouter.storage.field.imp.array;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.email.generic.EmailAttachment;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.exception.NotImplementedException;
import com.hotpads.util.core.logger.CustomLoggerTool;

public class EmailAttachmentArrayField extends BaseListField<EmailAttachment,List<EmailAttachment>>{
    private static final Logger logger = CustomLoggerTool.getValidLogger("jdbc", EmailAttachmentArrayField.class);
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
			try {
				return new String( bytes, "UTF-8" );
			} catch ( UnsupportedEncodingException e ) {
				logger.warn( "Illegal encoding " + e.getMessage() );
			}
		}
		return null;
	}
	
	@Override
	public List<EmailAttachment> parseStringEncodedValueButDoNotSet(String s){
		if ( s == null ) {
			return null;
		}
		byte[] bytes = s.getBytes();
		return fromBytes(bytes);
	}
	

	/*********************** ByteEncodedField ***********************/
	
	@Override
	public byte[] getBytes(){
		return toBytes(value);
	}

	public static byte[] toBytes( List<EmailAttachment> attachments ) {
		if(CollectionTool.isEmpty( attachments )){
			return null;
		}
		return serialize( attachments );
	}
	
	private static byte[] serialize(Object o) {
		assert o != null;
		byte[] rv=null;
		try {
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			ObjectOutputStream os=new ObjectOutputStream(bos);
			os.writeObject(o);
			os.close();
			bos.close();
			rv=bos.toByteArray();
		} catch(IOException e) {
			throw new IllegalArgumentException("Non-serializable object", e);
		}
		return rv;
	}

	private static Object deserialize(byte[] in) {
		Object rv=null;
		assert in != null;
		try {
			ByteArrayInputStream bis=new ByteArrayInputStream(in);
			ObjectInputStream is=new ObjectInputStream(bis);
			rv=is.readObject();
			is.close();
			bis.close();
		} catch(IOException e) {
			logger.warn("Caught IOException decoding " + in.length + " bytes of data", e);
		} catch (ClassNotFoundException e) {
			logger.warn("Caught ClassNotFoundException decoding " + in.length + " bytes of data", e);
		}
		return rv;
	}
	
	
	@Override
	public List<EmailAttachment> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		if ( byteOffset == 0 )
			return fromBytes(bytes);
		if ( byteOffset >= bytes.length || byteOffset < 0) return null;
		int newSize = bytes.length - byteOffset;
		byte[] newBytes = new byte[ newSize ];
		for (int i = 0; i < newSize; i++ ) {
			newBytes[i] = bytes[byteOffset+i];
		}
		return fromBytes(newBytes);
	}

	@SuppressWarnings("unchecked")
	public List<EmailAttachment> fromBytes(byte[] bytes){
		if ( bytes == null || bytes.length == 0 ) {
			return null;
		}
		Object o = deserialize( bytes );
		try {
			return (List<EmailAttachment>) o;
		} catch ( Exception e ) {
			String type = o.getClass().getName();
			if ( o instanceof List ) {
				List<Object> list = (List<Object>) o;
				if ( list.size() > 0 ) {
					Object member = list.get(0);
					type += "<" + member.getClass().getName() + ">";
				}
			}
			logger.warn( "Returned object of type " + type + " is not a List<EmailAttachment>" );
		}
		return null;
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		// TODO Auto-generated method stub
		return 0;
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
