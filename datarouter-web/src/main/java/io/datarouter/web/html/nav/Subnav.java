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
package io.datarouter.web.html.nav;

import java.util.ArrayList;
import java.util.List;

public class Subnav{

	public final String name;
	public final String href;
	public final List<Dropdown> dropdowns = new ArrayList<>();

	public Subnav(String name, String href){
		this.name = name;
		this.href = href;
	}

	public Subnav add(Dropdown dropdown){
		dropdowns.add(dropdown);
		return this;
	}

	public static class Dropdown{

		public final String name;
		public final List<DropdownItem> items = new ArrayList<>();

		public Dropdown(String name){
			this.name = name;
		}

		public Dropdown add(DropdownItem item){
			items.add(item);
			return this;
		}

		public Dropdown addItem(String name, String href){
			return add(new DropdownItem(name, href));
		}

	}

	public static class DropdownItem{

		public final String name;
		public final String href;
		public boolean confirm = false;

		public DropdownItem(String name, String href){
			this.name = name;
			this.href = href;
		}

		public DropdownItem confirm(){
			confirm = true;
			return this;
		}

	}

}
