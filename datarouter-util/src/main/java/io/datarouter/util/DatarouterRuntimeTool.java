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
package io.datarouter.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class DatarouterRuntimeTool{

	public static RunNativeDto runNative(String... command){
		Runtime runtime = Runtime.getRuntime();
		Process process;
		if(command.length == 1){
			try{
				process = runtime.exec(command[0]);
			}catch(IOException e){
				throw new RuntimeException("command=" + Arrays.toString(command), e);
			}
		}else{
			try{
				process = runtime.exec(command);
			}catch(IOException e){
				throw new RuntimeException("command=" + Arrays.toString(command), e);
			}
		}
		BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		StringBuilder stdout = new StringBuilder();
		String line;
		int numLines = 0;
		try{
			while((line = stdoutReader.readLine()) != null){
				stdout
						.append(numLines++ > 0 ? "\n" : "")
						.append(line);
			}
		}catch(IOException e){
			throw new RuntimeException("stdout=" + stdout + " command=" + Arrays.toString(command));
		}
		BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		StringBuilder stderr = new StringBuilder();
		String errLine;
		int numErrLines = 0;
		try{
			while((errLine = stderrReader.readLine()) != null){
				stderr
						.append(numErrLines++ > 0 ? "\n" : "")
						.append(errLine);
			}
		}catch(IOException e){
			throw new RuntimeException("stderr=" + stderr + " stdout=" + stdout + " command=" + Arrays.toString(
					command));
		}
		try{
			process.waitFor();
		}catch(InterruptedException e){
			throw new RuntimeException("stderr=" + stderr + " stdout=" + stdout + " command=" + Arrays.toString(
					command));
		}
		int exitValue = process.exitValue();
		if(exitValue > 0){
			throw new RuntimeException("exitStatus=" + exitValue + " stderr=" + stderr + " stdout=" + stdout
					+ " command=" + Arrays.toString(command));
		}
		return new RunNativeDto(stdout.toString(), stderr.toString(), exitValue);
	}

}
