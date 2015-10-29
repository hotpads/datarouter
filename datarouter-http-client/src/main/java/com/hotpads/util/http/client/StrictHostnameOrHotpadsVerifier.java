package com.hotpads.util.http.client;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;

public class StrictHostnameOrHotpadsVerifier implements HostnameVerifier{

	private HostnameVerifier defaultHostnameVerifier;

	public StrictHostnameOrHotpadsVerifier(){
		this.defaultHostnameVerifier = new DefaultHostnameVerifier();
	}

	@Override
    public final String toString() {
        return "STRICT_OR_HOTPADS";
    }

	@Override
	public boolean verify(String host, SSLSession session){
		if(!"hotpads.com".equals(host) && !host.endsWith(".hotpads.com")){
			return defaultHostnameVerifier.verify(host, session);
		}
		return true;
	}

}