package com.hotpads.util.http.security;

import java.util.HashSet;
import java.util.Set;

public enum UrlScheme {
	HTTP("http"),
	HTTPS("https"),
	ANY("any");

	public static final String LOCAL_HOST = "localhost";
	public static final String DOMAIN_NAME = "hotpads.com";

	public static final int
		PORT_HTTP_STANDARD = 80,
		PORT_HTTPS_STANDARD = 443,
		PORT_HTTP_DEV = 8080,
		PORT_HTTPS_DEV = 8443;

	public static final String LOCAL_DEV_SERVER = LOCAL_HOST + ":" + PORT_HTTP_DEV;
	public static final String LOCAL_DEV_SERVER_HTTPS = LOCAL_HOST + ":" + PORT_HTTPS_DEV;
	public static final String LOCAL_DEV_SERVER_URL = HTTP.stringRepresentation +"://" + LOCAL_HOST + ":"
			+ PORT_HTTP_DEV;
	public static final String LOCAL_DEV_SERVER_HTTPS_URL = HTTPS.stringRepresentation + "://" + LOCAL_HOST + ":"
			+ PORT_HTTPS_DEV;

	private final String stringRepresentation;

	private UrlScheme(String stringRepresentation){
		this.stringRepresentation = stringRepresentation;
	}

	public String getStringRepresentation() {
		return stringRepresentation;
	}


	protected static Set<Integer> STANDARD_PORTS = new HashSet<>();
	static{
		STANDARD_PORTS.add(PORT_HTTP_STANDARD);
		STANDARD_PORTS.add(PORT_HTTPS_STANDARD);
	}

}