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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.DefaultSettingValueWinner;
import io.datarouter.storage.setting.DefaultSettingValueWinner.DefaultSettingValueWinnerType;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.string.StringTool;

public class CodeOverridesTool{

	public record CodeOverrideRow(
			boolean isGlobalDefault,
			String environmentType,
			String environmentName,
			String environmentCategoryName,
			String serverType,
			String serverName,
			String value,
			boolean active,
			boolean winner){

		public static boolean notEmpty(
				List<CodeOverrideRow> rows,
				Function<CodeOverrideRow,String> stringExtractor){
			return Scanner.of(rows)
					.map(stringExtractor)
					.anyMatch(StringTool::notEmpty);
		}
	}

	public static <T> List<CodeOverrideRow> makeOverrideRows(CachedSetting<T> setting){
		DefaultSettingValue<T> defaults = setting.getDefaultSettingValue();
		DefaultSettingValueWinner defaultValueWinner = setting.getDefaultSettingValueWinner();
		boolean isGlobalDefault = setting.getMostSpecificDatabeanValue().isEmpty()
				&& defaultValueWinner.type == DefaultSettingValueWinnerType.GLOBAL_DEFAULT;
		boolean hasDatabaseOverride = setting.getMostSpecificDatabeanValue().isPresent();

		List<CodeOverrideRow> rows = new ArrayList<>();

		// environmentType overrides
		String winnerEnvironmentType = defaultValueWinner.environmentType != null
				? defaultValueWinner.environmentType
				: "";
		defaults.getValueByEnvironmentType().forEach((environmentType, value) -> {
			boolean isActive = winnerEnvironmentType.equals(environmentType.getPersistentString());
			boolean isWinner = !hasDatabaseOverride
					&& isActive
					&& defaultValueWinner.type == DefaultSettingValueWinnerType.ENVIRONMENT_TYPE;
			rows.add(new CodeOverrideRow(
					false,
					environmentType.getPersistentString(),
					null,
					null,
					null,
					null,
					setting.toStringValue(value),
					isActive,
					isWinner));
				});

		// serverType overrides
		String winnerServerType = defaultValueWinner.serverType != null
				? defaultValueWinner.serverType
				: "";
		defaults.getValueByServerTypeByEnvironmentType().forEach((environmentType, defaultByServerType) ->
				defaultByServerType.forEach((serverType, value) -> {
					boolean isActive = winnerEnvironmentType.equals(environmentType.getPersistentString())
							&& winnerServerType.equals(serverType);
					boolean isWinner = !hasDatabaseOverride
							&& isActive
							&& defaultValueWinner.type == DefaultSettingValueWinnerType.SERVER_TYPE;
					rows.add(new CodeOverrideRow(
							false,
							environmentType.getPersistentString(),
							null,
							null,
							serverType,
							null,
							setting.toStringValue(value),
							isActive,
							isWinner));
					}));

		// serviceName overrides
		String winnerServiceName = defaultValueWinner.serviceName != null
				? defaultValueWinner.serviceName
				: "";
		defaults.getValueByServiceNameByEnvironmentType().forEach((environmentType, defaultByServiceName) ->
				defaultByServiceName.forEach((serviceName, value) -> {
					boolean isActive = winnerEnvironmentType.equals(environmentType.getPersistentString())
							&& winnerServiceName.equals(serviceName);
					boolean isWinner = !hasDatabaseOverride
							&& isActive
							&& defaultValueWinner.type == DefaultSettingValueWinnerType.SERVICE_NAME;
					rows.add(new CodeOverrideRow(
							false,
							environmentType.getPersistentString(),
							null,
							null,
							null,
							serviceName,
							setting.toStringValue(value),
							isActive,
							isWinner));
				}));

		// serverName overrides
		String winnerServerName = defaultValueWinner.serverName != null
				? defaultValueWinner.serverName
				: "";
		defaults.getValueByServerNameByEnvironmentType().forEach((environmentType, defaultByServerName) ->
				defaultByServerName.forEach((serverName, value) -> {
					boolean isActive = winnerEnvironmentType.equals(environmentType.getPersistentString())
							&& winnerServerName.equals(serverName);
					boolean isWinner = !hasDatabaseOverride
							&& isActive
							&& defaultValueWinner.type == DefaultSettingValueWinnerType.SERVER_NAME;
					rows.add(new CodeOverrideRow(
							false,
							environmentType.getPersistentString(),
							null,
							null,
							null,
							serverName,
							setting.toStringValue(value),
							isActive,
							isWinner));
					}));

		// environmentName overrides
		String winnerEnvironmentName = defaultValueWinner.environmentName != null
				? defaultValueWinner.environmentName
				: "";
		defaults.getValueByEnvironmentNameByEnvironmentType().forEach((environmentType, defaultByEnvironment) ->
				defaultByEnvironment.forEach((environmentName, value) -> {
					boolean isActive = !isGlobalDefault
							&& winnerEnvironmentType.equals(environmentType.getPersistentString())
							&& winnerEnvironmentName.equals(environmentName);
					boolean isWinner = !hasDatabaseOverride
							&& isActive
							&& defaultValueWinner.type == DefaultSettingValueWinnerType.ENVIRONMENT_NAME;
					rows.add(new CodeOverrideRow(
							false,
							environmentType.getPersistentString(),
							environmentName,
							null,
							null,
							null,
							setting.toStringValue(value),
							isActive,
							isWinner));
					}));

		// environmentCategory overrides
		String winnerEnvironmentCategoryName = defaultValueWinner.environmentCategoryName != null
				? defaultValueWinner.environmentCategoryName
				: "";
		defaults.getValueByEnvironmentCategoryNameByEnvironmentType()
				.forEach((environmentType, defaultByEnvironmentCategory) ->
					defaultByEnvironmentCategory.forEach((environmentCategory, value) -> {
					boolean isActive = !isGlobalDefault
							&& winnerEnvironmentType.equals(environmentType.getPersistentString())
							&& winnerEnvironmentCategoryName.equals(environmentCategory);
					boolean isWinner = !hasDatabaseOverride
							&& isActive
							&& defaultValueWinner.type == DefaultSettingValueWinnerType.ENVIRONMENT_CATEGORY;
					rows.add(new CodeOverrideRow(
							false,
							environmentType.getPersistentString(),
							null,
							environmentCategory,
							null,
							null,
							setting.toStringValue(value),
							isActive,
							isWinner));
				}));
		return rows;
	}

}
