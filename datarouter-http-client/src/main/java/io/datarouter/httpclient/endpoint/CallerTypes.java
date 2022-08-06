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
package io.datarouter.httpclient.endpoint;

/**
 * Some predefined CallerTypes
 */
public class CallerTypes{

	/**
	 * The default option for endpoints
	 */
	public static final CallerType UNKNOWN = new CallerType("unknown");

	/**
	 * For internal services calling each-other. This option is good if you have multiple client/server modules in your
	 * IDE and can track how calls are used by the compiler
	 */
	public static final CallerType DATAROUTER_SERVICE = new CallerType("datarouterService");

	/**
	 * For APIs only used by your internal front-end that are easy to update
	 */
	public static final CallerType INTERNAL_FRONT_END = new CallerType("internalFrontEnd");

	/**
	 * For third parties to interact with your API
	 */
	public static final CallerType EXTERNAL = new CallerType("external");

	/**
	 * An API account marked for deletion
	 */
	public static final CallerType DEPRECATED = new CallerType("deprecated");

}
