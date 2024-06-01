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

import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterGitRunService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterGitRunService.class);

	public final TransportConfigCallback transport;

	@Inject
	public DatarouterGitRunService(
			DatarouterSshSessionFactory sshSessionFactory,
			DatarouterGitHttpCredentialsProviderService httpCredentialService){
		this.transport = (Transport transport) -> {
			if(transport instanceof SshTransport sshTransport){
				sshTransport.setSshSessionFactory(sshSessionFactory);
			}
			if(transport instanceof HttpTransport httpTransport){
				httpTransport.setCredentialsProvider(httpCredentialService.getCredentialsProvider());
			}
		};
	}

	protected static <T> T run(DatarouterGitSupplier<T> callable){
		try{
			return callable.get();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	protected static <T> T run(Path gitDirectory, DatarouterGitRunner<T> runner){
		try(Git git = Git.open(gitDirectory.toFile())){
			return runner.run(git);
		}catch(Exception e){
			logger.error("Command failed in {}", gitDirectory, e);
			throw new RuntimeException("Command failed in " + gitDirectory, e);
		}
	}

	public static <T> T getRepository(Path gitDirectory, DatarouterGitRepositoryRunner<T> runner){
		return run(gitDirectory, git -> runner.run(git.getRepository()));
	}

	public interface DatarouterGitRepositoryRunner<T>{
		T run(Repository repository) throws Exception;
	}

	public interface DatarouterGitRunner<T>{
		T run(Git git) throws Exception;
	}

	public interface DatarouterGitSupplier<T>{
		T get() throws Exception;
	}

}
