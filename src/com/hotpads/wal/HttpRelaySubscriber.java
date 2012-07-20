package com.hotpads.wal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownServiceException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.hotpads.util.core.map.GridTool;
import com.hotpads.util.wal.WalMessage;
import com.hotpads.util.wal.WalPosition;
import com.hotpads.util.wal.WalSubscriber;
import com.hotpads.util.wal.imp.BaseWalSubscriber;

public class HttpRelaySubscriber extends BaseWalSubscriber{
	
	/************************** fields *****************************/
	protected static final Log logger = LogFactory.getLog(HttpRelaySubscriber.class);
	protected String destinationUrl;
	//http://hc.apache.org/httpclient-3.x/apidocs/org/apache/commons/httpclient/HttpClient.html
	protected HttpClient client;
	protected WalPosition logPosition;
	protected long WalStartPosition;
	protected static HttpClient httpClient;
	static {
		MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setConnectionTimeout(1000);
		params.setSoTimeout(3000);
		params.setDefaultMaxConnectionsPerHost(25);
		params.setMaxTotalConnections(50);
		manager.setParams(params);
		httpClient = new HttpClient(manager); 		
	}
	
	
	/************************** constructors *****************************/
	
	public HttpRelaySubscriber(String destinationUrl, WalPosition logPosition){
		this.destinationUrl = destinationUrl;
		
		//see TextIndexRemoteCall for example setup
		//this.client = new HttpConnection(destinationUrl);

		this.logPosition = logPosition;
	}

	
	/************************** flush ******************************************/

	@Override
	public void run(){
		//send to remote host and return whether it was successful or not
		System.out.println("start run method of the subscriber ");
		
		//List<WalMessage> messages = getNewMessages();
		//if(messages.size()<1){ return ; }
		//System.out.println("Started sending the messages to the destination url " +messages.size());
		// Send the messages to the destinationUrl
		// Check they have been received
		GetMethod post = new GetMethod(destinationUrl);
		NameValuePair[] data = {
				new NameValuePair("messages","NOcUZkbWWH/nSUONlqizVTenjcUNFjkBCz8UlblcR72yoOQq2qF+lgQOG3p93EbIdl5ZZKE="),
				//new NameValuePair("Passwd","blablabla"), // don't use it ;p 
		};
		//post.setRequestBody(data);
		
		try{
			int responseCode = httpClient.executeMethod(post);
			System.out.println("The response code received is : "+responseCode);
			if(responseCode != 200 && responseCode != 303) 
				throw new UnknownServiceException("Received "+responseCode+" response.");
			InputStream in = post.getResponseBodyAsStream();
			
			 BufferedReader reader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
		      
				StringBuilder response = new StringBuilder();
		        String line;
		        while ((line = reader.readLine()) != null) {
		            response.append(line);
		        }
		        System.out.println("the response received from the post method is : " +response);
		        reader.close();
		        post.releaseConnection();
		}catch(HttpException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		System.out.println("Finished appending the messages to the target wal");
		//logPosition = subscriptionManager.getManagedWal().getHead();
		System.out.println("the new logPosition of the RelaySubscriber is :" + logPosition);
	}
	
	@Override
	public boolean isOlder(WalSubscriber oldest){
		if(oldest==null){ return true;}
		return logPosition.isOlder(oldest.getLogPosition());
	}
	

	/*************************************** Getters & Setters *******************************************/
	
	@Override
	public WalPosition getLogPosition(){
		return logPosition;
	}

	public static class HttpRelaySubscriberTest{
		
		@Test
		public void testHttpsubscrieber(){
			HttpRelaySubscriber subscriber = new HttpRelaySubscriber("http://localhost:8080/job/wal/test1", null);
			subscriber.run();
		}
		
	}
}
