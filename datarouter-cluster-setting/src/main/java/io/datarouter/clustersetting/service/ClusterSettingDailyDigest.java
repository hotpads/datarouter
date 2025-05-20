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
package io.datarouter.clustersetting.service;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.enums.ClusterSettingValidity;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseEmailLinks;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideViewHandler.ClusterSettingOverrideEmailLinks;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ClusterSettingDailyDigest implements DailyDigest{

	private static final String TASK_CATEGORY = "clusterSetting";

	@Inject
	private ClusterSettingService settingService;
	@Inject
	private DailyDigestRmlService digestService;
	@Inject
	private DatarouterClusterSettingPaths paths;
	@Inject
	private ClusterSettingOverrideEmailLinks overrideEmailLinks;
	@Inject
	private ClusterSettingBrowseEmailLinks browseEmailLinks;

	@Override
	public String getTitle(){
		return "Cluster Settings";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.MEDIUM;
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		List<CategoryAndLink> tables = Scanner.of(
				makeHeaderAndRelayContent(ClusterSettingValidity.REDUNDANT, browseEmailLinks::fromEmail),
				makeHeaderAndRelayContent(ClusterSettingValidity.UNREFERENCED, overrideEmailLinks::view),
				makeHeaderAndRelayContent(ClusterSettingValidity.OLD, browseEmailLinks::fromEmail),
				makeHeaderAndRelayContent(ClusterSettingValidity.UNKNOWN, overrideEmailLinks::view))
				.concatOpt(Function.identity())
				.list();

		if(tables.isEmpty()){
			return Optional.empty();
		}

		return Optional.of(Rml.paragraph(
				digestService.makeHeading("Settings", paths.datarouter.settings.overrides.view))
				.with(tables.stream()
						.flatMap(table -> Stream.of(
								Rml.heading(4, Rml.text(table.validity().display)),
								Rml.text(table.validity().description).italic(),
								Rml.table()
										.with(table.settings().stream()
												.map(setting -> Rml.text(setting.name()).link(setting.link()))
												.map(Rml::tableCell)
												.map(Rml::tableRow))))));
	}

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		return Scanner.of(
				makeHeaderAndRelayContent(ClusterSettingValidity.REDUNDANT, browseEmailLinks::fromEmail),
				makeHeaderAndRelayContent(ClusterSettingValidity.UNREFERENCED, overrideEmailLinks::view),
				makeHeaderAndRelayContent(ClusterSettingValidity.OLD, browseEmailLinks::fromEmail),
				makeHeaderAndRelayContent(ClusterSettingValidity.UNKNOWN, overrideEmailLinks::view))
				.concatOpt(Function.identity())
				.concat(category -> Scanner.of(category.settings())
						.map(setting -> new DailyDigestPlatformTask(
								List.of(TASK_CATEGORY, setting.name()),
								List.of(TASK_CATEGORY, category.validity().persistentString),
								category.validity().display + ": " + setting.name(),
								Rml.container(
										Rml.paragraph(Rml.text(setting.name()).link(setting.link())),
										Rml.paragraph(Rml.text(getSettingDescription(category.validity())))))))
				.list();
	}

	private static String getSettingDescription(ClusterSettingValidity validity){
		return switch(validity){
			case REDUNDANT -> "This setting is redundant meaning the custom value is the same as the default value "
					+ "already in code. Generally this means you can remove the custom value.";
			case UNREFERENCED -> "This is an unreferenced setting meaning it does not exist in code. Generally this "
					+ "means the setting is obsolete and the override can be removed.";
			case OLD -> "The setting override has not changed in a while, if it's still needed consider moving it to "
					+ "code.";
			case UNKNOWN -> "Setting root not found in code.";
			case VALID, INVALID_SERVER_NAME, INVALID_SERVER_TYPE -> validity.description;
		};
	}

	private Optional<CategoryAndLink> makeHeaderAndRelayContent(
			ClusterSettingValidity validity,
			Function<String,String> toLink){
		List<ClusterSetting> settings = settingService.scanWithValidity(validity).list();

		if(settings.isEmpty()){
			return Optional.empty();
		}

		return Optional.of(new CategoryAndLink(
				validity,
				settings.stream()
						.map(ClusterSetting::getName)
						.map(setting -> new SettingAndLink(setting, toLink.apply(setting)))
						.toList()));
	}

	private record CategoryAndLink(
			ClusterSettingValidity validity,
			List<SettingAndLink> settings){
	}

	private record SettingAndLink(
			String name,
			String link){
	}

}
