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
package io.datarouter.ratelimiter.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.br;
import static j2html.TagCreator.caption;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.join;
import static j2html.TagCreator.li;
import static j2html.TagCreator.p;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;
import static j2html.TagCreator.ul;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.ratelimiter.DatarouterRateLimiter;
import io.datarouter.ratelimiter.DatarouterRateLimiterConfig;
import io.datarouter.ratelimiter.DatarouterRateLimiterGroup;
import io.datarouter.ratelimiter.DatarouterRateLimiterGroup.DatarouterRateLimiterPackage;
import io.datarouter.ratelimiter.DatarouterRateLimiterRegistry;
import io.datarouter.ratelimiter.config.DatarouterRateLimiterPaths;
import io.datarouter.ratelimiter.storage.BaseTallyDao;
import io.datarouter.ratelimiter.util.DatarouterRateLimiterKeyTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;

public class DatarouterRateLimiterHandler extends BaseHandler{

	private static final String P_rateLimiterName = "rateLimiterName";
	private static final String P_rateLimiterKey = "rateLimiterKey";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterRateLimiterRegistry registry;
	@Inject
	private ServletContextSupplier contextSupplier;
	@Inject
	private DatarouterRateLimiterPaths paths;
	@Inject
	private MetricLinkBuilder linkBuilder;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private BaseTallyDao tallyDao;

	@Handler
	public Mav list(){
		return pageFactory.simplePage(
				request,
				"Datarouter - Rate Limiters",
				makeListContent());
	}

	@Handler
	public Mav viewDetails(
			@Param(P_rateLimiterName) String rateLimiterName,
			@Param(P_rateLimiterKey) String rateLimiterKey){
		DatarouterRateLimiter rateLimiter = Scanner.of(registry.getRateLimiterPackages())
				.include(rateLimiterPackage -> rateLimiterPackage.name().equals(rateLimiterName))
				.findFirst()
				.map(DatarouterRateLimiterPackage::rateLimiterClass)
				.map(rateLimiterClass -> injector.getInstance(rateLimiterClass))
				.orElseThrow();

		return pageFactory.simplePage(
				request,
				"Datarouter - Rate Limiter Config",
				makeViewDetailsContent(rateLimiter, rateLimiterKey));
	}

	@Handler
	public Mav viewConfigurationDocs(){
		return pageFactory.simplePage(
				request,
				"Datarouter - Configuration Docs",
				div(
						h2("Rate Limiter Configuration Overview"),
						br(),
						p("Rate limiters track counts starting at the top of the day and time interval.\n"
								+ "If we have 3 rate limiters with the following configurations:"),
						ul(
								li("timeUnit = seconds and bucketInterval = 10"),
								li("timeUnit = hours   and bucketInterval = 6"),
								li("timeUnit = minutes and bucketInterval = 4")),
						p("And we check for an allow at instant 2009-06-06 11:11:11.123, "
								+ "they will be distributed to the following buckets:"),
						ul(
								li("2009-06-06T11:11:10Z when timeUnit = seconds and bucketInterval = 10"),
								li("2009-06-06T06:00:00Z when timeUnit = hours   and bucketInterval = 6"),
								li("2009-06-06T11:08:00Z when timeUnit = minutes and bucketInterval = 4")),
						p(join("The fist limit that is checked is", b("maxSpikeRequests"), "for the current bucket. "
								+ "The second limit that is check is", b("maxAverageRequests"), " which is the average "
								+ "counts across ", b("numIntervals"), ". For the case where ", b("numIntervals"),
								" == 1, the minimum values between ", b("maxSpikeRequests"), " and ",
								b("maxAverageRequests"), "will trigger the limit.")),
						p(join("For ", b("numIntervals"), " == 1 it is recommended that ", b("maxSpikeRequests"),
								" and ", b("maxAverageRequests"), " have the same value.")),
						table(
								thead(th("Key Terms"), th("Description")),
								tr(td("maxAverageRequests"), td("Threshold average number of requests")),
								tr(td("maxSpikeRequests"), td("Threshold max number of requests")),
								tr(td("numIntervals"), td("Number of buckets")),
								tr(td("bucketTimeInterval"), td("Duration period of each bucket")),
								tr(td("unit"), td("Time unit of bucketTimeInterval")))
								.withClass("table table-striped table-bordered table-sm"))
						.withClass("container mt-3"));
	}

	private DivTag makeViewDetailsContent(DatarouterRateLimiter rateLimiter, String rateLimiterKey){
		var rateLimiterConfig = rateLimiter.getConfig();
		var sortedBucketCounts = getSortedBucketCounts(rateLimiterKey, rateLimiterConfig);
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath()
						+ paths.datarouter.rateLimiters.viewConfigurationDocs.toSlashedString());

