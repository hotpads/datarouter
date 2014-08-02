package com.hotpads.email.generic;

import java.io.Serializable;



public class EmailAttachment implements Serializable, Comparable<EmailAttachment> {

	private static final long serialVersionUID = 1L;

	public static final String CONTENT_TYPE_PLAIN_TEXT = "text/plain";
	public static final String CONTENT_TYPE_HTML = "text/html";
	public static final String CONTENT_TYPE_VCARD = "text/vcard";
	public EmailAttachment() {
	}
	public EmailAttachment(String name, byte[] bytes, String contentType) {
		this.name = name;
		this.bytes = bytes;
		this.contentType = contentType;
	}
	
    private String name;
	private byte[] bytes;
	private String contentType;

	public String getName(){
		return name;
	}
	public byte[] getBytes(){
		return bytes;
	}
	public String getContentType(){
		return contentType;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setBytes(byte[] bytes){
		this.bytes = bytes;
	}
	public void setContentType(String contentType){
		this.contentType = contentType;
	}
	
	@Override
	public int compareTo(EmailAttachment o){
		if ( o == null ) {
			return 1;
		}
		int result = compare( name, o.name );
		if ( result != 0 ) {
		    return result;
		}
		result = compare( contentType, o.contentType );
		if ( result != 0 ) {
		    return result;
		}
		return compare( bytes, o.bytes );
	}
		
	private int compare( String a, String b ) {
		if ( a == null ) return ( b == null ? 0 : -1 );
		if ( b == null ) return 1;
		return a.compareTo( b );
	}
	
	private int compare( byte[] a, byte[] b ) {
		if ( a == null ) return ( b == null ? 0 : -1 );
		if ( b == null ) return 1;
		int result = compare(a.length,b.length );
		if ( result != 0 ) return result;
		for ( int i = 0; i < a.length; i++ ) {
			result = compare( a[i], b[i] );
			if ( result != 0 ) {
				return result;
			}
		}
		return 0;
	}
	
	private int compare( int a, int b ) {
	    if ( a == b ) return 0;
	    return a < b ? -1: 1;
	}
	
	public String show() {
		StringBuilder sb = new StringBuilder();
		sb.append( "{ name=").append( name == null ? "null" :name );
		sb.append( "\n bytes=[");
		if ( bytes == null ) {
		 	sb.append( "null!" );
		} else {
			int n = 0;
			for ( byte b : bytes ) {
				n++;
				if ( n >= 100 ) {
					sb.append( "..etc.." );
					break;
				}
				if ( ' ' <= b && b <= '~' ) {
					sb.append( (char) b );
				} else {
					sb.append("{").append( (int) b ).append("}");
				}
			}
		}
		sb.append( "]\n contentType=").append( contentType == null ? "null" :contentType ).append("\n}");
		return sb.toString();
	}
}

