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
package io.datarouter.storage.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.setting.DatarouterEmailSubscriberSettings;
import io.datarouter.util.EmailTool;

@Deprecated // IN-7968 - Split up DatarouterAdministratorEmailService
@Singleton
public class DatarouterAdministratorEmailService{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterSubscribersSupplier subscribers;
	@Inject
	private DatarouterEmailSubscriberSettings adminEmailSettings;

	public List<String> getAdminAndSubscribers(){
		List<String> administrators = new ArrayList<>();
		administrators.add(datarouterProperties.getAdministratorEmail());
		if(adminEmailSettings.includeSubscribers.get()){
			administrators.addAll(subscribers.get());
		}
		return administrators;
	}

	public String getAdminAndSubscribersCsv(){
		return String.join(",", getAdminAndSubscribers());
	}

	public String getAdminAndSubscribersCsv(String... additional){
		return getAdminAndSubscribersCsv(Set.of(additional));
	}

	public String getAdminAndSubscribersCsv(Collection<String> additional){
		Set<String> emails = new HashSet<>(getAdminAndSubscribers());
		emails.addAll(additional);
		return String.join(",", emails);
	}

	// excludes main administrator email
	public String getSubscribersCsv(String... additionalEmailAddresses){
		return String.join(",", getSubscribers(additionalEmailAddresses));
	}

	public Set<String> getSubscribers(String... additionalEmailAddresses){
		Set<String> emails = new HashSet<>();
		if(adminEmailSettings.includeSubscribers.get()){
			emails.addAll(subscribers.get());
		}
		emails.addAll(Set.of(additionalEmailAddresses));
		return emails;
	}

	public String getAdministratorEmailWithSupplement(String supplement){
		return EmailTool.addSupplementToEmailAddress(datarouterProperties.getAdministratorEmail(), supplement);
	}

}
