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
package io.datarouter.web.navigation;

import io.datarouter.util.string.StringTool;

public enum DatarouterNavBarCategory implements NavBarCategory{
	HOME("Home"),
	CONFIGURATION("Configuration"),
	MONITORING("Monitoring"),
	JOBS("Jobs"),
	DATA("Data"),
	TOOLS("Tools"),
	EXTERNAL("External"),
	;

	private final String display;

	DatarouterNavBarCategory(String display){
		this.display = display;
	}

	@Override
	public String display(){
		return display;
	}

	@Override
	public String sortBy(){
		return StringTool.pad(Integer.toString(ordinal()), '0', 2);// 2 assumes max 100 menus which should be enough
	}

	@Override
	public NavBarItemType type(){
		return NavBarItemType.DATAROUTER;
	}

}
