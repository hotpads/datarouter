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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.setting.DatarouterAdminEmailSettings;
import io.datarouter.util.EmailTool;

@Singleton
public class DatarouterAdministratorEmailService{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterAdditionalAdministratorsSupplier additionalAdministrators;
	@Inject
	private DatarouterAdminEmailSettings adminEmailSettings;

	public List<String> getAdministratorEmailAddresses(){
		List<String> administrators = new ArrayList<>();
		administrators.add(datarouterProperties.getAdministratorEmail());
		if(adminEmailSettings.includeAdditionalAdministratorsEmails.get()){
			administrators.addAll(additionalAdministrators.get());
		}
		return administrators;
	}

	public String getAdministratorEmailAddressesCsv(){
		return String.join(",", getAdministratorEmailAddresses());
	}

	public String getAdministratorEmailAddressesCsv(String... additionalEmailAddresses){
		return getAdministratorEmailAddressesCsv(Set.of(additionalEmailAddresses));
	}

	public String getAdministratorEmailAddressesCsv(Collection<String> additionalEmailAddresses){
		Set<String> emails = new HashSet<>(getAdministratorEmailAddresses());
		emails.addAll(additionalEmailAddresses);
		return String.join(",", emails);
	}

	// excludes main administrator email
	public String getAdditionalAdministratorOnlyCsv(String... additionalEmailAddresses){
		return String.join(",", getAdditionalAdministratorOnly(additionalEmailAddresses));
	}

	public List<String> getAdditionalAdministratorOnly(String... additionalEmailAddresses){
		List<String> emails = new ArrayList<>();
		if(adminEmailSettings.includeAdditionalAdministratorsEmails.get()){
			emails.addAll(additionalAdministrators.get());
		}
		emails.addAll(Set.of(additionalEmailAddresses));
		return emails;
	}

	public String getAdministratorEmailWithSupplement(String supplement){
		return EmailTool.addSupplementToEmailAddress(datarouterProperties.getAdministratorEmail(), supplement);
	}

}
