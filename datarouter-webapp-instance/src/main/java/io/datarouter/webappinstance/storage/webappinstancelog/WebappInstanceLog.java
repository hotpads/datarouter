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
package io.datarouter.webappinstance.storage.webappinstancelog;

import java.util.function.Supplier;

import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;

public class WebappInstanceLog extends BaseWebappInstanceLog<WebappInstanceLogKey,WebappInstanceLog>{

	public static class WebappInstanceLogFielder
	extends BaseWebappInstanceLogFielder<WebappInstanceLogKey,WebappInstanceLog>{

		public WebappInstanceLogFielder(){
			super(WebappInstanceLogKey::new);
		}

	}

	public WebappInstanceLog(){
		super(new WebappInstanceLogKey());
	}

	public WebappInstanceLog(WebappInstance instance){
		super(new WebappInstanceLogKey(instance), instance);
		this.commitId = instance.getCommitId();
		this.javaVersion = instance.getJavaVersion();
		this.servletContainerVersion = instance.getServletContainerVersion();
	}

	@Override
	public Supplier<WebappInstanceLogKey> getKeySupplier(){
		return WebappInstanceLogKey::new;
	}

}
