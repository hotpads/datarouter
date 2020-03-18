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
package io.datarouter.client.mysql.ddl.execute;

import static j2html.TagCreator.pre;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.connection.MysqlConnectionPoolHolder;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.storage.BaseSchemaUpdateService;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.SchemaUpdateResult;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterSchemaUpdateScheduler;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.lazy.Lazy;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.email.DatarouterHtmlEmailService;

@Singleton
public class MysqlSchemaUpdateService extends BaseSchemaUpdateService{

	private final MysqlSingleTableSchemaUpdateService mysqlSingleTableSchemaUpdateService;
	private final DatarouterHtmlEmailService htmlEmailService;
	private final MysqlConnectionPoolHolder mysqlConnectionPoolHolder;
	private final DatarouterWebPaths datarouterWebPaths;

	@Inject
	public MysqlSchemaUpdateService(
			DatarouterProperties datarouterProperties,
			DatarouterAdministratorEmailService adminEmailService,
			MysqlSingleTableSchemaUpdateService mysqlSingleTableSchemaUpdateService,
			DatarouterSchemaUpdateScheduler executor,
			DatarouterHtmlEmailService htmlEmailService,
			MysqlConnectionPoolHolder mysqlConnectionPoolHolder,
			DatarouterWebPaths datarouterWebPaths){
		super(datarouterProperties, adminEmailService, executor);
		this.mysqlSingleTableSchemaUpdateService = mysqlSingleTableSchemaUpdateService;
		this.htmlEmailService = htmlEmailService;
		this.mysqlConnectionPoolHolder = mysqlConnectionPoolHolder;
		this.datarouterWebPaths = datarouterWebPaths;
	}

	@Override
	protected Callable<Optional<SchemaUpdateResult>> makeSchemaUpdateCallable(
			ClientId clientId,
			Lazy<List<String>> existingTableNames,
			PhysicalNode<?,?,?> node){
		return () -> mysqlSingleTableSchemaUpdateService.performSchemaUpdate(clientId, existingTableNames, node);
	}

	@Override
	protected void sendEmail(String fromEmail, String toEmail, String subject, String body){
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(datarouterWebPaths.datarouter)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(subject)
				.withTitle("MySQL Schema Update")
				.withTitleHref(primaryHref)
				.withContent(pre(body));
		htmlEmailService.trySendJ2Html(fromEmail, toEmail, emailBuilder);
	}

	@Override
	protected List<String> fetchExistingTables(ClientId clientId){
		try(Connection connection = mysqlConnectionPoolHolder.getConnectionPool(clientId).checkOut()){
			return MysqlTool.showTables(connection, mysqlConnectionPoolHolder.getConnectionPool(clientId)
					.getSchemaName());
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
	}

}
