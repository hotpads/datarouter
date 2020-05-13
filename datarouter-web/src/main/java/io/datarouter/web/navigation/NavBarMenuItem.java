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
package io.datarouter.web.navigation;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.web.dispatcher.DispatchRule;

public class NavBarMenuItem{
	private static final Logger logger = LoggerFactory.getLogger(NavBarMenuItem.class);

	private final URI href;//this is what will appear in HTML
	private final URI path;//this is used to look up dispatch rules (no query params)
	private final String text;
	protected final List<NavBarMenuItem> subItems;

	protected Supplier<Optional<DispatchRule>> dispatchRule;

	public NavBarMenuItem(String text, List<NavBarMenuItem> subItems){
		this.href = URI.create("");
		this.path = href;
		this.text = text;
		this.subItems = subItems.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		this.dispatchRule = SingletonSupplier.of(Optional::empty);
	}

	public NavBarMenuItem(String text, NavBarMenuItem... subItems){
		this(text, Scanner.of(subItems).list());
	}

	public NavBarMenuItem(String path, String text, NavBar parentNavBar){
		this(path, "", text, parentNavBar);
	}

	public NavBarMenuItem(String path, String queryParamStr, String text, NavBar parentNavBar){
		this.href = URI.create(path + queryParamStr);
		this.path = URI.create(path);
		this.text = text;
		this.subItems = null;
		this.dispatchRule = SingletonSupplier.of(() -> findRequiredDispatchRule(parentNavBar));
	}

	public NavBarMenuItem(PathNode pathNode, String text, NavBar parentNavBar){
		this(pathNode.toSlashedString(), text, parentNavBar);
	}

	public NavBarMenuItem(PathNode pathNode, String queryParamString, String text, NavBar parentNavBar){
		this(pathNode.toSlashedString(), queryParamString, text, parentNavBar);
	}

	private Optional<DispatchRule> findRequiredDispatchRule(NavBar parentNavBar){
		Optional<DispatchRule> optRule = parentNavBar.getDispatchRule(path);
		if(optRule.isEmpty()){
			String message = String.format("no DispatchRule for %s, %s", path, text);
			logger.warn(message);
		}
		return optRule;
	}

	public Boolean isDropdown(){
		return subItems != null;
	}

	public boolean isAllowed(HttpServletRequest request){
		boolean hasAllowedSubItem = isDropdown() && !getSubItems(request).isEmpty();
		//if dispatchRule is not present, then assume permission
		boolean isAllowedItem = !isDropdown() && dispatchRule.get().map(rule -> rule.checkRoles(request)).orElse(true);
		return hasAllowedSubItem || isAllowedItem;
	}

	public URI getHref(){
		return href;
	}

	public URI getAbsoluteHref(HttpServletRequest request){
		if(href.isAbsolute()){
			return getHref();
		}
		return URI.create(request.getContextPath() + href.toString());
	}

	public String getText(){
		return text;
	}

	public List<NavBarMenuItem> getSubItems(HttpServletRequest request){
		return Scanner.of(subItems).include(item -> item.isAllowed(request)).list();
	}

}
