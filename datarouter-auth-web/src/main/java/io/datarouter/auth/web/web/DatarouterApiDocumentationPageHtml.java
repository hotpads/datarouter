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
package io.datarouter.auth.web.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.input;
import static j2html.TagCreator.p;
import static j2html.TagCreator.pre;
import static j2html.TagCreator.s;
import static j2html.TagCreator.span;
import static j2html.TagCreator.style;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.textarea;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import io.datarouter.auth.web.link.DatarouterDocsLink;
import io.datarouter.httpclient.endpoint.link.DatarouterLinkClient;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.handler.documentation.ApiDocSchemaDto;
import io.datarouter.web.handler.documentation.DatarouterJsonApiSchemaHtml;
import io.datarouter.web.handler.documentation.DocumentedEndpointJspDto;
import io.datarouter.web.handler.documentation.DocumentedParameterJspDto;
import io.datarouter.web.handler.documentation.DocumentedResponseJspDto;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.StyleTag;
import j2html.tags.specialized.TrTag;

// When changing id's in this class you must also update apiDocs.js
public class DatarouterApiDocumentationPageHtml{

	private final String apiName;
	private final Map<String,List<DocumentedEndpointJspDto>> endpointsByDispatchType;
	private final boolean hideAuth;
	private final String apiKeyParameterName;
	private final String apiKey;
	private final String contextPath;
	private final List<ApiDocSchemaDto> schemas;
	private final DatarouterLinkClient datarouterLinkClient;

	public DatarouterApiDocumentationPageHtml(
			String apiName,
			Map<String,List<DocumentedEndpointJspDto>> endpointsByDispatchType,
			boolean hideAuth,
			String apiKeyParameterName,
			String apiKey,
			String contextPath,
			List<ApiDocSchemaDto> schemas,
			DatarouterLinkClient datarouterLinkClient){
		this.apiName = apiName;
		this.endpointsByDispatchType = endpointsByDispatchType;
		this.hideAuth = hideAuth;
		this.apiKeyParameterName = apiKeyParameterName;
		this.apiKey = apiKey;
		this.contextPath = contextPath;
		this.schemas = schemas;
		this.datarouterLinkClient = datarouterLinkClient;
	}

	public DomContent build(String url){
		return div(
				buildStyles(),
				buildHeader(),
				buildContent(url),
				buildSchemas(schemas))
				.with(DatarouterJsonApiSchemaHtml.buildInfoTableStyle())
				.withClass("container my-4");
	}

	private StyleTag buildStyles(){
		return style("""
				.string { color: green; }
				.number { color: blue; }
				.boolean { color: red; }
				.null { color: magenta; }
				.key { color: black; }
				.copyable { cursor: pointer; }
				""");
	}

	private DivTag buildHeader(){
		return div(
				h2(apiName + " API Documentation"))
				.withClass("mb-4");
	}

	private DivTag buildContent(String url){
		DocumentedEndpointJspDto endpoint = Scanner.of(endpointsByDispatchType.entrySet())
				.concatIter(Entry::getValue)
				.include(e -> e.getUrl().equals(url))
				.findFirst()
				.orElse(null);
		if (endpoint == null) {
			return div("No api found at path: " + url);
		}

		return buildEndpointCard(endpoint);
	}

	private DivTag buildEndpointCard(DocumentedEndpointJspDto endpoint){
		return div(
				div()
						.with(p()
								.condWith(!endpoint.getIsDeprecated() && endpoint.getDeprecatedOn().isEmpty(),
										text(endpoint.getUrl()))
								.condWith(endpoint.getIsDeprecated() || !endpoint.getDeprecatedOn().isEmpty(),
										s(endpoint.getUrl()))
								.withClass("lead"))
						.condWith(!Optional.ofNullable(endpoint.getDescription()).orElse("").isEmpty(),
								p(endpoint.getDescription()).withClass("text-muted")),
				div(buildCardBody(endpoint)));
	}

	private DivTag buildCardBody(DocumentedEndpointJspDto endpoint){
		return div(
				buildImplementationInfo(endpoint),
				buildParameters(endpoint),
				buildResponse(endpoint),
				buildTryItOut(endpoint),
				buildErrors(endpoint));
	}

