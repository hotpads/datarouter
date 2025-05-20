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
package io.datarouter.web.filter.metric;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.instrumentation.metric.node.BaseMetricRoot;
import io.datarouter.instrumentation.metric.node.MetricNode;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.UserAgentTypeConfig;
import io.datarouter.web.util.RequestAttributeKey;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterHandlerMetricsFilter implements Filter{

	private static final HandlerMetrics METRICS = new HandlerMetrics();

	@Inject
	private UserAgentTypeConfig userAgentTypeConfig;

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException,ServletException{
		HttpServletResponse response = (HttpServletResponse)res;
		HttpServletRequest request = (HttpServletRequest)req;

		METRICS.request.count();

		long startMs = System.currentTimeMillis();

		try{
			fc.doFilter(req, res);
		}finally{
			long latencyMs = System.currentTimeMillis() - startMs;

			METRICS.response.all.count();
			METRICS.latencyMs.all.measureWithPercentiles(latencyMs);

			Optional<Class<? extends BaseHandler>> handlerClass = find(request, BaseHandler.HANDLER_CLASS);
			Optional<Method> handlerMethod = find(request, BaseHandler.HANDLER_METHOD);

			handlerClass.ifPresent(clazz -> handlerMethod.ifPresent(method -> {
				String classFull = clazz.getName();
				String classSimple = clazz.getSimpleName();
				String methodName = method.getName();

				METRICS.latencyMs.clazz.className(classSimple).measureWithPercentiles(latencyMs);
				METRICS.latencyMs.method.className(classSimple).method(methodName).measureWithPercentiles(latencyMs);
				METRICS.latencyMs.packagedClass.packagedClass(classFull).measureWithPercentiles(latencyMs);
				METRICS.latencyMs.packagedMethod.packagedClass(classFull).method(methodName).measureWithPercentiles(
						latencyMs);

				METRICS.response.status.status(response.getStatus()).count();
				METRICS.response.statusGroup.group((response.getStatus() / 100) + "xx").count();

				METRICS.response.clazz.className(classSimple).count();
				METRICS.response.method.className(classSimple).method(methodName).count();
				String userAgentCategory = userAgentTypeConfig.categorizeUserAgent(RequestTool.getUserAgent(request));
				METRICS.response.userAgent.clazz.className(classSimple).userAgent(userAgentCategory).count();
				METRICS.response.userAgent.method.className(classSimple).method(methodName).userAgent(userAgentCategory)
						.count();
			}));
		}
	}

	private static <T> Optional<T> find(HttpServletRequest request, RequestAttributeKey<T> attribute){
		return RequestAttributeTool.get(request, attribute);
	}

	/**
	 *  DATAROUTER-3604
	 *
	 *  Metrics should follow the pattern of specifying a unique category early in the metric name. Instead of appending
	 *  new dimensions to the end of existing names, create a new category.
	 */
	private static class HandlerMetrics extends BaseMetricRoot{

		private HandlerMetrics(){
			super("Handler");
		}

		private final MetricNode request = literal("request");
		private final ResponseNode response = literal(ResponseNode::new, "response");
		private final LatencyMsNode latencyMs = literal(LatencyMsNode::new, "latencyMs");

		private static class ResponseNode extends MetricNode{
			private final MetricNode all = literal("all");
			private final StatusNode status = literal(StatusNode::new, "status");
			private final StatusGroupNode statusGroup = literal(StatusGroupNode::new, "statusGroup");
			private final ClassNode clazz = literal(ClassNode::new, "class");
			private final ClassNode method = literal(ClassNode::new, "method");
			private final UserAgentNode userAgent = literal(UserAgentNode::new, "userAgent");
		}

		private static class StatusNode extends MetricNode{
			private StatusVariable status(int status){
				return variable(StatusVariable::new, Integer.toString(status));
			}
		}

		private static class StatusVariable extends MetricNodeVariable<StatusVariable>{
			public StatusVariable(){
				super("statusCode", "HTTP response status code", StatusVariable::new);
			}
		}

		private static class StatusGroupNode extends MetricNode{
			private StatusGroupVariable group(String group){
				return variable(StatusGroupVariable::new, group);
			}
		}

		private static class StatusGroupVariable extends MetricNodeVariable<StatusGroupVariable>{
			public StatusGroupVariable(){
				super("statusGroup", "HTTP response status code group", StatusGroupVariable::new);
			}
		}

		private static class LatencyMsNode extends MetricNode{
			private final MetricNode all = literal("all");
			private final ClassNode clazz = literal(ClassNode::new, "class");
			private final ClassNode method = literal(ClassNode::new, "method");
			private final PackagedClassNode packagedClass = literal(PackagedClassNode::new, "packagedClass");
			private final PackagedClassNode packagedMethod = literal(PackagedClassNode::new, "packagedMethod");
		}

		private static class UserAgentNode extends MetricNode{
			private final ClassNode clazz = literal(ClassNode::new, "class");
			private final ClassNode method = literal(ClassNode::new, "method");
		}

		private static class PackagedClassNode extends MetricNode{
			private PackagedClassVariable packagedClass(String packagedClass){
				return variable(PackagedClassVariable::new, packagedClass);
			}
		}

		private static class PackagedClassVariable extends MetricNodeVariable<PackagedClassVariable>{
			private PackagedClassVariable(){
				super("packagedClassName", "Full handler class name", PackagedClassVariable::new);
			}

			private MethodVariable method(String method){
				return variable(MethodVariable::new, method);
			}
		}

		private static class ClassNode extends MetricNode{
			private ClassVariable className(String className){
				return variable(ClassVariable::new, className);
			}
		}

		private static class ClassVariable extends MetricNodeVariable<ClassVariable>{
			public ClassVariable(){
				super("className", "Handler class name", ClassVariable::new);
			}

			private MethodVariable method(String method){
				return variable(MethodVariable::new, method);
			}

			private UserAgentVariable userAgent(String userAgent){
				return variable(UserAgentVariable::new, userAgent);
			}
		}

		private static class MethodVariable extends MetricNodeVariable<MethodVariable>{
			public MethodVariable(){
				super("methodName", "Handler method name", MethodVariable::new);
			}

			private UserAgentVariable userAgent(String userAgent){
				return variable(UserAgentVariable::new, userAgent);
			}
		}

		private static class UserAgentVariable extends MetricNodeVariable<UserAgentVariable>{
			public UserAgentVariable(){
				super("userAgent", "Categorized user agent", UserAgentVariable::new);
			}
		}

	}

}
