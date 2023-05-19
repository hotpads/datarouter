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
package io.datarouter.clustersetting.web.browse.setting;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.clustersetting.ClusterSettingScope;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.string.StringTool;

public class DatabaseOverridesTool{

	public record DatabaseOverrideRow(
			String name,
			ClusterSettingScope scope,
			String serverType,
			String serverName,
			String value,
			boolean active,
			boolean winner){

		public static boolean notEmpty(
				List<DatabaseOverrideRow> rows,
				Function<DatabaseOverrideRow,String> stringExtractor){
			return Scanner.of(rows)
					.map(stringExtractor)
					.anyMatch(StringTool::notEmpty);
		}

		public static boolean anyServerType(List<DatabaseOverrideRow> rows){
			return Scanner.of(rows)
					.anyMatch(row -> !ServerType.UNKNOWN.getPersistentString().equals(row.serverType));
		}

	}

	public static List<DatabaseOverrideRow> makeRows(
			CachedSetting<?> setting,
			List<ClusterSetting> dbSettings,
			Optional<ClusterSetting> mostSpecificSetting){
		boolean isActive = setting.getMostSpecificDatabeanValue().isPresent();
		return Scanner.of(dbSettings)
				.map(dbSetting -> {
					boolean isWinner = mostSpecificSetting.isPresent()
							&& dbSetting.equals(mostSpecificSetting.get());
					return new DatabaseOverrideRow(
							setting.getName(),
							dbSetting.getScope(),
							dbSetting.getServerType(),
							dbSetting.getServerName(),
							dbSetting.getValue(),
							isActive,
							isWinner);
				})
				.list();
	}

}