/**
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
package io.datarouter.storage.servertype;

import java.util.List;

import io.datarouter.util.web.HtmlSelectOptionBean;

public class SimpleServerType implements ServerType{

	private final String serviceName;
	private final boolean isProduction;

	public SimpleServerType(String serviceName, boolean isProduction){
		this.serviceName = serviceName;
		this.isProduction = isProduction;
	}

	@Override
	public List<HtmlSelectOptionBean> getHtmlSelectOptionsVarNames(){
		List<HtmlSelectOptionBean> beans = StandardServerType.ALL.getHtmlSelectOptionsVarNames();
		beans.add(new HtmlSelectOptionBean(serviceName, serviceName));
		return beans;
	}

	@Override
	public ServerType fromPersistentString(String str){
		if(serviceName.equals(str)){
			return this;
		}
		return StandardServerType.fromPersistentStringStatic(str);
	}

	@Override
	public String getPersistentString(){
		return serviceName;
	}

	@Override
	public boolean isProduction(){
		return isProduction;
	}

}
