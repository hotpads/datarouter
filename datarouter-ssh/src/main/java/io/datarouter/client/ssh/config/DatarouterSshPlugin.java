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
package io.datarouter.client.ssh.config;

import io.datarouter.client.ssh.config.DatarouterSshConfigSupplier.NoOpDatarouterSshConfigSupplier;
import io.datarouter.web.config.BaseWebPlugin;

public class DatarouterSshPlugin extends BaseWebPlugin{

	private final Class<? extends DatarouterSshConfigSupplier> sshConfigSupplier;

	private DatarouterSshPlugin(Class<? extends DatarouterSshConfigSupplier> sshConfigSupplier){
		this.sshConfigSupplier = sshConfigSupplier;
		addDatarouterGithubDocLink("datarouter-ssh");
		addSettingRoot(DatarouterSshSettings.class);
	}

	@Override
	protected void configure(){
		bind(DatarouterSshConfigSupplier.class).to(sshConfigSupplier);
	}

	public static class DatarouterSshPluginBuilder{

		private Class<? extends DatarouterSshConfigSupplier> sshConfigSupplier = NoOpDatarouterSshConfigSupplier.class;

		public DatarouterSshPluginBuilder withSshConfigSupplier(
				Class<? extends DatarouterSshConfigSupplier> sshConfigSupplier){
			this.sshConfigSupplier = sshConfigSupplier;
			return this;
		}

		public DatarouterSshPlugin build(){
			return new DatarouterSshPlugin(sshConfigSupplier);
		}

	}

}
