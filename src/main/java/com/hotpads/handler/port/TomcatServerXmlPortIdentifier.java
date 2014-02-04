package com.hotpads.handler.port;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Singleton;
import javax.servlet.ServletContext;

import com.google.inject.Provider;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.io.RuntimeIOException;

@Singleton 
public class TomcatServerXmlPortIdentifier implements PortIdentifier{
	
	public static class TomcatServerXmlPortIdentifierProvider implements Provider<TomcatServerXmlPortIdentifier>{
		private ServletContext servletContext;
		
		public TomcatServerXmlPortIdentifierProvider(ServletContext servletContext){
			this.servletContext = servletContext;
		}
		
		@Override
		public TomcatServerXmlPortIdentifier get(){
			return new TomcatServerXmlPortIdentifier(servletContext);
		}
	}

	
	/************ fields **********************************/
	
	private ServletContext servletContext;
	private Integer httpPort;
	private Integer httpsPort;
	private String severXmlPath;

	public TomcatServerXmlPortIdentifier(ServletContext servletContext){
		this.servletContext = servletContext;
		severXmlPath = initServerXmlPath();
		this.httpPort = getHttpPortFromServerXml();
		this.httpsPort = getRedirectPortFromServerXml();
	}

	/**************** PortIdentifier *********************/

	private Integer getHttpPortFromServerXml(){
		String content = getServerXmlContent(severXmlPath);
		if(content != null){
			String stringToSearch = " port=\"";
			content = StringTool.getStringSurroundedWith(content, "<Service", "</Service>");
			String connectorTag = getConnectorTag(content, stringToSearch);
			String result = StringTool.getStringSurroundedWith(connectorTag, stringToSearch, "\"");

			if(!StringTool.isEmptyOrWhitespace(result) || StringTool.containsOnlyNumbers(result)){ return new Integer(
					result); }
		}
		throw new RuntimeException("No HTTP Port has been identified from " + severXmlPath);
	}

	private Integer getRedirectPortFromServerXml(){
		String content = getServerXmlContent(severXmlPath);
		if(content != null){
			String stringToSearch = " redirectPort=\"";
			content = StringTool.getStringSurroundedWith(content, "<Service", "</Service>");
			String connectorTag = getConnectorTag(content, stringToSearch);
			String result = StringTool.getStringSurroundedWith(connectorTag, stringToSearch, "\"");

			if(!StringTool.isEmptyOrWhitespace(result) || StringTool.containsOnlyNumbers(result)){ return new Integer(
					result); }
		}
		throw new RuntimeException("No RedirectPort has been identified from " + severXmlPath);
	}

	private static String getConnectorTag(String content, String stringToSearch){
		String connectorTag = StringTool.getStringSurroundedWith(content, "<Connector", "/>");
		while(!StringTool.isEmptyOrWhitespace(connectorTag)
				&& (connectorTag.indexOf("compressableMimeType") < 0 || connectorTag.indexOf(stringToSearch) < 0)){
			content = content + "</Service>";
			connectorTag = StringTool.getStringSurroundedWith(content, connectorTag, "</Service>");
			connectorTag = StringTool.getStringSurroundedWith(connectorTag, "<Connector", "/>");
		}
		return connectorTag;

	}

	private static String getServerXmlContent(String serverXmlPath){
		byte[] encoded;
		try{
			encoded = Files.readAllBytes(Paths.get(serverXmlPath));
		}catch(IOException e){
			throw new RuntimeIOException(e);
		}
		Charset encoding = Charset.defaultCharset();
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();

	}

	private String initServerXmlPath(){
		String webappsPath = servletContext.getRealPath("");
		String confPath = webappsPath + "/../../conf";
		severXmlPath = confPath + "/server.xml";
		boolean serverXmlExists = new File(severXmlPath).exists();
		if(serverXmlExists){ return severXmlPath; }
		throw new RuntimeException("No server.xml has been found at " + severXmlPath);
	}
	
	public Integer getHttpPort(){
		return httpPort;
	}

	@Override
	public Integer getHttpsPort(){
		return httpsPort;
	}
}
