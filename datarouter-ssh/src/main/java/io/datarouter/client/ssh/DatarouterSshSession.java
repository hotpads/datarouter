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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import io.datarouter.client.ssh.request.SshSessionConfig;
import io.datarouter.util.concurrent.ThreadTool;

public class DatarouterSshSession implements AutoCloseable{

	private final SshSessionConfig request;
	private final Session session;

	public DatarouterSshSession(SshSessionConfig request, Session session){
		this.request = request;
		this.session = session;
	}

	public ExecProcess exec(String... command){
		try{
			ChannelExec channel = (ChannelExec)session.openChannel("exec");
			try{
				channel.setCommand(String.join(" ", command));
				channel.connect();
				request.listener().onChannelConnect();
				return new ExecProcess(channel);
			}catch(Exception e){
				channel.disconnect();
				throw e;
			}
		}catch(JSchException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close(){
		session.disconnect();
	}

	public static class ExecProcess extends Process implements AutoCloseable{

		private final ChannelExec channel;
		private final OutputStream outputStream;
		private final InputStream inputStream;
		private final InputStream errorStream;

		public ExecProcess(ChannelExec channel){
			this.channel = channel;
			try{
				this.outputStream = channel.getOutputStream();
				this.inputStream = channel.getInputStream();
				this.errorStream = channel.getErrStream();
			}catch(IOException e){
				throw new RuntimeException(e);
			}
		}

		@Override
		public int waitFor() throws InterruptedException{
			try{
				while(!channel.isClosed() || inputStream.available() > 0 || errorStream.available() > 0){
					ThreadTool.sleep(100);
				}
			}catch(IOException e){
				throw new RuntimeException(e);
			}
			return channel.getExitStatus();
		}

		@Override
		public OutputStream getOutputStream(){
			return outputStream;
		}

		@Override
		public InputStream getInputStream(){
			return inputStream;
		}

		@Override
		public InputStream getErrorStream(){
			return errorStream;
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
		}

		@Override
		public void close(){
			destroy();
		}

	}

}
