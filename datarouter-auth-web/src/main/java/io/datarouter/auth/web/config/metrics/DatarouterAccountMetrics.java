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
package io.datarouter.auth.web.config.metrics;

import io.datarouter.auth.storage.account.permission.DatarouterAccountPermissionKey;
import io.datarouter.instrumentation.metric.node.BaseMetricRoot;
import io.datarouter.instrumentation.metric.node.MetricNode;
import io.datarouter.storage.util.DatarouterCounters;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountMetrics extends BaseMetricRoot{

	public void incPermissionUsage(DatarouterAccountPermissionKey permission){
		name.account(permission.getAccountName()).count();
		endpoint.endpoint(permission.getEndpoint()).account(permission.getAccountName()).count();
	}

	public String getAccountMetricName(String accountName){
		return name.account(accountName).toMetricName();
	}

	//setup

	public DatarouterAccountMetrics(){
		this("account");
	}

	protected DatarouterAccountMetrics(String rootSuffix){
		super(DatarouterCounters.PREFIX + " " + rootSuffix);
	}

	private final Name name = literal(Name::new, "name");
	private final Endpoint endpoint = literal(Endpoint::new, "endpoint");

	private static class Name extends MetricNode{
		private AccountName account(String account){
			return variable(AccountName::new, account);
		}
	}

	private static class Endpoint extends MetricNode{
		private EndpointName endpoint(String endpointName){
			return variable(EndpointName::new, endpointName);
		}
	}

	private static class AccountName extends MetricNodeVariable<AccountName>{
		private AccountName(){
			super("accountName", "Account name", AccountName::new);
		}
	}

	private static class EndpointName extends MetricNodeVariable<EndpointName>{
		private EndpointName(){
			super("endpoint", "Endpoint name", EndpointName::new);
		}

		private AccountName account(String account){
			return variable(AccountName::new, account);
		}
	}

}
