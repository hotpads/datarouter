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

import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.datarouter.client.ssh.DatarouterSshExecutors.DatarouterSshOutputProcessorExecutor;
import io.datarouter.util.RunNativeDto;
import io.datarouter.util.process.RunNativeInputStreamReader;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterSshProcessService{

	@Inject
	private DatarouterSshOutputProcessorExecutor executor;

	public RunNativeDto runProcess(Process process, Duration timeout){
		try{
			Future<String> stdout = processStdStream(process.getInputStream());
			Future<String> stderr = processStdStream(process.getErrorStream());
			int exitVal;
			if(timeout.isZero()){
				exitVal = process.waitFor();
			}else{
				boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
				if(!finished){
					process.destroyForcibly();
					throw new RuntimeException("SSH process timed out after " + timeout.toMillis() + "ms");
				}
				exitVal = process.exitValue();
			}
			String stdoutStr = stdout.get();
			String stderrStr = stderr.get();
			return new RunNativeDto(exitVal, stdoutStr, stderrStr);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private Future<String> processStdStream(InputStream stream){
		return executor.submit(() -> new RunNativeInputStreamReader(stream)
				.linesWithReplacement()
				.collect(Collectors.joining("\n")));
	}

}