	private DivTag buildImplementationInfo(DocumentedEndpointJspDto endpoint){
		String newServiceHref = endpoint.getNewServiceHref();
		String newWebEndpointPath = endpoint.getNewWebEndpointPath();
		String newMobileEndpointPath = endpoint.getNewMobileEndpointPath();
		return div(
				b("Handler: "), text(endpoint.getImplementation()),
				b("RequestType: "), text(endpoint.getRequestType()))
				.withClass("mb-2")
				.condWith(endpoint.getIsDeprecated() || !endpoint.getDeprecatedOn().isEmpty(),
						div()
								.withClass("mt-1")
								.condWith(endpoint.getIsDeprecated() || !endpoint.getDeprecatedOn().isEmpty(),
										span("Deprecated").withClass("badge badge-warning"))
								.condWith(!endpoint.getDeprecatedOn().isEmpty(),
										span("Deprecated On: " + endpoint.getDeprecatedOn()).withClass(
												"badge badge-warning"))
								.condWith(!endpoint.getDeprecationLink().isEmpty(), span()
										.with(a("More Information")
												.withHref(endpoint.getDeprecationLink()))
										.withClass("border-left px-2 border-dark"))
								.condWith(!"".equals(newWebEndpointPath), span()
										.with(a("Replacement Web API")
												.withHref(getReplacementDocsHref(newServiceHref, newWebEndpointPath)))
										.withClass("border-left px-2 border-dark"))
								.condWith(!"".equals(newMobileEndpointPath), span()
										.with(a("Replacement Mobile API")
												.withHref(
														getReplacementDocsHref(newServiceHref, newMobileEndpointPath)))
										.withClass("border-left px-2 border-dark")));
	}

	private String getReplacementDocsHref(String newServiceHref, String newEndpointPath){
		if(!newServiceHref.isEmpty()){
			return newServiceHref + datarouterLinkClient.toInternalUrlWithoutContext(
					new DatarouterDocsLink()
							.withEndpoint(newEndpointPath));
		}
		return datarouterLinkClient.toInternalUrl(new DatarouterDocsLink()
				.withEndpoint(newEndpointPath));
	}

	private DivTag buildParameters(DocumentedEndpointJspDto endpoint){
		if(endpoint.getParameters().isEmpty()){
			return div(h3("Parameters"), text("None"));
		}
		return div(
				h3("Parameters"),
				table(
						thead(tr(
								th("Name"),
								th("Value"),
								th("Type"))),
						tbody(
								each(endpoint.getParameters(), this::buildParameterRow)))
						.withClass("table"))
				.condWith(!endpoint.getParamsEnumValuesDisplay().isEmpty() && !endpoint.hasRequestBody(), div()
						.with(h5("Parameter Enum Values"))
						.with(pre(endpoint.getParamsEnumValuesDisplay())
								.withClass("bg-light border p-2 json")));
	}

	private TrTag buildParameterRow(DocumentedParameterJspDto param){
		String note;
		if(hideAuth && param.getHidden()){
			note = "(Automatically Configured)";
		}else if(param.getRequired()){
			note = "(required)";
		}else{
			note = "(optional)";
		}
		String paramValue = param.getName().equals(apiKeyParameterName) ? apiKey : "";
		DomContent paramName;
		if(param.getRequestBody()){
			paramName = span("Request body");
		}else if(param.getIsDeprecated()){
			paramName = span()
					.with(s(param.getName()))
					.with(span("Deprecated").withClass("badge badge-warning"));
		}else{
			paramName = span(param.getName());
		}

		String inputType;
		String inputId = "param-" + param.getName();
		if(param.getRequestBody()){
			inputType = "textarea";
		}else{
			inputType = "input";
		}

		DomContent inputField;
		if(inputType.equals("textarea")){
			inputField = textarea()
					.withId(inputId)
					.withClass("form-control paramValue" + (param.getRequestBody() ? " request-body" : ""))
					.withStyle("display:table-cell; width:100%")
					.withRows("10")
					.withName("requestBody")
					.withText(paramValue);
		}else{
			inputField = input()
					.withId(inputId)
					.withClass("form-control paramValue")
					.withStyle("display:table-cell; width:100%")
					.withType("text")
					.withName(param.getName())
					.withPlaceholder(note)
					.withValue(paramValue);
		}

		DomContent paramDetails;
		if(param.getSchema() == null){
			paramDetails = text(param.getType());
		}else{
			paramDetails = div(DatarouterJsonApiSchemaHtml.toFieldType(param.getSchema(), ""))
					.withStyle(getFieldTypeStyle());
		}

		return tr(
				td(paramName)
						.withClass("paramName")
						.withData("name", param.getName()),
				td(inputField),
				td(paramDetails)
						.condWith(param.getDescription() != null, div(param.getDescription()))
						.condWith(param.getExample() != null && !param.getRequestBody(), div()
								.with(pre(param.getExample())
										.withClass("bg-light p-2 json copyable mb-0 border")
										.withData("copydest", inputId)))
						.condWith(param.getExample() != null && param.getRequestBody(), div()
								.withClass("card mt-2")
								.with(div()
										.withClass("card-header p-0")
										.with(button()
												.withText("Example")
												.attr("data-toggle", "collapse")
												.attr("data-target", "#example-collapse-" + inputId)
												.withClass("btn btn-link")
										)
								)
								.with(div()
										.withClass("collapse show")
										.withId("example-collapse-" + inputId)
										.with(div()
												.withClass("card-body p-0")
												.with(pre(param.getExample())
														.withClass("bg-light p-2 json copyable mb-0")
														.withData("copydest", inputId)
												)
										)
								)))
				.withClass("param-row");
	}

