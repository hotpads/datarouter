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
package io.datarouter.web.html;

import java.util.Arrays;
import java.util.List;

public class CloudflareImports{

	public static final String
			BABEL_STANDALONE_6_24_0 = "https://cdnjs.cloudflare.com/ajax/libs/babel-standalone/6.24.0/babel.min.js",
			BABEL_STANDALONE_6_26_0 = "https://cdnjs.cloudflare.com/ajax/libs/babel-standalone/6.26.0/babel.min.js",

			REACT_15_6_1 = "https://cdnjs.cloudflare.com/ajax/libs/react/15.6.1/react.js",
			REACT_16_2_0 = "https://cdnjs.cloudflare.com/ajax/libs/react/16.2.0/umd/react.production.min.js",

			REACT_DOM_15_6_1 = "https://cdnjs.cloudflare.com/ajax/libs/react/15.6.1/react-dom.js",
			REACT_DOM_16_2_0
					= "https://cdnjs.cloudflare.com/ajax/libs/react-dom/16.2.0/umd/react-dom.production.min.js",

			REACT_ROUTER_3_0_2 = "https://cdnjs.cloudflare.com/ajax/libs/react-router/3.0.2/ReactRouter.min.js",
			REACT_ROUTER_3_2_5 = "https://cdnjs.cloudflare.com/ajax/libs/react-router/3.2.5/ReactRouter.min.js";

	public static final List<String> REACT_GROUP_1 = Arrays.asList(
			BABEL_STANDALONE_6_24_0,
			REACT_15_6_1,
			REACT_DOM_15_6_1,
			REACT_ROUTER_3_0_2);

	public static final List<String> REACT_GROUP_2 = Arrays.asList(
			BABEL_STANDALONE_6_26_0,
			REACT_16_2_0,
			REACT_DOM_16_2_0,
			REACT_ROUTER_3_2_5);

}
