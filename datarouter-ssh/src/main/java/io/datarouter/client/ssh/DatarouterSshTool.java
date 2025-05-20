/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import io.datarouter.client.ssh.request.SshExecSessionRequest;
import io.datarouter.client.ssh.request.SshSessionConfig;
import io.datarouter.client.ssh.request.SshSftpGetSessionRequest;
import io.datarouter.client.ssh.request.SshSftpPutSessionRequest;
import io.datarouter.util.concurrent.ThreadTool;

public class DatarouterSshTool{

	private static final Map<String,JSch> CLIENT_BY_RSA_KEY = new HashMap<>();
	private static final String SERVER_HOST_KEY_CONFIG = "server_host_key",
			PUBKEY_ACCEPTED_ALGORITHMS_CONFIG = "PubkeyAcceptedKeyTypes",
			KEX_CONFIG = "kex",
			DEPRECATED_KEY_ALGORITHMS = ",ssh-rsa,ssh-dss",
			DEPRECATED_KEY_EXCHANGE_ALGORITHMS =
					",diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group1-sha1";

	public static Process process(String rsaPrivateKey, SshExecSessionRequest request){
		SessionAndChannel<ChannelExec> sessionAndChannel = exec(rsaPrivateKey, request);
		Session session = sessionAndChannel.session();
		ChannelExec channel = sessionAndChannel.channel();

		return new Process(){

			@Override
			public int waitFor(){
				while(!channel.isClosed()){
					ThreadTool.sleepUnchecked(100);
				}
				return channel.getExitStatus();
			}

			@Override
			public OutputStream getOutputStream(){
				try{
					return channel.getOutputStream();
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}

			@Override
			public InputStream getInputStream(){
				try{
					return channel.getInputStream();
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}

			@Override
			public InputStream getErrorStream(){
				try{
					return channel.getErrStream();
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}

			@Override
			public int exitValue(){
				if(!channel.isClosed()){
					throw new IllegalThreadStateException("Not done");
				}
				return channel.getExitStatus();
			}

			@Override
			public void destroy(){
				channel.disconnect();
				session.disconnect();
			}

		};
	}

	public static void sftp(String rsaPrivateKey, SshSftpPutSessionRequest request){
		SessionAndChannel<ChannelSftp> sessionAndChannel = sftp(rsaPrivateKey, request.config());
		Session session = sessionAndChannel.session();
		ChannelSftp sftp = sessionAndChannel.channel();
		try{
			sftp.put(request.inputStream(), request.destinationFile());
		}catch(SftpException e){
			throw new RuntimeException(e);
		}finally{
			sftp.exit();
			session.disconnect();
		}
	}

	public static void sftp(String rsaPrivateKey, SshSftpGetSessionRequest request){
		SessionAndChannel<ChannelSftp> sessionAndChannel = sftp(rsaPrivateKey, request.config());
		Session session = sessionAndChannel.session();
		ChannelSftp sftp = sessionAndChannel.channel();
		try{
			sftp.get(request.remoteFilePath(), request.outputFileStream());
		}catch(SftpException e){
			throw new RuntimeException(e);
		}finally{
			sftp.exit();
			session.disconnect();
		}
	}

	public static SessionAndChannel<ChannelSftp> sftp(String rsaPrivateKey, SshSessionConfig request){
		return connectSessionAndStartChannel(
				"sftp",
				rsaPrivateKey,
				request,
				(ChannelSftp _) -> {});
	}

	public static SessionAndChannel<ChannelExec> exec(String rsaPrivateKey, SshExecSessionRequest request){
		return connectSessionAndStartChannel(
				"exec",
				rsaPrivateKey,
				request.config(),
				(ChannelExec exec) -> exec.setCommand(String.join(" ", request.command())));
	}

	public static ShellSshSessionRunner shell(String rsaPrivateKey, SshSessionConfig config) throws IOException{
		SessionAndChannel<ChannelShell> sessionAndChannel = connectSessionAndStartChannel(
				"shell",
				rsaPrivateKey,
				config,
				_ -> {});
		return new ShellSshSessionRunner(sessionAndChannel.session, sessionAndChannel.channel);
	}

	public static DatarouterSshSession startSession(String rsaPrivateKey, SshSessionConfig request){
		return new DatarouterSshSession(request, sessionConnect(rsaPrivateKey, request));
	}

	public static <T extends Channel> SessionAndChannel<T> connectSessionAndStartChannel(
			String channelName,
			String rsaPrivateKey,
			SshSessionConfig request,
			Consumer<T> beforeConnect){
		int connectTimeout = (int)request.connectTimeout().toMillis();
		Session session = sessionConnect(rsaPrivateKey, request);
		try{
			@SuppressWarnings("unchecked")
			T channel = (T)session.openChannel(channelName);
			beforeConnect.accept(channel);
			channel.connect(connectTimeout);
			request.listener().onChannelConnect();
			return new SessionAndChannel<>(session, channel);
		}catch(Exception e){
			session.disconnect();
			throw new RuntimeException(e);
		}
	}

	private static Session sessionConnect(
			String rsaPrivateKey,
			SshSessionConfig request){
		int connectTimeout = (int)request.connectTimeout().toMillis();
		try{
			JSch jsch = getJSch(rsaPrivateKey);
			Session session = jsch.getSession(request.username(), request.host(), request.port());
			session.connect(connectTimeout);
			request.listener().onSessionConnect();
			return session;
		}catch(JSchException e){
			throw new RuntimeException(e);
		}
	}

	public static synchronized JSch getJSch(String rsaPrivateKey) throws JSchException{
		if(CLIENT_BY_RSA_KEY.containsKey(rsaPrivateKey)){
			return CLIENT_BY_RSA_KEY.get(rsaPrivateKey);
		}
		var jsch = new JSch();
		jsch.addIdentity(rsaPrivateKey, rsaPrivateKey.getBytes(), null, null);
		CLIENT_BY_RSA_KEY.put(rsaPrivateKey, jsch);
		return jsch;
	}

	public record SessionAndChannel<T extends Channel>(
			Session session,
			T channel){
	}

	public interface SshSessionListener{

		void onSessionConnect();
		void onChannelConnect();

		SshSessionListener NO_OP = new SshSessionListener(){
			@Override
			public void onChannelConnect(){
			}

			@Override
			public void onSessionConnect(){
			}
		};

	}

	@Deprecated
	public static void supportDeprecatedSignatureAlgorithms(Session session){
		session.setConfig(SERVER_HOST_KEY_CONFIG,
				session.getConfig(SERVER_HOST_KEY_CONFIG) + DEPRECATED_KEY_ALGORITHMS);
		session.setConfig(PUBKEY_ACCEPTED_ALGORITHMS_CONFIG,
				session.getConfig(PUBKEY_ACCEPTED_ALGORITHMS_CONFIG) + DEPRECATED_KEY_ALGORITHMS);
		session.setConfig(KEX_CONFIG, session.getConfig(KEX_CONFIG) + DEPRECATED_KEY_EXCHANGE_ALGORITHMS);
	}

}
