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
package io.datarouter.conveyor.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.i;
import static j2html.TagCreator.span;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import io.datarouter.clustersetting.link.ClusterSettingBrowseLink;
import io.datarouter.conveyor.ConveyorAppListener;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.config.DatarouterConveyorClusterThreadCountSettings;
import io.datarouter.conveyor.config.DatarouterConveyorSettingRoot;
import io.datarouter.conveyor.config.DatarouterConveyorShouldRunSettings;
import io.datarouter.conveyor.config.DatarouterConveyorThreadCountSettings;
import io.datarouter.conveyor.dto.ConveyorSummary;
import io.datarouter.conveyor.web.ConveyorExternalLinkBuilder.ConveyorExternalLinkBuilderSupplier;
import io.datarouter.httpclient.endpoint.link.DatarouterLinkClient;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.ThTag;
import jakarta.inject.Inject;

public class ConveyorHandler extends BaseHandler{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ConveyorExternalLinkBuilderSupplier externalLinkBuilder;
	@Inject
	private DatarouterLinkClient linkClient;

	@Handler
	private Mav list(){
		Map<Boolean,List<ConveyorSummary>> summariesByShouldRun = injector.scanValuesOfType(ConveyorAppListener.class)
				.map(ConveyorAppListener::getProcessorByConveyorName)
				.concatIter(ConveyorSummary::summarize)
				.groupBy(ConveyorSummary::shouldRun);
		List<ConveyorSummary> enabledConveyors = summariesByShouldRun.getOrDefault(true, List.of());
		List<ConveyorSummary> disabledConveyors = summariesByShouldRun.getOrDefault(false, List.of());
		var content = div(
				h3("Conveyors"),
				makeTableDiv("Enabled", enabledConveyors),
				makeTableDiv("Disabled", disabledConveyors))
				.withClass("container mt-3");
		return pageFactory.startBuilder(request)
				.withTitle("Conveyors")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private DivTag makeTableDiv(String title, List<ConveyorSummary> rows){
		List<ConveyorSummary> sortedRows = Scanner.of(rows)
				.sort(Comparator.comparing(ConveyorSummary::name, String::compareToIgnoreCase))
				.list();
		var headerDiv = makeTableHeaderDiv(title, rows.size());
		var table = makeTableBuilder().build(sortedRows);
		var tableDiv = div(table)
				.withClass("mt-1");
		return div(headerDiv, tableDiv)
				.withClass("mt-3");
	}

	private DivTag makeTableHeaderDiv(String name, int count){
		var nameSpan = span(name);
		var countSpan = span(String.format("(%s)", count))
				.withStyle("color:gray;");
		return div(h5(nameSpan, countSpan));
	}

	private J2HtmlTable<ConveyorSummary> makeTableBuilder(){
		return new J2HtmlTable<ConveyorSummary>()
				.withClasses("sortable table table-sm table-striped table-hover border")
				.withHtmlColumn(
						"Name",
						row -> {
							String metricNamePrefix = ConveyorCounters.PREFIX + " " + row.name();
							return externalLinkBuilder.get().counters(metricNamePrefix)
									.map(href -> td(a(row.name()).withHref(href)))
									.orElse(td(row.name()));
						})
				.withHtmlColumn(
						makeRightAlignTh("Active Threads"),
						row -> td(NumberFormatter.addCommas(row.executor().getActiveCount()))
								.withStyle("text-align:right"))
				.withHtmlColumn(
						makeRightAlignTh("Pool Size"),
						row -> td(NumberFormatter.addCommas(row.executor().getPoolSize()))
								.withStyle("text-align:right"))
				.withHtmlColumn(
						makeRightAlignTh("Max Instance Threads"),
						row -> {
							var browseLink = new ClusterSettingBrowseLink()
									.withLocation(makeMaxThreadCountSettingLocation(row.name()));
							String href = linkClient.toInternalUrl(browseLink);
							return td(a(NumberFormatter.addCommas(row.maxInstanceThreads())).withHref(href))
									.withStyle("text-align:right");
				})
				.withHtmlColumn(
						makeRightAlignTh("Max Cluster Threads"),
						row -> {
							var browseLink = new ClusterSettingBrowseLink()
									.withLocation(makeMaxClusterThreadCountSettingLocation(row.name()));
							String href = linkClient.toInternalUrl(browseLink);
							return td(a(NumberFormatter.addCommas(row.maxClusterThreads())).withHref(href))
									.withStyle("text-align:right");
				})
				.withHtmlColumn(
						"Enabled",
						row -> {
							var browseLink = new ClusterSettingBrowseLink()
									.withLocation(makeShouldRunSettingLocation(row.name()));
							String href = linkClient.toInternalUrl(browseLink);
							return td(a(String.valueOf(row.shouldRun())).withHref(href));
				})
				.withHtmlColumn(
						"Exceptions",
						row -> {
							var chartIcon = i().withClass("fas fa-chart-line");
							return externalLinkBuilder.get().exceptions(row.name())
									.map(href -> td(a(chartIcon).withHref(href)))
									.orElse(td("N/A"));

				});
	}

	private ThTag makeRightAlignTh(String name){
		return th(name).withStyle("text-align:right");
	}

	private String makeShouldRunSettingLocation(String settingName){
		return DatarouterConveyorSettingRoot.SETTING_NAME_PREFIX
				+ DatarouterConveyorShouldRunSettings.SETTING_NAME_PREFIX
				+ settingName;
	}

	private String makeMaxThreadCountSettingLocation(String settingName){
		return DatarouterConveyorSettingRoot.SETTING_NAME_PREFIX
				+ DatarouterConveyorThreadCountSettings.SETTING_NAME_PREFIX
				+ settingName;
	}

	private String makeMaxClusterThreadCountSettingLocation(String settingName){
		return DatarouterConveyorSettingRoot.SETTING_NAME_PREFIX
				+ DatarouterConveyorClusterThreadCountSettings.SETTING_NAME_PREFIX
				+ settingName;
	}

}
