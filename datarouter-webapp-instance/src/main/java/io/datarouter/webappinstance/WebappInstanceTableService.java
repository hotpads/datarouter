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
package io.datarouter.webappinstance;

import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.li;
import static j2html.TagCreator.span;
import static j2html.TagCreator.strong;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tag;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;
import static j2html.TagCreator.ul;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import io.datarouter.webappinstance.job.WebappInstanceUpdateJob;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
import j2html.tags.specialized.TdTag;

@Singleton
public class WebappInstanceTableService{

	public static final int STALE_WEBAPP_INSTANCE_DAYS = 1;
	public static final int OLD_WEBAPP_INSTANCE_DAYS = 4;

	private static final DomContent LEGEND = table().withClasses("table", "table-bordered", "table-sm")
			.with(tr(th("Legend")))
			.with(tr(td("Instance build is older than " + pluralDay(STALE_WEBAPP_INSTANCE_DAYS)))
					.withClass("table-warning"))
			.with(tr(td("Instance build is older than " + pluralDay(OLD_WEBAPP_INSTANCE_DAYS)))
					.withClass("table-danger"))
			.with(tr(td(span("version").withClasses("badge", "badge-warning"),
					text(" Value differs from related instances"))));

	@Inject
	private Bootstrap4PageFactory pageFactory;

	public <T> Mav buildMav(HttpServletRequest request, List<T> instances, WebappInstanceTableOptions options,
			List<WebappInstanceColumn<T>> columns){
		Map<WebappInstanceColumn<T>,ColumnUsageStat<T>> statsByColumn = getStatsByColumn(instances, columns);

		List<ColumnUsageStat<T>> columnStats = Scanner.of(columns)
				.map(statsByColumn::get)
				.include(Objects::nonNull)
				.exclude(stat -> stat.column.hideUsageStatsBreakdown)
				.list();

		var info = Scanner.of(columnStats)
				.include(stat -> stat.allCommon)
				.map(stat -> {
					String stringValue = stringOrEmpty(stat.mostCommon);
					DomContent mostCommon = b(stringValue);
					if(stat.column.statLinkBuilder.isPresent()){
						var link = stat.column.statLinkBuilder.get().apply(stringValue);
						mostCommon = a(mostCommon)
								.withHref(link)
								.withCondTarget(stat.column.statLinkTargetBlank, "_blank");
					}
					return div(
							text("All instances have " + stat.column.name + " "),
							mostCommon);
				})
				.list();

		var warning = Scanner.of(columnStats)
				.exclude(stat -> stat.allCommon)
				.map(stat -> div(
						text("Multiple " + stat.column.name + " running accross instances"),
						ul().with(Scanner.of(stat.usage)
								.map(entry -> {
									var entryValue = text(" - " + stringOrEmpty(entry.key));
									var percentage = strong(entry.getUsagePercentagePrintable() + '%');
									if(stat.column.statLinkBuilder.isPresent()){
										var link = stat.column.statLinkBuilder.get().apply(stringOrEmpty(entry.key));
										var aTag = a(percentage, entryValue)
												.withHref(link)
												.withCondTarget(stat.column.statLinkTargetBlank, "_blank");
										return li(aTag);
									}
									return li(percentage, entryValue);
								})
								.list())))
				.list();

		return pageFactory.startBuilder(request)
				.withContent(div().withClass("container-fluid")
						.with(h2("Webapp Instances").withClasses("mt-5", "pb-2", "mb-3", "border-bottom"))
						.with(options.beforeAlerts)
						.condWith(options.showInstanceCount || !info.isEmpty(), div().withClasses("alert", "alert-info")
								.condWith(options.showInstanceCount, makeInstanceCount(instances))
								.with(info))
						.condWith(!warning.isEmpty(), div().withClasses("alert", "alert-warning").with(warning))
						.with(buildTable(instances, columns))
						.with(div(LEGEND).withClasses("col-sm-6", "offset-sm-3")))
				.withTitle("Webapp Instances")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.buildMav();
	}

	public <T> TableTag buildTable(List<T> instances, List<WebappInstanceColumn<T>> columns){
		return buildTable(instances, columns, getStatsByColumn(instances, columns));
	}

