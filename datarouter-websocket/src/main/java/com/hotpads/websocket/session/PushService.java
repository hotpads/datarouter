package com.hotpads.websocket.session;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.util.core.collections.KeyRangeTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.request.HotPadsHttpRequest.HttpRequestMethod;
import com.hotpads.util.http.response.HotPadsHttpResponse;

@Singleton
public class PushService{
	private static final Logger logger = LoggerFactory.getLogger(PushService.class);

	public static final String PUSH_SERVICE_HTTPCLIENT = "pushServiceHtpClient";

	private final HotPadsHttpClient httpClient;
	private final SortedMapStorageNode<WebSocketSessionKey,WebSocketSession> webSocketSessionNode;

	@Inject
	public PushService(@Named(PUSH_SERVICE_HTTPCLIENT) HotPadsHttpClient httpClient,
			WebSocketSessionNodeProvider webSocketSessionNodeProvider){
		this.httpClient = httpClient;
		this.webSocketSessionNode = webSocketSessionNodeProvider.get();
	}

	public void register(WebSocketSession webSocketSession){
		webSocketSessionNode.put(webSocketSession, null);
	}

	public void unregister(WebSocketSessionKey webSocketSessionKey){
		webSocketSessionNode.delete(webSocketSessionKey, null);
	}

	//TODO Optimization: don't do the http call if the socket is open on the current server
	public void forwardToAll(String userToken, String message){
		WebSocketSessionKey prefix = new WebSocketSessionKey(userToken);
		Range<WebSocketSessionKey> range = new Range<>(prefix, true, prefix, true);
		Iterable<WebSocketSession> scan = webSocketSessionNode.scan(range, null);
		for(WebSocketSession webSocketSession : scan){
			HotPadsHttpResponse response = executeCommand(WebSocketCommandName.PUSH, webSocketSession, message);
			boolean success = Boolean.parseBoolean(response.getEntity());
			if(!success){
				logger.error("Forwarding to {} failed: deleting the session", webSocketSession);
				unregister(webSocketSession.getKey());
			}
		}
	}

	public long getNumberOfSession(String userToken){
		WebSocketSessionKey prefix = new WebSocketSessionKey(userToken);
		return webSocketSessionNode.streamKeys(KeyRangeTool.forPrefix(prefix), null).count();
	}

	//TODO Optimization: don't do the http call if the socket is open on the current server
	public boolean isAlive(WebSocketSession webSocketSession){
		HotPadsHttpResponse response = executeCommand(WebSocketCommandName.IS_ALIVE, webSocketSession, null);
		return Boolean.parseBoolean(response.getEntity());
	}

	private HotPadsHttpResponse executeCommand(WebSocketCommandName webSocketCommandName,
			WebSocketSession webSocketSession, String message){
		String url = "http://" + webSocketSession.getServerName() + WebSocketApiDispatcher.WEBSOCKET_COMMAND + "/"
				+ webSocketCommandName.getPath();
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.POST, url, false);
		WebSocketCommand webSocketCommand = new WebSocketCommand(webSocketSession.getKey(), message);
		httpClient.addDtoToPayload(request, webSocketCommand, null);
		return httpClient.execute(request);
	}

}
