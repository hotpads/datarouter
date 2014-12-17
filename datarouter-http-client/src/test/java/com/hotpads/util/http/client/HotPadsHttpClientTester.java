package com.hotpads.util.http.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.request.HotPadsHttpRequest.HttpRequestMethod;
import com.hotpads.util.http.response.exception.HotPadsHttpException;

public class HotPadsHttpClientTester {
	
	private static final int PORT = 8192;
	private static final String URL = "http://localhost:" + PORT + "/";
	private final HotPadsHttpClient client = new HotPadsHttpClientBuilder().build();
	
	private static final class ServSocket extends Thread {
		private Socket sock;
		public void run() {
			try {
				System.out.println("opening socket on port " + PORT);
				sock = new ServerSocket(PORT).accept();
				System.out.println("successfully opened");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void shutdown() {
			if(!sock.isClosed()) {
				try {
					System.out.println("closing socket on port " + PORT);
					sock.close();
					System.out.println("successfully closed");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static final ServSocket SOCKET = new ServSocket();
	
	@Before
	public void setUp() {
		SOCKET.start();
	}
	
	@Test(expected=HotPadsHttpException.class)
	public void testRemoteRuntimeException() {
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.POST, URL, false);
		client.execute(request);
	}
	
	@After
	public void tearDown() {
		SOCKET.shutdown();
		try {
			SOCKET.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
