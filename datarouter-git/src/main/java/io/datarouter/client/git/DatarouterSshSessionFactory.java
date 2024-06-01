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
package io.datarouter.client.git;

import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig.Host;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import io.datarouter.client.ssh.DatarouterSshService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterSshSessionFactory extends JschConfigSessionFactory{

	@Inject
	private DatarouterSshService sshService;

	@Override
	public String getType(){
		return "ssh";
	}

	@Override
	protected JSch getJSch(Host hc, FS fs) throws JSchException{
		return sshService.getJSch();
	}

}
