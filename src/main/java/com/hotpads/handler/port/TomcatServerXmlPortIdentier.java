package com.hotpads.handler.port;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Singleton;
import javax.servlet.ServletContext;

import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.io.RuntimeIOException;

@Singleton
public class TomcatServerXmlPortIdentier implements PortIdentifier{

	private ServletContext servletContext;
	private Integer httpPort;
	private Integer httpsPort;

	public TomcatServerXmlPortIdentier(ServletContext servletContext){
		this.servletContext = servletContext;
		this.httpPort = initHttpPort();
		this.httpsPort = initHttpsPort();
	}
	
	/**************** PortIdentifier *********************/
	
	public Integer getHttpPort(){
		return httpPort;
	}
	
	@Override
	public Integer getHttpsPort(){
		return httpsPort;
	}
	
	private Integer initHttpPort(){
		String severXmlPath;
		String webappsPath = servletContext.getRealPath("");
		String confPath = webappsPath + "/../../conf";
		severXmlPath = confPath + "/server.xml";
		boolean serverXmlExists = new File(severXmlPath).exists();
		if(serverXmlExists){
			return getHttpPortFromServerXml(severXmlPath);
		}else{
			return null;
		}
	}
	

	private static Integer getHttpPortFromServerXml(String severXmlPath){
		String content = getServerXmlContent(severXmlPath);
		if(content != null){
			//End the search at RediectPort hoping that the port is defined before
			String stringToSearch="redirectPort=\"";
			int index = content.indexOf(stringToSearch);
			//Get the connector line where redirectPort and Port are defined
			String result = content.substring(0, index);
			int lastIndexConnector = result.lastIndexOf("Connector");
			result = result.substring(lastIndexConnector);
			//Search for the port value
			stringToSearch = "port=\"";
			index = result.indexOf(stringToSearch);
			result = result.substring(index+stringToSearch.length());
			result = result.substring(0, result.indexOf("\""));

			if(StringTool.isEmptyOrWhitespace(result) || !StringTool.containsOnlyNumbers(result)){
				throw new RuntimeException("No HTTP Port has been identified in the server.xml of this tomcat instance");
			}
			return new Integer(result);
		}
		return null;
	}

	private Integer initHttpsPort(){
		String severXmlPath;
		String webappsPath = servletContext.getRealPath("");
		String confPath = webappsPath + "/../../conf";
		severXmlPath = confPath + "/server.xml";
		boolean serverXmlExists = new File(severXmlPath).exists();
		if(serverXmlExists){
			return getRedirectPortFromServerXml(severXmlPath);
		}else{
			return null;
		}
	}

	private  Integer getRedirectPortFromServerXml(String severXmlPath){
		String content = getServerXmlContent(severXmlPath);
	
		if(content != null){
			String stringToSearch="redirectPort=\"";
			int index = content.indexOf(stringToSearch);
			String result = content.substring(index + stringToSearch.length());
			result = result.substring(0, result.indexOf("\""));
			if(StringTool.isEmptyOrWhitespace(result) || !StringTool.containsOnlyNumbers(result)){
				throw new RuntimeException("No RedirectPort has been identified in the server.xml of this tomcat instance");
			}
			return new Integer(result);
		}
		return null;
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

	public static void main(String[] args){
		System.out.println(getHttpPortFromServerXml("/hpdev/tomcat/conf/server.xml"));
	}
}
