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
package io.datarouter.web.handler;

import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.input;
import static j2html.TagCreator.label;
import static j2html.TagCreator.option;
import static j2html.TagCreator.select;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.datarouter.gson.DatarouterGsons;
import io.datarouter.types.MilliTime;
import io.datarouter.types.MilliTimeReversed;
import io.datarouter.types.Ulid;
import io.datarouter.types.UlidReversed;
import io.datarouter.util.time.ZoneIds;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.TagCreator;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.FormTag;
import jakarta.inject.Inject;

public class TimeConverterToolHandler extends BaseHandler{

	public static final String ULID = "Ulid";
	public static final String ULID_REVERSED = "UlidReversed";
	public static final String MILLI_TIME = "MilliTime";
	public static final String MILLI_TIME_REVERSED = "MilliTimeReversed";
	private static final List<String> TYPES = List.of(ULID, ULID_REVERSED, MILLI_TIME, MILLI_TIME_REVERSED);
	private ZoneId zoneId = ZoneId.systemDefault();

	@Inject
	private Bootstrap4PageFactory pageFactory;

	private static final Gson gsonJsonSerializer = DatarouterGsons.withoutEnums();

	@Handler
	public Mav timeConverter(){
		return pageFactory.simplePage(request, "Time Converter", makeContent());
	}

	private DivTag makeContent(){
		var conversionResults = new ArrayList<ConversionResult>();
		String input = params.optionalNotEmpty("input").orElse(MilliTime.now().toString());
		String type = params.optional("type", MILLI_TIME);
		params.optional("timezone").ifPresentOrElse(tz -> zoneId = ZoneId.of(tz), () -> zoneId = getUserZoneId());
		params.optional("results").ifPresent(resultsJson -> conversionResults.addAll(
				gsonJsonSerializer.fromJson(resultsJson, new TypeToken<List<ConversionResult>>(){}.getType())));
		params.optional("submit").ifPresent(_ -> conversionResults.add(
				new ConversionResult(type, input, toHumanReadable(input, type),
						reverseInput(input, type), zoneId.getId())));
		var title = h2().withText("Time Converter").withClass("mb-4");
		return div(title, makeForm(conversionResults), makeHistoryTable(conversionResults.reversed()))
				.withClass("container mt-4");
	}

	private FormTag makeForm(List<ConversionResult> results){
		var lastType = results.isEmpty() ? MILLI_TIME : results.getLast().type;
		var tzDiv = wrap(
				label("Timezone").withClass("text-black-50 justify-content-start mb-0"),
				select()
						.withName("timezone")
						.withClass("form-control form-select")
						.with(TagCreator.each(ZoneIds.ZONE_IDS,
						zone -> option(zone.getId()).attr(zone.getId().equals(zoneId.getId()) ? "selected" : "", "")
								.withValue(zone.getId()))));
		var typeDiv = wrap(
				label().withText("Type").withClass("text-black-50 justify-content-start mb-0"),
				select().withName("type").with(TagCreator.each(TYPES,type ->
								option(type).withValue(type).attr(lastType.equals(type) ? "selected" : "", "")))
						.withClass("form-control form-select"));

		var inputDiv = wrap(
				label().withText("Input").withClass("text-black-50 justify-content-start mb-0"),
				input().withClass("form-control")
						.withPlaceholder(String.valueOf(MilliTime.now().toEpochMilli()))
						.withType("text")
						.withName("input"));

		var prevResults =
				input().withType("hidden").withName("results").withValue(gsonJsonSerializer.toJson(results));

		var actionsDiv = wrap(button("Convert").withClass("btn btn-primary mr-2")
				.withType("submit").withName("submit"));
		return form(tzDiv, typeDiv, inputDiv, actionsDiv, prevResults)
				.withClass("form form-inline d-block d-sm-flex align-items-end mb-4");
	}

	private DivTag wrap(DomContent...tags){
		return div(tags)
				.withClass("mr-2");
	}

	private DivTag makeHistoryTable(List<ConversionResult> results){
		var table = table().withClass("table");
		var tr = tr();
		var thead = thead().with(th().withText("Timezone"), th().withText("Type"), th().withText("Input"),
				th().withText("Human Readable"), th().withText("Reversed")).with(tr);
		table.with(thead);

		var tbody = tbody();
		results.forEach(result -> {
			var row = tr();
			row.with(td().withText(result.timezone), td().withText(result.type), td().withText(result.input),
					td().withText(result.humanReadable), td().withText(result.reverse));
			tbody.with(row);
		});
		table.with(tbody);
		var title = h4().withText("Conversion History").withClass("mb-4");
		var clearButton = button("Clear History").withClass("btn btn-danger").withType("reset")
				.attr("onclick", "window.location.href='" + request.getRequestURI() + "';");
		return div(title, table, clearButton);
	}

	private String toHumanReadable(String input, String type){
		return switch(type){
			case ULID -> convertUlid(input);
			case ULID_REVERSED -> convertUlidReversed(input);
			case MILLI_TIME -> convertMilliTime(input);
			case MILLI_TIME_REVERSED -> convertMilliTimeReversed(input);
			default -> "Unknown type";
		};
	}

	private String reverseInput(String input, String type){
		return switch(type){
			case ULID -> reverseUlid(input);
			case ULID_REVERSED -> reverseUlidReversed(input);
			case MILLI_TIME -> reverseMilliTime(input);
			case MILLI_TIME_REVERSED -> reverseMilliTimeReversed(input);
			default -> "Unknown type";
		};
	}

	private String applyOrErrorMsg(String input, Function<String,String> converter){
		try{
			return converter.apply(input);
		}catch(IllegalArgumentException e){
			return e.getMessage();
		}
	}

	private String convertUlid(String input){
		return applyOrErrorMsg(input,text -> new Ulid(text)
				.getAsHumanReadableTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME, zoneId));
	}

	private String reverseUlid(String input){
		return applyOrErrorMsg(input, _ -> UlidReversed.toUlidReversed(new Ulid(input)).reverseValue());
	}

	private String convertUlidReversed(String input){
		return applyOrErrorMsg(input, _ -> UlidReversed.toUlid(new UlidReversed(input))
				.getAsHumanReadableTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME, zoneId));
	}

	private String reverseUlidReversed(String input){
		return applyOrErrorMsg(input, _ -> UlidReversed.toUlid(new UlidReversed(input)).value());
	}

	private String convertMilliTime(String input){
		return applyOrErrorMsg(input, _ ->
				MilliTime.ofEpochMilli(Long.parseLong(input)).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME, zoneId));
	}

	private String reverseMilliTime(String input){
		return applyOrErrorMsg(input, _ ->
				String.valueOf(MilliTime.ofEpochMilli(Long.parseLong(input)).toReversedEpochMilli()));
	}

	private String convertMilliTimeReversed(String input){
		return applyOrErrorMsg(input,_ -> MilliTimeReversed.ofReversedEpochMilli(Long.parseLong(input))
				.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME, zoneId));
	}

	private String reverseMilliTimeReversed(String input){
		return applyOrErrorMsg(input, _ ->
				String.valueOf(MilliTimeReversed.ofReversedEpochMilli(Long.parseLong(input)).toEpochMilli()));
	}

	private record ConversionResult(
			String type,
			String input,
			String humanReadable,
			String reverse,
			String timezone){

	}

}