	private DivTag buildResponse(DocumentedEndpointJspDto endpoint){
		DocumentedResponseJspDto response = endpoint.getResponse();
		DivTag result = div(
				h3("Response"))
				.withClass("mb-4");
		if(response == null) {
			result.with(text("Nothing"));
		}else if(response.getSchema() != null){
			result
					.with(div(DatarouterJsonApiSchemaHtml.toFieldType(response.getSchema(), ""))
							.withStyle(getFieldTypeStyle()));
		}else{
			result.with(text(response.getType()));
		}
		if (response != null && response.getExample() != null) {
			result.with(div()
					.withClass("card mt-2")
					.with(div()
							.withClass("card-header p-0")
							.with(button()
									.withText("Example Response")
									.attr("data-toggle", "collapse")
									.attr("data-target", "#example-collapse")
									.withClass("btn btn-link")
							)
					)
					.with(div()
							.withClass("collapse")
							.withId("example-collapse")
							.with(div()
									.withClass("card-body p-0")
									.with(pre(response.getExample())
											.withClass("bg-light p-2 json mb-0")
									)
							)
					)
			);
		}
		return result;
	}

	private DivTag buildErrors(DocumentedEndpointJspDto endpoint){
		if(endpoint.getErrors().isEmpty()){
			return div();
		}
		return div(
				h3("Errors"),
				table(
						tr(
								th("Message"),
								th("Status code")),
						each(endpoint.getErrors(),
								error -> tr(
										td(error.getMessage()),
										td(String.valueOf(error.getStatusCode())))))
						.withClass("table"));
	}

	private DivTag buildTryItOut(DocumentedEndpointJspDto endpoint){
		return div(
				button("Try It Out")
						.withType("button")
						.withClass("btn btn-primary")
						.withId("submit-button")
						.withData("url", contextPath + endpoint.getUrl())
						.withData("hide-auth", String.valueOf(hideAuth)),
				div()
						.withStyle("display:none")
						.withClass("mt-3")
						.withId("response-div")
						.with(
								h3("Response"),
								// The id's in this section are used to look up the elements with javascript. If you
								// change the ids here, make sure to change them in apiDocs.js as well.
								buildResponseSection("Request URL", "requestUrl"),
								buildResponseSection("Request Body", "requestBody"),
								buildResponseSection("Response Body", "jsonResponse"),
								buildResponseSection("Response Code", "responseCode"),
								buildResponseSection("Response Header", "responseHeader")))
				.withId("response-container")
				.withData("apikeyfieldname", endpoint.getApiKeyFieldName());
	}

	private DomContent buildResponseSection(String title, String id) {
		return div(
				h4(title),
				pre().withId(id).withClass("bg-light border p-2")
		);
	}

	private DomContent buildSchemas(List<ApiDocSchemaDto> schemas){
		if (schemas.isEmpty()) {
			return div();
		}
		return div()
				.withClass("mt-4")
				.with(h2("Schemas"))
				.with(DatarouterJsonApiSchemaHtml.buildSchemas(schemas));
	}

	private String getFieldTypeStyle(){
		return "word-spacing: -3px; word-break: break-all";
	}
}
