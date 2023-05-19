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
package io.datarouter.aws.s3.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;

import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;

public class S3Html{

	public static DivTag makeHeader(String title, String subtitle){
		var header = div(
				makeTitle(title, subtitle));
		return header;
	}

	public static DivTag makeTitle(String title, String subtitle){
		return div(
				h4(title),
				div(subtitle))
				.withClass("mt-3");
	}

	public static ATag makeDangerButton(String name, String href){
		return a(name)
				.withClass("btn btn-danger")
				.withHref(href);
	}

}