	private <T> TableTag buildTable(List<T> instances, List<WebappInstanceColumn<T>> columns,
			Map<WebappInstanceColumn<T>,ColumnUsageStat<T>> statsByColumn){
		var table = new J2HtmlTable<T>()
				.withClasses("sortable", "table", "table-bordered", "table-sm", "table-striped");

		columns.forEach(col -> {
			if(!col.showUsageStats){
				table.withHtmlColumn(col.name, instance -> col.buildTd(instance, Optional.empty()));
				return;
			}
			var colStats = statsByColumn.get(col);
			if(!colStats.allCommon){
				table.withHtmlColumn(col.name, instance -> col.buildTd(instance, Optional.of(colStats)));
			}
		});

		return table.build(instances);
	}

	private String stringOrEmpty(Object object){
		return Optional.ofNullable(object)
				.map(Object::toString)
				.orElse("");
	}

	private <T> Map<WebappInstanceColumn<T>,ColumnUsageStat<T>> getStatsByColumn(List<T> instances,
			List<WebappInstanceColumn<T>> columns){
		return Scanner.of(columns)
				.include(column -> column.showUsageStats)
				.toMap(Function.identity(), col -> new ColumnUsageStat<>(instances, col));
	}

	private static DivTag makeInstanceCount(List<?> list){
		var count = b(Integer.toString(list.size()));
		return list.size() == 1
				? div(text("There is "), count, text(" instance reporting"))
				: div(text("There are "), count, text(" instances reporting"));
	}

	private static String pluralDay(int days){
		return "" + days + " day" + (days == 1 ? "" : "s");
	}

	public static boolean getHighlightRefreshedLast(Instant refreshedLast){
		if(refreshedLast == null){
			return false;
		}
		long secondsSinceLastRefresh = Duration.between(refreshedLast, Instant.now()).toSeconds();
		return secondsSinceLastRefresh > (WebappInstanceUpdateJob.WEBAPP_INSTANCE_UPDATE_SECONDS_DELAY + 1);
	}

	public static boolean isStaleWebappInstance(Instant buildInstant){
		long days = Duration.between(buildInstant, Instant.now()).toDays();
		return days > STALE_WEBAPP_INSTANCE_DAYS;
	}

	public static boolean isOldWebappInstance(Instant buildInstant){
		long days = Duration.between(buildInstant, Instant.now()).toDays();
		return days > OLD_WEBAPP_INSTANCE_DAYS;
	}

