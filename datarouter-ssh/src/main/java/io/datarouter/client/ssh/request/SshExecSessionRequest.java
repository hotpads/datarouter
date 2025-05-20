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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.scanner.Scanner;

public record SshExecSessionRequest(
		SshSessionConfig config,
		String[] command){

	public SshExecSessionRequest(SshSessionConfig config, SshExecCommandBuilder command){
		this(config, command.build());
	}

	public static class SshExecCommandBuilder{

		private final List<String> parts = new ArrayList<>();
		private boolean isLoginShell;
		private final List<String> obfuscatedTokens = new ArrayList<>();

		public SshExecCommandBuilder asLoginShell(){
			this.isLoginShell = true;
			return this;
		}

		public SshExecCommandBuilder append(String... parts){
			this.parts.addAll(Arrays.asList(parts));
			return this;
		}

		public SshExecCommandBuilder condAppend(boolean condition, String part){
			if(condition){
				return append(part);
			}
			return this;
		}

		public SshExecCommandBuilder obfuscate(String obfuscatedToken){
			this.obfuscatedTokens.add(obfuscatedToken);
			return this;
		}

		public String display(){
			String display = String.join(" ", build());
			for(String obfuscatedToken : obfuscatedTokens){
				display = display.replace(obfuscatedToken, "x".repeat(obfuscatedToken.length()));
			}
			return display;
		}

		public String[] build(){
			if(isLoginShell){
				String wrapped = Scanner.of(parts)
						.map(part -> part.replace("\"", "\\\""))
						.collect(Collectors.joining(" ", "\"", "\""));

				return new String[]{"bash", "-l", "-c", wrapped};
			}
			return parts.toArray(EmptyArray.STRING);
		}

	}

}
