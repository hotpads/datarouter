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
package io.datarouter.web.dispatcher;

/**
 * The dispatch type is a field on a {@link DispatchRule} and helps categorize
 * the rules in a {@link RouteSet}.
 */
public enum DispatchType{

	/** JAVA_ENDPOINT - Endpoints intended for use by other Datarouter Java services */
	JAVA_ENDPOINT("Java Endpoint"),
	/** DEFAULT - Placeholder value before the type is set */
	DEFAULT("Default"),
	/** EXTERNAL_ENDPOINT - Endpoints called by third-parties */
	EXTERNAL_ENDPOINT("External Endpoint"),
	/** INTERNAL_LINK - For routing internal links */
	INTERNAL_LINK("Internal Link"),
	/** MOBILE_ENDPOINT - Endpoints called by mobile applications */
	MOBILE_ENDPOINT("Mobile Endpoint"),
	/** WEB_API - Endpoints called by front-end applications */
	WEB_API("Web API"),
	;

	public final String value;

	DispatchType(String value){
		this.value = value;
	}

}
