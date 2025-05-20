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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import io.datarouter.client.ssh.config.DatarouterSshConfigSupplier;
import io.datarouter.client.ssh.config.DatarouterSshSettings;
import io.datarouter.client.ssh.request.SshExecSessionRequest;
import io.datarouter.client.ssh.request.SshSessionConfig;
import io.datarouter.client.ssh.request.SshSftpGetSessionRequest;
import io.datarouter.client.ssh.request.SshSftpPutSessionRequest;
import io.datarouter.util.RunNativeDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterSshService{

	private final DatarouterSshProcessService processService;
	private final DatarouterSshSettings settings;

	@Inject
	public DatarouterSshService(
			DatarouterSshConfigSupplier configSupplier,
			DatarouterSshProcessService processService,
			DatarouterSshSettings settings){
		this.processService = processService;
		this.settings = settings;
		JSch.setLogger(new DatarouterJSchLogger());
		configSupplier.get().forEach(JSch::setConfig);
	}

	public Process process(SshExecSessionRequest request){
		return DatarouterSshTool.process(settings.rsaPrivateKey.get(), request);
	}

	public SshProcess sshProcess(SshExecSessionRequest request){
		return new WrappedSshProcess(process(request));
	}

	public RunNativeDto runProcess(SshExecSessionRequest request){
		return processService.runProcess(process(request), request.config().waitForTimeout());
	}

	public void sftp(SshSftpPutSessionRequest request){
		DatarouterSshTool.sftp(settings.rsaPrivateKey.get(), request);
	}

	public void sftp(SshSftpGetSessionRequest request){
		DatarouterSshTool.sftp(settings.rsaPrivateKey.get(), request);
	}

	public DatarouterSshSession sshSession(SshSessionConfig request){
		return DatarouterSshTool.startSession(settings.rsaPrivateKey.get(), request);
	}

	public void sftpWithKey(SshSftpPutSessionRequest request, String privateKey){
		DatarouterSshTool.sftp(privateKey, request);
	}

	public JSch getJSch() throws JSchException{
		return DatarouterSshTool.getJSch(settings.rsaPrivateKey.get());
	}

	public ShellSshSessionRunner shell(SshSessionConfig config) throws IOException{
		return DatarouterSshTool.shell(settings.rsaPrivateKey.get(), config);
	}

}
