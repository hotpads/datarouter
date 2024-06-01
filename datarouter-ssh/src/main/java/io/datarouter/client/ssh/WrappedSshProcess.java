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

/**
 * wrapper that implements AutoCloseable
 */
public class WrappedSshProcess implements SshProcess{

	private final Process process;

	public WrappedSshProcess(Process process){
		this.process = process;
	}

	@Override
	public boolean isAlive(){
		return process.isAlive();
	}

	@Override
	public int exitValue(){
		return process.exitValue();
	}

	@Override
	public int waitFor() throws InterruptedException{
		return process.waitFor();
	}

	@Override
	public InputStream stdout(){
		return process.getInputStream();
	}

	@Override
	public InputStream stderr(){
		return process.getErrorStream();
	}

	@Override
	public void close(){
		process.destroy();
	}

}