		var docsLink = a("What do these mean?").withHref(uriBuilder.toString());
		var tbody = tbody();
		tbody.with(
				tr(td("Name"), td(rateLimiterConfig.name)),
				tr(td("Max Average Requests"), td(rateLimiterConfig.maxAverageRequests.toString())),
				tr(td("Max Spike Requests"), td(rateLimiterConfig.maxSpikeRequests.toString())),
				tr(td("Num Intervals"), td(rateLimiterConfig.numIntervals.toString())),
				tr(td("Bucket Interval"), td(new DatarouterDuration(
						rateLimiterConfig.bucketIntervalMs, TimeUnit.MILLISECONDS).toString())),
				tr(td("Expiration"), td(new DatarouterDuration(rateLimiterConfig.expiration).toString())));
		var table = table(caption(docsLink).withStyle("caption-side: top"), tbody)
				.withClass("table table-striped table-bordered table-sm");

		var form = new HtmlForm(HtmlFormMethod.GET);
		form.addTextField()
				.withLabel("Key")
				.withName(P_rateLimiterKey)
				.withPlaceholder("Enter a rate limiter key to see the bucket counts")
				.withValue(rateLimiterKey);
		form.addButton()
				.withLabel("Get")
				.withValue("viewDetails");
		form.addHiddenField(P_rateLimiterName, rateLimiter.getName());
		TableTag countsTable = new J2HtmlTable<BucketCountDto>()
				.withClasses("table table-sm table-striped border")
				.withHtmlColumn(
						th("Bucket"),
						dto -> td(dto.bucket))
				.withHtmlColumn(
						th("Count"),
						dto -> td(Long.toString(dto.count)))
				.build(sortedBucketCounts);

		return div()
				.with(h2("Rate Limiter Details"))
				.with(
						table,
						br(),
						h3("View Bucket Counts"),
						br(),
						Bootstrap4FormHtml.render(form).withClass("card card-body bg-light"),
						br(),
						countsTable)
				.withClass("container mt-3");
	}

	private DivTag makeListContent(){
		List<TableTag> rateLimiterGroupTables = new ArrayList<>();
		registry.getRateLimiterGroups().forEach(rateLimiterGroup ->
				rateLimiterGroupTables.add(
						makeRateLimiterGroupTable(rateLimiterGroup)));
		return div().with(
						h2("Rate Limiters"),
						p("The registered rate limiter groups are listed below."),
						br())
				.with(rateLimiterGroupTables)
				.with(br())
				.withClass("container mt-3");
	}

	private TableTag makeRateLimiterGroupTable(DatarouterRateLimiterGroup rateLimiterGroup){
		return new J2HtmlTable<DatarouterRateLimiterPackage>()
				.withClasses("table table-sm table-striped border")
				.withHtmlColumn(
						th(rateLimiterGroup.getClass().getSimpleName()),
						this::makeRateLimiterNameRow)
				.withHtmlColumn(
						"Details",
						row -> createViewDetailsLink(row.name()))
				.withHtmlColumn(
						"Available Metrics",
						row -> createAvailableMetricsLink(row.name()))
				.build(rateLimiterGroup.getRateLimiters());
	}

	private TdTag createAvailableMetricsLink(String rateLimiterName){
		return td(a("Counters")
				.withHref(linkBuilder.availableMetricsLink(DatarouterRateLimiter.COUNTER_PREFIX + rateLimiterName))
				.withTarget("_blank"));
	}

	private TdTag makeRateLimiterNameRow(DatarouterRateLimiterPackage rateLimiterPackage){
		return td(rateLimiterPackage.name());
	}

	private TdTag createViewDetailsLink(String rateLimiterName){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath()
						+ paths.datarouter.rateLimiters.viewDetails.toSlashedString());
		uriBuilder.addParameter(P_rateLimiterName, rateLimiterName);
		uriBuilder.addParameters(List.of(
				new BasicNameValuePair(P_rateLimiterName, rateLimiterName),
				new BasicNameValuePair(P_rateLimiterKey, "")));
		var link = a("Details").withHref(uriBuilder.toString());
		return td(link);
	}

	private List<BucketCountDto> getSortedBucketCounts(
			String rateLimiterKey,
			DatarouterRateLimiterConfig rateLimiterConfig){
		var keyPrefix = DatarouterRateLimiterKeyTool.makeKeyPrefix(rateLimiterConfig, rateLimiterKey);
		List<String> buckets = DatarouterRateLimiterKeyTool.buildKeysToRead(
				keyPrefix,
				Instant.now(),
				rateLimiterConfig);
		Map<String,Long> bucketCounts = tallyDao.getMultiTallyCount(buckets, Duration.ZERO, Duration.ofSeconds(5));

		return Scanner.of(bucketCounts.entrySet())
				.sort(Entry.comparingByKey())
				.map(entry -> new BucketCountDto(entry.getKey(), entry.getValue()))
				.list();
	}

	private record BucketCountDto(String bucket, long count){}

}
