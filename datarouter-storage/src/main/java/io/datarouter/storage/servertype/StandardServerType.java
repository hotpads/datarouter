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

import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.DisplayablePersistentString;
import io.datarouter.util.web.EnumTool;
import io.datarouter.util.web.HtmlSelectOptionBean;

public enum StandardServerType implements ServerType, DisplayablePersistentString{

	UNKNOWN(ServerType.UNKNOWN, false),
	ALL(ServerType.ALL, false),//for factory-like usage
	DEV(ServerType.DEV, false);

	private final String persistentString;
	private final boolean isProduction;

	StandardServerType(String persistentString, boolean isProduction){
		this.persistentString = persistentString;
		this.isProduction = isProduction;
	}

	@Override
	public List<HtmlSelectOptionBean> getHtmlSelectOptionsVarNames(){
		return EnumTool.getHtmlSelectOptions(values());
	}

	@Override
	public StandardServerType fromPersistentString(String str){
		return fromPersistentStringStatic(str);
	}

	public static StandardServerType fromPersistentStringStatic(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public String getDisplay(){
		return persistentString;
	}

	@Override
	public boolean isProduction(){
		return isProduction;
	}

	@Override
	public ServerType getWebServerType(){
		return DEV;
	}

}
