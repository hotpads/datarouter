/**
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
package io.datarouter.storage.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.setting.DatarouterStorageSettingRoot;
import io.datarouter.util.EmailTool;
import io.datarouter.util.collection.SetTool;

@Singleton
public class DatarouterAdministratorEmailService{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterAdditionalAdministrators additionalAdministrators;
	@Inject
	private DatarouterStorageSettingRoot datarouterStorageSettings;

	public List<String> getAdministratorEmailAddresses(){
		List<String> administrators = new ArrayList<>();
		administrators.add(datarouterProperties.getAdministratorEmail());
		if(datarouterStorageSettings.includeAdditionalAdministratorsEmails.get()){
			administrators.addAll(additionalAdministrators.get());
		}
		return administrators;
	}

	public String getAdministratorEmailAddressesCsv(){
		return String.join(",", getAdministratorEmailAddresses());
	}

	public String getAdministratorEmailAddressesCsv(String... additionalEmailAddresses){
		return getAdministratorEmailAddressesCsv(SetTool.of(additionalEmailAddresses));
	}

	public String getAdministratorEmailAddressesCsv(Set<String> additionalEmailAddresses){
		Collection<String> emails = getAdministratorEmailAddresses();
		emails.addAll(additionalEmailAddresses);
		return String.join(",", emails);
	}

	public String getAdministratorEmailWithSupplement(String supplement){
		return EmailTool.addSupplementToEmailAddress(datarouterProperties.getAdministratorEmail(), supplement);
	}

}
