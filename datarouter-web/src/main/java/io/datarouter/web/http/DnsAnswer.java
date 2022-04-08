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
package io.datarouter.web.http;

public class DnsAnswer{

	public final String name;
	public final String ttl;
	public final String clazz;
	public final String type;
	public final String value;

	public DnsAnswer(String name, String ttl, String clazz, String type, String value){
		this.name = name;
		this.ttl = ttl;
		this.clazz = clazz;
		this.type = type;
		this.value = value;
	}

	@Override
	public String toString(){
		return "DnsAnswer [name=" + name + ", ttl=" + ttl + ", clazz=" + clazz + ", type=" + type + ", value=" + value
				+ "]";
	}

}
