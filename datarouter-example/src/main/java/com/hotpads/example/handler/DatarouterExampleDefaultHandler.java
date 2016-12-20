package com.hotpads.example.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;

public class DatarouterExampleDefaultHandler extends BaseHandler{

	@Handler(defaultHandler = true)
	protected Mav showHostname() throws UnknownHostException{
		String username = System.getProperty("user.name");
		String hostname = InetAddress.getLocalHost().getHostName();
		return new MessageMav(hostname + ", " + username);
	}

}