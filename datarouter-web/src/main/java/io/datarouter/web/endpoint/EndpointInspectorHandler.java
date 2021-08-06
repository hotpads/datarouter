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
package io.datarouter.web.endpoint;

import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.td;
import static j2html.TagCreator.ul;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;

import io.datarouter.httpclient.client.BaseDatarouterEndpointHttpClientWrapper;
import io.datarouter.httpclient.client.DatarouterHttpClient;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.EndpointParam;
import io.datarouter.httpclient.endpoint.EndpointRegistry;
import io.datarouter.httpclient.endpoint.EndpointRequestBody;
import io.datarouter.httpclient.endpoint.EndpointTool;
import io.datarouter.httpclient.endpoint.Endpoints;
import io.datarouter.httpclient.endpoint.IgnoredField;
import io.datarouter.httpclient.endpoint.ParamType;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

public class EndpointInspectorHandler extends BaseHandler{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterService datarouterService;

	@Handler(defaultHandler = true)
	public Mav view(){
		var endpoints = Scanner.of(injector.getInstancesOfType(DatarouterHttpClient.class).values())
				.include(httpClient -> httpClient instanceof BaseDatarouterEndpointHttpClientWrapper)
				.map(BaseDatarouterEndpointHttpClientWrapper.class::cast)
				.concatIter(httpClient -> {
					return Scanner.of(httpClient.endpoints)
						.concatIter(EndpointRegistry::getEndpoints)
						.map(Supplier::get)
						.concatIter(Endpoints::getEndpoints)
						.map(ReflectionTool::createWithoutNoArgs)
						.each(httpClient::initUrlPrefix)
						.list();
				})
				.sort(Comparator.comparing(endpoint -> endpoint.getClass().getSimpleName()))
				.list();
		var content = makeContent(endpoints);
		return pageFactory.startBuilder(request)
				.withTitle("Endpoint Inspector")
				.withContent(content)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.buildMav();
	}

	private <T extends BaseEndpoint<?>> ContainerTag makeContent(List<T> rows){
		var h2 = h2("Endpoints that " + datarouterService.getServiceName() + " can hit");
		var table = new J2HtmlTable<T>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Name", row -> row.getClass().getSimpleName())
				.withColumn("Type", row -> row.method.name())
				.withColumn("Response Type", row -> row.responseType.getTypeName())
				.withColumn("Path", row -> row.urlPrefix + row.pathNode.toSlashedString())
				.withColumn("Entity", row -> EndpointTool.hasEntity(row)
						.map(Class::getSimpleName)
						.orElse(""))
				.withHtmlColumn("Required Params", row -> {
					List<String> paramDtos = Scanner.of(row.getClass().getFields())
							.exclude(field -> field.isAnnotationPresent(IgnoredField.class))
							.exclude(field -> field.isAnnotationPresent(EndpointRequestBody.class))
							.exclude(field -> field.getType().isAssignableFrom(Optional.class))
							.map(field -> new EndpointInspectorParamDto(row, field))
							.map(EndpointInspectorParamDto::toString)
							.list();
					return td(ul(each(paramDtos, TagCreator::li)));
				})
				.withHtmlColumn("Optional Params", row -> {
					List<String> paramDtos = Scanner.of(row.getClass().getFields())
							.exclude(field -> field.isAnnotationPresent(IgnoredField.class))
							.exclude(field -> field.isAnnotationPresent(EndpointRequestBody.class))
							.include(field -> field.getType().isAssignableFrom(Optional.class))
							.map(field -> new EndpointInspectorParamDto(row, field))
							.map(EndpointInspectorParamDto::toString)
							.list();
					return td(ul(each(paramDtos, TagCreator::li)));
				})
				.withCaption("Total " + rows.size())
				.build(rows);
		return div(h2, table)
				.withClass("container-fluid");
	}

	private static class EndpointInspectorParamDto{

		private final String requestParamType;
		private final Field field;
		private final boolean isOptional;

		private EndpointInspectorParamDto(BaseEndpoint<?> endpoint, Field field){
			this.field = field;
			this.requestParamType = Optional.ofNullable(field.getAnnotation(EndpointParam.class))
					.map(EndpointParam::paramType)
					.map(ParamType::name)
					.orElse(endpoint.method.name());
			this.isOptional = field.getType().isAssignableFrom(Optional.class);
		}

		private String getType(){
			if(!isOptional){
				return "[" + field.getType().getSimpleName() + "]";
			}
			Class<?> clazz = (Class<?>)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
			return "[" + clazz.getSimpleName() + "]";
		}

		@Override
		public String toString(){
			return getType() + " " + field.getName() + " " + requestParamType;
		}

	}

}
