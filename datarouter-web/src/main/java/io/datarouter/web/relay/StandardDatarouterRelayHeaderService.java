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
package io.datarouter.web.relay;

import java.util.List;

import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService.EmailHeaderRow;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class StandardDatarouterRelayHeaderService{

	@Inject
	private ServiceName serviceName;
	@Inject
	private ServerName serverName;
	@Inject
	private EnvironmentName environmentName;

	public RmlBlock makeStandardHeader(){
		return makeStandardHeaderWithSupplements(List.of());
	}

	public RmlBlock makeStandardHeaderWithSupplements(List<EmailHeaderRow> supplements){
		return Rml.table(
				makeRow("environment", environmentName.get()),
				makeRow("service", serviceName.get()),
				makeRow("serverName", serverName.get()))
				.with(supplements.stream()
						.map(row -> makeRow(row.header(), row.value())));
	}

	private static RmlBlock makeRow(String label, String value){
		return Rml.tableRow(
				Rml.tableCell(Rml.text(label).strong()),
				Rml.tableCell(Rml.text(value)));
	}

}
