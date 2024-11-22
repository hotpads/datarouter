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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import io.datarouter.client.ssh.exception.BadExitCodeException;
import io.datarouter.types.MilliTime;

// TODO decide whether to keep this
public class ShellSshSessionRunner implements AutoCloseable{
	private static final Logger logger = LoggerFactory.getLogger(ShellSshSessionRunner.class);

	private static final String COMMAND_IDENTIFIER_PREFIX = "COMMAND_",
			CODE_CHECK_PREFIX = "COMMAND_FINISHED_WITH_CODE_",
			PREVIOUS_CODE_PLACEHOLDER = "$?",
			ECHO_COMMAND = "echo",
			SCREEN_CONNECTION_CONFIRMED = "SCREEN_CONNECTION_CONFIRMED";
	private static final int
			SUCCESS_CODE = 0,
			NEXT_LINE_TIMEOUT_MS = 1000,
			START_SCREEN_TIMEOUT_MS = 3000,
			CONNECT_SCREEN_TIMEOUT_MS = 3000;

	public final Session session;
	public final ChannelShell channel;
	public final OutputStreamWriter writer;
	public final BufferedReader stdoutReader;

	private int commandCount = 0;

	public ShellSshSessionRunner(Session session, ChannelShell channel) throws IOException{
		this.session = session;
		this.channel = channel;
		this.writer = new OutputStreamWriter(channel.getOutputStream());
		this.stdoutReader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
	}

	public void init(int timeoutMs) throws Exception{
		waitForPrompt(timeoutMs, false);
	}

	public void waitForCommand(String command, int timeoutMs, boolean printOutput) throws Exception{
		runCommand(command);
		waitForPrompt(timeoutMs, printOutput);
	}

	public void runCommand(String command) throws Exception{
		writer.write(command + "\n");
		writer.flush();
	}

	public void startScreen(String screenName, boolean printOutput) throws Exception{
		logger.warn("Opening screen={}", screenName);
		String openScreenCommand = "screen -dmS " + screenName;
		waitForCommand(openScreenCommand, START_SCREEN_TIMEOUT_MS, printOutput);
	}

	public void sendCommandToScreen(String screenName, String command, int timeoutMs) throws Exception{
		String writeScreenOpenCheckCommand = "screen -S %s -X stuff $'%s\\n'".formatted(screenName, command);
		waitForCommand(writeScreenOpenCheckCommand, timeoutMs, true);
	}

	public void connectScreen(String screenName, boolean printOutput) throws Exception{
		String connectionConfirmationEcho = "%s \"%s\"".formatted(ECHO_COMMAND, SCREEN_CONNECTION_CONFIRMED);
		sendCommandToScreen(screenName, connectionConfirmationEcho, CONNECT_SCREEN_TIMEOUT_MS);
		String connectScreenCommand = "screen -rd %s".formatted(screenName);
		runCommand(connectScreenCommand);
		// connecting to the screen can take more time
		readUntilLineMatch(line -> !line.contains(ECHO_COMMAND) && line.contains(SCREEN_CONNECTION_CONFIRMED),
				CONNECT_SCREEN_TIMEOUT_MS, CONNECT_SCREEN_TIMEOUT_MS, printOutput);
		logger.warn("Connected to screen={}", screenName);
	}

	@Override
	public void close(){
		try{
			writer.close();
			stdoutReader.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}finally{
			channel.disconnect();
			session.disconnect();
		}
	}

	private void waitForPrompt(int timeoutMs, boolean printOutput) throws Exception{
		runCommand(getCommandCheckStatement());
		findCommandCheckStatement(timeoutMs, NEXT_LINE_TIMEOUT_MS, printOutput);
	}

	private void findCommandCheckStatement(int totalTimeoutMs, int nextLineTimeoutMs, boolean printOutput)
	throws Exception{
		String commandCheckOutput = readUntilLineMatch(
				line -> !line.contains(ECHO_COMMAND) && line.contains(getCurrentCommandIdentifier()),
				totalTimeoutMs,
				nextLineTimeoutMs,
				printOutput);
		int code = Integer.parseInt(commandCheckOutput.substring(
				commandCheckOutput.indexOf(CODE_CHECK_PREFIX) + CODE_CHECK_PREFIX.length()));
		logger.warn("Command[{}] exited with code={}", commandCount, code);
		if(code != SUCCESS_CODE){
			throw new BadExitCodeException("Command[%d] failed with code=%d".formatted(commandCount, code));
		}
		commandCount++;
	}

	private String readUntilLineMatch(Function<String,Boolean> matcher, int totalTimeoutMs, int nextLineTimeoutMs,
			boolean printOutput)
	throws Exception{
		MilliTime timeout = MilliTime.now().plus(totalTimeoutMs, ChronoUnit.MILLIS);

		String line = null;
		try(ExecutorService executor = Executors.newSingleThreadExecutor()){
			boolean hasFoundPrompt = false;
			while(!hasFoundPrompt){
				if(MilliTime.now().isAfter(timeout)){
					throw new InterruptedException("Timed out waiting for prompt after " + totalTimeoutMs + "ms");
				}
				Future<String> future = executor.submit(() -> {
					try{
						return stdoutReader.readLine();
					}catch(IOException e){
						throw new RuntimeException(e);
					}
				});
				try{
					line = future.get(nextLineTimeoutMs, TimeUnit.MILLISECONDS);
				}catch(TimeoutException e){
					future.cancel(true);
					throw new InterruptedException("Timed out waiting for next line after " + nextLineTimeoutMs + "ms");
				}
				if(line == null){
					continue;
				}
				if(printOutput){
					logger.warn(line);
				}
				hasFoundPrompt = matcher.apply(line);
			}
		}
		return line;
	}

	private String getCurrentCommandIdentifier(){
		return COMMAND_IDENTIFIER_PREFIX + commandCount;
	}

	private String getCommandCheckStatement(){
		String commandIdentifier = getCurrentCommandIdentifier();
		String codeCheckStatement = CODE_CHECK_PREFIX + PREVIOUS_CODE_PLACEHOLDER;
		return "%s \"%s %s\"".formatted(ECHO_COMMAND, commandIdentifier, codeCheckStatement);
	}

}
