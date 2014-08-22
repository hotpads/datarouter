package com.hotpads.wal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownServiceException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.util.wal.WalPosition;
import com.hotpads.util.wal.WalSubscriber;
import com.hotpads.util.wal.imp.BaseWalSubscriber;

public class HttpRelaySubscriber extends BaseWalSubscriber{
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	/************************** fields *****************************/
	
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
		logger.debug("start run method of the subscriber ");
		
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
			logger.debug("The response code received is : "+responseCode);
			if(responseCode != 200 && responseCode != 303) {
				throw new UnknownServiceException("Received "+responseCode+" response.");
			}
			InputStream in = post.getResponseBodyAsStream();
			
			 BufferedReader reader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
		      
				StringBuilder response = new StringBuilder();
		        String line;
		        while ((line = reader.readLine()) != null) {
		            response.append(line);
		        }
		        logger.debug("the response received from the post method is : " +response);
		        reader.close();
		        post.releaseConnection();
		}catch(IOException e){
			logger.error("", e);
		}
		
		logger.debug("Finished appending the messages to the target wal");
		//logPosition = subscriptionManager.getManagedWal().getHead();
		logger.debug("the new logPosition of the RelaySubscriber is :" + logPosition);
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