	public static <S,T> T getMostCommonValue(List<S> objects, Function<S,T> mapper){
		return objects.stream()
				.collect(Collectors.groupingBy(mapper, Collectors.counting()))
				.entrySet().stream()
				.reduce((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? entry1 : entry2)
				.map(Entry::getKey)
				.get();
	}

	public static class WebappInstanceTableOptions{

		private DomContent beforeAlerts = tag(null);
		private boolean showInstanceCount = false;

		public WebappInstanceTableOptions withBeforeAlerts(DomContent beforeAlerts){
			this.beforeAlerts = beforeAlerts;
			return this;
		}

		public WebappInstanceTableOptions withShowInstanceCount(boolean showInstanceCount){
			this.showInstanceCount = showInstanceCount;
			return this;
		}

	}

	public record CellClass<T>(
			String cell,
			Predicate<T> predicate){
	}

	public static class WebappInstanceColumn<T>{

		private final String name;
		private final Function<T,?> getValue;
		private final List<CellClass<T>> cellClasses = new ArrayList<>();
		private boolean showUsageStats = false;
		private boolean hideUsageStatsBreakdown = false;
		private boolean cellLinkTargetBlank = false;
		private boolean statLinkTargetBlank = false;
		private Optional<Function<T,String>> cellLinkBuilder = Optional.empty();
		private Optional<Function<String,String>> statLinkBuilder = Optional.empty();
		private Optional<Function<T,?>> getSortableValue = Optional.empty();
		private Optional<Function<T,String>> getTitle = Optional.empty();
		private Optional<BiPredicate<T,ColumnUsageStat<T>>> asBadge = Optional.empty();

		public WebappInstanceColumn(String name, Function<T,?> getValue){
			this.name = name;
			this.getValue = getValue;
		}

		public WebappInstanceColumn<T> withShowUsageStats(){
			showUsageStats = true;
			return this;
		}

		public WebappInstanceColumn<T> withHideUsageStatsBreakdown(){
			hideUsageStatsBreakdown = true;
			return this;
		}

		public WebappInstanceColumn<T> withCellLinkBuilder(boolean targetBlank, Function<T,String> cellLinkBuilder){
			this.cellLinkTargetBlank = targetBlank;
			this.cellLinkBuilder = Optional.of(cellLinkBuilder);
			return this;
		}

		public WebappInstanceColumn<T> withStatLinkBuilder(boolean targetBlank,
				Function<String,String> statLinkBuilder){
			this.statLinkTargetBlank = targetBlank;
			this.statLinkBuilder = Optional.of(statLinkBuilder);
			return this;
		}

		public WebappInstanceColumn<T> withSortableValue(Function<T,?> getSortableValue){
			this.getSortableValue = Optional.of(getSortableValue);
			return this;
		}

		public WebappInstanceColumn<T> withTitle(Function<T,String> getTitle){
			this.getTitle = Optional.of(getTitle);
			return this;
		}

		public WebappInstanceColumn<T> withCellClass(String className, Predicate<T> addClass){
			cellClasses.add(new CellClass<>(className, addClass));
			return this;
		}

		public WebappInstanceColumn<T> withAsBadge(BiPredicate<T,ColumnUsageStat<T>> asBadge){
			this.asBadge = Optional.of(asBadge);
			return this;
		}

		private TdTag buildTd(T item, Optional<ColumnUsageStat<T>> stats){
			Object val = getValue.apply(item);
			String valueString = Objects.toString(val, "");
			DomContent value = val instanceof DomContent ? ((DomContent)val) : text(valueString);
			boolean wrapBadge = asBadge.isPresent() && stats.isPresent() && asBadge.get().test(item, stats.get());
			DomContent badgedValue = wrapBadge ? span(value).withClasses("badge", "badge-warning") : value;
			DomContent linked = cellLinkBuilder.map(builder -> builder.apply(item))
					.or(() -> statLinkBuilder.map(builder -> builder.apply(valueString)))
					.<DomContent>map(href -> a(badgedValue).withHref(href)
							.withCondTarget(cellLinkTargetBlank, "_blank"))
					.orElseGet(() -> badgedValue);
			var td = td(linked);
			getSortableValue.map(getSortable -> getSortable.apply(item))
					.ifPresent(customKey -> td.attr("sorttable_customkey", customKey));
			getTitle.map(fn -> fn.apply(item))
					.ifPresent(td::withTitle);
			Scanner.of(cellClasses)
					.include(pair -> pair.predicate().test(item))
					.map(CellClass::cell)
					.forEach(td::withClass);
			return td;
		}

	}

	public static class ColumnUsageStat<T>{

		public final WebappInstanceColumn<T> column;
		public final List<UsageStatEntry> usage;
		public final int uniqueCount;
		public final boolean allCommon;
		public final Object mostCommon;

		protected ColumnUsageStat(List<T> instances, WebappInstanceColumn<T> column){
			this.column = column;
			Map<Object,Long> frequency = instances.stream()
					.map(column.getValue)
					.filter(Objects::nonNull)
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			uniqueCount = frequency.size();
			allCommon = uniqueCount <= 1;
			mostCommon = Scanner.of(frequency.entrySet())
					.findMax(Entry.comparingByValue())
					.map(Entry::getKey)
					.orElse(null);
			usage = Scanner.of(frequency.entrySet())
					.sort(Entry.comparingByValue(Comparator.reverseOrder()))
					.map(entry -> new UsageStatEntry(entry.getKey(), entry.getValue() / (float)instances.size() * 100))
					.list();
		}

	}

	protected static class UsageStatEntry{

		private final Object key;
		private final float usagePercentage;

		public UsageStatEntry(Object key, float usagePercentage){
			this.key = key;
			this.usagePercentage = usagePercentage;
		}

		public String getUsagePercentagePrintable(){
			return String.format("%.1f", usagePercentage);
		}

	}

}
