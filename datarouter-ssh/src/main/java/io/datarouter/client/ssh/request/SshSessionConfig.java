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
package io.datarouter.client.ssh.request;

import java.time.Duration;

import io.datarouter.client.ssh.DatarouterSshTool.SshSessionListener;

public record SshSessionConfig(
		String host,
		String username,
		int port,
		Duration connectTimeout,
		Duration waitForTimeout,
		SshSessionListener listener){

	public static Builder startBuilder(String host){
		return new Builder(host);
	}

	public static class Builder{

		private final String host;
		private String username = null;
		private int port = 22;
		private Duration connectTimeout = Duration.ZERO;
		private Duration waitForTimeout = Duration.ZERO;
		private SshSessionListener listener = SshSessionListener.NO_OP;

		private Builder(String host){
			this.host = host;
		}

		public Builder withUsername(String username){
			this.username = username;
			return this;
		}

		public Builder withPort(int port){
			this.port = port;
			return this;
		}

		public Builder withConnectTimeout(Duration connectTimeout){
			this.connectTimeout = connectTimeout;
			return this;
		}

		public Builder withWaitForTimeout(Duration waitForTimeout){
			this.waitForTimeout = waitForTimeout;
			return this;
		}

		public Builder withListener(SshSessionListener listener){
			this.listener = listener;
			return this;
		}

		public SshSessionConfig build(){
			return new SshSessionConfig(host, username, port, connectTimeout, waitForTimeout, listener);
		}

	}

}
