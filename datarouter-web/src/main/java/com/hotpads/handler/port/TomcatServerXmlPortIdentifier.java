package com.hotpads.handler.port;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

import com.hotpads.datarouter.config.ServletContextProvider;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.concurrent.Lazy;
import com.hotpads.util.core.io.RuntimeIOException;

@Singleton
public class TomcatServerXmlPortIdentifier implements PortIdentifier{

	@Singleton
	public static class TomcatServerXmlPortIdentifierProvider extends Lazy<TomcatServerXmlPortIdentifier>{

		@Inject
		private ServletContextProvider servletContextProvider;

		@Override
		protected TomcatServerXmlPortIdentifier load(){
			return new TomcatServerXmlPortIdentifier(servletContextProvider);
		}

	}

	private final Integer httpPort;
	private final Integer httpsPort;

	@Inject
	public TomcatServerXmlPortIdentifier(ServletContextProvider servletContextProvider){
		String severXmlPath = initServerXmlPath(servletContextProvider.get());
		httpPort = getHttpPortFromServerXml(severXmlPath);
		httpsPort = getRedirectPortFromServerXml(severXmlPath);
	}

	private Integer getHttpPortFromServerXml(String severXmlPath){
		String content = getServerXmlContent(severXmlPath);
		if(content != null){
			String stringToSearch = " port=\"";
			content = DrStringTool.getStringSurroundedWith(content, "<Service", "</Service>");
			String connectorTag = getConnectorTag(content, stringToSearch);
			String result = DrStringTool.getStringSurroundedWith(connectorTag, stringToSearch, "\"");

			if(!DrStringTool.isEmptyOrWhitespace(result) || DrStringTool.containsOnlyNumbers(result)){
				return new Integer(result);
			}
		}
		throw new RuntimeException("No HTTP Port has been identified from " + severXmlPath);
	}

	private Integer getRedirectPortFromServerXml(String severXmlPath){
		String content = getServerXmlContent(severXmlPath);
		if(content != null){
			String stringToSearch = " redirectPort=\"";
			content = DrStringTool.getStringSurroundedWith(content, "<Service", "</Service>");
			String connectorTag = getConnectorTag(content, stringToSearch);
			String result = DrStringTool.getStringSurroundedWith(connectorTag, stringToSearch, "\"");

			if(!DrStringTool.isEmptyOrWhitespace(result) || DrStringTool.containsOnlyNumbers(result)){
				return new Integer(result);
			}
		}
		throw new RuntimeException("No RedirectPort has been identified from " + severXmlPath);
	}

	private static String getConnectorTag(String content, String stringToSearch){
		String connectorTag = DrStringTool.getStringSurroundedWith(content, "<Connector", "/>");
		while(!DrStringTool.isEmptyOrWhitespace(connectorTag)
				&& (connectorTag.indexOf("compressableMimeType") < 0 || connectorTag.indexOf(stringToSearch) < 0)){
			content = content + "</Service>";
			connectorTag = DrStringTool.getStringSurroundedWith(content, connectorTag, "</Service>");
			connectorTag = DrStringTool.getStringSurroundedWith(connectorTag, "<Connector", "/>");
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

	private String initServerXmlPath(ServletContext servletContext){
		String webappsPath = servletContext.getRealPath("");
		String confPath = webappsPath + "/../../conf";
		String severXmlPath = confPath + "/server.xml";
		boolean serverXmlExists = new File(severXmlPath).exists();
		if(serverXmlExists){
			return severXmlPath;
		}
		throw new RuntimeException("No server.xml has been found at " + severXmlPath);
	}

	@Override
	public Integer getHttpPort(){
		return httpPort;
	}

	@Override
	public Integer getHttpsPort(){
		return httpsPort;
	}

}
