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

import java.util.Set;

public interface UserAgentTypeConfig{

	String JAVA_USER_AGENT = "java";
	String ANDROID_USER_AGENT = "android";
	String IOS_USER_AGENT = "ios";
	String BOT_USER_AGENT = "bot";
	String WEB_USER_AGENT = "web";

	Set<String> getMobileUserAgents();

	Set<String> getIosUserAgents();

	Set<String> getAndroidUserAgents();

	Set<String> getJavaUserAgents();

	Set<String> getBotUserAgents();

	class NoOpUserAgentTypeConfig implements UserAgentTypeConfig{

		@Override
		public Set<String> getMobileUserAgents(){
			return Set.of();
		}

		@Override
		public Set<String> getIosUserAgents(){
			return Set.of();
		}

		@Override
		public Set<String> getAndroidUserAgents(){
			return Set.of();
		}

		@Override
		public Set<String> getJavaUserAgents(){
			return Set.of();
		}

		@Override
		public Set<String> getBotUserAgents(){
			return Set.of();
		}

	}

}
