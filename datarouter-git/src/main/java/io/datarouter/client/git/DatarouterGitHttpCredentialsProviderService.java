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

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterGitHttpCredentialsProviderService{

	@Inject
	private DatarouterGitHttpCredentialsProvider httpCredentialsProvider;

	public CredentialsProvider getCredentialsProvider(){
		return new CredentialsProvider(){

			@Override
			public boolean supports(CredentialItem... items){
				for(CredentialItem item : items){
					if(item instanceof CredentialItem.Username || item instanceof CredentialItem.Password){
						continue;
					}
					return false;
				}
				return true;
			}

			@Override
			public boolean isInteractive(){
				return false;
			}

			@Override
			public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem{
				UsernamePasswordCredentialsProvider credentialsProvider = httpCredentialsProvider.getCredentials(uri);
				return credentialsProvider.get(uri, items);
			}

		};
	}

}
