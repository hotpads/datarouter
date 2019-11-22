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
package io.datarouter.web.config;

import javax.inject.Singleton;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.web.file.FilesRoot;

@Singleton
public class DatarouterWebFiles extends FilesRoot{

	public final AutocompleteFiles autocomplete = branch(AutocompleteFiles::new, "autocomplete");
	public final BootstrapFiles bootstrap = branch(BootstrapFiles::new, "bootstrap");
	public final FontAwesomeFiles fontAwesome = branch(FontAwesomeFiles::new, "font-awesome");
	public final CssFiles css = branch(CssFiles::new, "css");
	public final DygraphFiles dygraph = branch(DygraphFiles::new, "dygraph");
	public final JeeAssetsFiles jeeAssets = branch(JeeAssetsFiles::new, "jee-assets");
	public final JqueryFiles jquery = branch(JqueryFiles::new, "jquery");
	public final JqueryFloatThread203Files jqueryFloatThreadFiles203 = branch(JqueryFloatThread203Files::new,
			"jQuery.floatThead-2.0.3");
	public final JsFiles js = branch(JsFiles::new, "js");
	public final JspFiles jsp = branch(JspFiles::new, "jsp");
	public final RequirejsFiles requirejs = branch(RequirejsFiles::new, "requirejs");
	public final SorttableFiles sorttable = branch(SorttableFiles::new, "sorttable");

	public static class AutocompleteFiles extends PathNode{
		public final PathNode autocompleteJs = leaf("autocomplete.js");
	}

	public static class BootstrapFiles extends PathNode{
		public final Bootstrap3Files v3 = branch(Bootstrap3Files::new, "v3");
		public final Bootstrap4Files v4 = branch(Bootstrap4Files::new, "v4");
	}

	public static class Bootstrap3Files extends PathNode{
		public final Bootstrap3CssFiles css = branch(Bootstrap3CssFiles::new, "css");
		public final Bootstrap3FontsFiles fonts = branch(Bootstrap3FontsFiles::new, "fonts");
		public final Bootstrap3JsFiles js = branch(Bootstrap3JsFiles::new, "js");
	}

	public static class Bootstrap4Files extends PathNode{
		public final Bootstrap4CssFiles css = branch(Bootstrap4CssFiles::new, "css");
		public final Bootstrap4JsFiles js = branch(Bootstrap4JsFiles::new, "js");
	}

	public static class Bootstrap4CssFiles extends PathNode{
		public final PathNode bootstrapCss = leaf("bootstrap.min.css");
		public final PathNode datarouterBoostrap4OverrideCss = leaf("datarouter-boostrap-4-override.css");
	}

	public static class Bootstrap4JsFiles extends PathNode{
		public final PathNode bootstrapJs = leaf("bootstrap.bundle.min.js");
	}

	public static class FontAwesomeFiles extends PathNode{
		public final FontAwesomeCssFiles css = branch(FontAwesomeCssFiles::new, "css");
		public final FontAwesomeWebfontFiles webfonts = branch(FontAwesomeWebfontFiles::new, "webfonts");
	}

	public static class FontAwesomeCssFiles extends PathNode{
		public final PathNode fontAwesomeBaseCss = leaf("fontawesome.min.css");
		public final PathNode fontAwesomeBrandsCss = leaf("brands.min.css");
		public final PathNode fontAwesomeRegularCss = leaf("regular.min.css");
		public final PathNode fontAwesomeSolidCss = leaf("solid.min.css");
	}

	public static class FontAwesomeWebfontFiles extends PathNode{
		public final PathNode fontAwesomeBrandsFontEot = leaf("fa-brands-400.eot");
		public final PathNode fontAwesomeBrandsFontSvg = leaf("fa-brands-400.svg");
		public final PathNode fontAwesomeBrandsFontTtf = leaf("fa-brands-400.ttf");
		public final PathNode fontAwesomeBrandsFontWoff = leaf("fa-brands-400.woff");
		public final PathNode fontAwesomeBrandsFontWoff2 = leaf("fa-brands-400.woff2");
		public final PathNode fontAwesomeRegularFontEot = leaf("fa-regular-400.eot");
		public final PathNode fontAwesomeRegularFontSvg = leaf("fa-regular-400.svg");
		public final PathNode fontAwesomeRegularFontTtf = leaf("fa-regular-400.ttf");
		public final PathNode fontAwesomeRegularFontWoff = leaf("fa-regular-400.woff");
		public final PathNode fontAwesomeRegularFontWoff2 = leaf("fa-regular-400.woff2");
		public final PathNode fontAwesomeSolidFontEot = leaf("fa-solid-900.eot");
		public final PathNode fontAwesomeSolidFontSvg = leaf("fa-solid-900.svg");
		public final PathNode fontAwesomeSolidFontTtf = leaf("fa-solid-900.ttf");
		public final PathNode fontAwesomeSolidFontWoff = leaf("fa-solid-900.woff");
		public final PathNode fontAwesomeSolidFontWoff2 = leaf("fa-solid-900.woff2");
	}

	public static class Bootstrap3CssFiles extends PathNode{
		public final PathNode bootstrapThemeCss = leaf("bootstrap-theme.css");
		public final PathNode bootstrapCss = leaf("bootstrap.css");
	}

	public static class Bootstrap3FontsFiles extends PathNode{
		public final PathNode glyphiconsHalflingsRegularEot = leaf("glyphicons-halflings-regular.eot");
		public final PathNode glyphiconsHalflingsRegularSvg = leaf("glyphicons-halflings-regular.svg");
		public final PathNode glyphiconsHalflingsRegularTtf = leaf("glyphicons-halflings-regular.ttf");
		public final PathNode glyphiconsHalflingsRegularWoff = leaf("glyphicons-halflings-regular.woff");
		public final PathNode glyphiconsHalflingsRegularWoff2 = leaf("glyphicons-halflings-regular.woff2");
	}

	public static class Bootstrap3JsFiles extends PathNode{
		public final PathNode bootstrapJs = leaf("bootstrap.js");
	}

	public static class CssFiles extends PathNode{
		public final NavbarCssFiles navbar = branch(NavbarCssFiles::new, "navbar");
		public final PathNode commonCss = leaf("common.css");
		public final PathNode newCommonCss = leaf("new-common.css");
	}

	public static class NavbarCssFiles extends PathNode{
		public final PathNode navbarCss = leaf("navbar.css");
		public final PathNode navbarV2Css = leaf("navbar-v2.css");
		public final PathNode navbarV2MultiWebappCss = leaf("navbar-v2-multi-webapp.css");
		public final PathNode navbarV2SingleWebappCss = leaf("navbar-v2-single-webapp.css");
	}

	public static class DygraphFiles extends PathNode{
		public final PathNode dygraphCombinedJs = leaf("dygraph-combined.js");
		public final PathNode dygraphExtraJs = leaf("dygraph-extra.js");
	}

	public static class JeeAssetsFiles extends PathNode{
		public final JeeAssetsMultipleSelectFiles multipleSelect = branch(JeeAssetsMultipleSelectFiles::new,
				"multiple-select");
		public final PathNode datarouterLogoPng = leaf("datarouter-logo.png");
	}

	public static class JeeAssetsMultipleSelectFiles extends PathNode{
		public final PathNode multipleSelectCss = leaf("multiple-select.css");
		public final PathNode multipleSelectJs = leaf("multiple-select.js");
		public final PathNode multipleSelectPng = leaf("multiple-select.png");
	}

	public static class JqueryFiles extends PathNode{
		public final JqueryImagesFiles images = branch(JqueryImagesFiles::new, "images");
		public final PathNode jqueryUiCss = leaf("jquery-ui.css");
		public final PathNode jqueryUiJs = leaf("jquery-ui.js");
		public final PathNode jqueryJs = leaf("jquery.js");
		public final PathNode jqueryValidateJs = leaf("jquery.validate.js");
	}

	public static class JqueryImagesFiles extends PathNode{
		public final PathNode uiBgDiagonalsThick18b8190040x40Png = leaf("ui-bg_diagonals-thick_18_b81900_40x40.png");
		public final PathNode uiBgDiagonalsThick2066666640x40Png = leaf("ui-bg_diagonals-thick_20_666666_40x40.png");
		public final PathNode uiBgGlass100f6f6f61x400Png = leaf("ui-bg_glass_100_f6f6f6_1x400.png");
		public final PathNode uiBgGlass100fdf5ce1x400Png = leaf("ui-bg_glass_100_fdf5ce_1x400.png");
		public final PathNode uiBgGlass65ffffff1x400Png = leaf("ui-bg_glass_65_ffffff_1x400.png");

		public final PathNode uiIcons222222256x240Png = leaf("ui-icons_222222_256x240.png");
		public final PathNode uiIconsffd27a256x240Png = leaf("ui-icons_ffd27a_256x240.png");
		public final PathNode uiIcons228ef1256x240Png = leaf("ui-icons_228ef1_256x240.png");
		public final PathNode uiIconsffffff256x240Png = leaf("ui-icons_ffffff_256x240.png");
		public final PathNode uiIconsef8c08256x240Png = leaf("ui-icons_ef8c08_256x240.png");

		public final PathNode uiBgHighlightSoft100eeeeee1x100Png = leaf("ui-bg_highlight-soft_100_eeeeee_1x100.png");
		public final PathNode uiBgHighlightSoft75ffe45c1x100Png = leaf("ui-bg_highlight-soft_75_ffe45c_1x100.png");

		public final PathNode uiBgGlossWave35f6a828500x100Png = leaf("ui-bg_gloss-wave_35_f6a828_500x100.png");
		public final PathNode uiBgFlat1000000040x100Png = leaf("ui-bg_flat_10_000000_40x100.png");
	}

	public static class JqueryFloatThread203Files extends PathNode{
		public final JqueryFloatThread203JsFiles js = branch(JqueryFloatThread203JsFiles::new, "js");
	}

	public static class JqueryFloatThread203JsFiles extends PathNode{
		public final PathNode jqueryFloatThreadMinJs = leaf("jquery.floatThead.min.js");
	}

	public static class JsFiles extends PathNode{
		public final JsUtilFiles util = branch(JsUtilFiles::new, "util");
		public final PathNode accountManagerJs = leaf("accountManager.js");
		public final PathNode coreCommonJs = leaf("core-common.js");
		public final PathNode navbarRequestTimingJs = leaf("navbar-request-timing.js");
		public final PathNode navbarRequestTimingV2Js = leaf("navbar-request-timing-v2.js");
		public final PathNode viewUsersJsx = leaf("viewUsers.jsx");
	}

	public static class JsUtilFiles extends PathNode{
		public final PathNode jqueryCookieJs = leaf("jquery-cookie.js");
		public final PathNode jqueryCsvJs = leaf("jquery-csv.js");
	}

	public static class JspFiles extends PathNode{
		public final JspAdminFiles admin = branch(JspAdminFiles::new, "admin");
		public final JspAuthenticationFiles authentication = branch(JspAuthenticationFiles::new, "authentication");
		public final JspCssFiles css = branch(JspCssFiles::new, "css");
		public final JspDocsFiles docs = branch(JspDocsFiles::new, "docs");
		public final JspGenericFiles generic = branch(JspGenericFiles::new, "generic");
		public final JspMenuFiles menu = branch(JspMenuFiles::new, "menu");
	}

	public static class JspAdminFiles extends PathNode{
		public final JspAdminDatarouterFiles datarouter = branch(JspAdminDatarouterFiles::new, "datarouter");
		public final PathNode deleteNodeDataJsp = leaf("deleteNodeData.jsp");
		public final PathNode viewNodeDataJsp = leaf("viewNodeData.jsp");
		public final PathNode getNodeDataJsp = leaf("getNodeData.jsp");
	}

	public static class JspAdminDatarouterFiles extends PathNode{
		public final PathNode datarouterMenuJsp = leaf("datarouterMenu.jsp");
		public final JspAdminDatarouterExecutorsMonitoringFiles executorsMonitoring = branch(
				JspAdminDatarouterExecutorsMonitoringFiles::new, "executorsMonitoring");
		public final JspAdminDatarouterMemoryStatsFiles memoryStats = branch(JspAdminDatarouterMemoryStatsFiles::new,
				"memoryStats");
	}

	public static class JspAdminDatarouterExecutorsMonitoringFiles extends PathNode{
		public final PathNode executorsJsp = leaf("executors.jsp");
	}

	public static class JspAdminDatarouterMemoryStatsFiles extends PathNode{
		public final PathNode librariesJsp = leaf("libraries.jsp");
		public final PathNode memoryJsp = leaf("memory.jsp");
		public final PathNode memoryPoolJsp = leaf("memoryPool.jsp");
		public final PathNode memoryUsageJsp = leaf("memoryUsage.jsp");
	}

	public static class JspAuthenticationFiles extends PathNode{
		public final PathNode createUserFormJsp = leaf("createUserForm.jsp");
		public final PathNode editUserFormJsp = leaf("editUserForm.jsp");
		public final PathNode permissionRequestJsp = leaf("permissionRequest.jsp");
		public final PathNode resetPasswordFormJsp = leaf("resetPasswordForm.jsp");
		public final PathNode signinFormJsp = leaf("signinForm.jsp");
		public final PathNode viewUsersJsp = leaf("viewUsers.jsp");
	}

	public static class JspCssFiles extends PathNode{
		public final PathNode cssImportJspf = leaf("css-import.jspf");
		public final PathNode newCssImportJspf = leaf("new-css-import.jspf");
	}

	public static class JspDocsFiles extends PathNode{
		public final PathNode dispatcherDocsJsp = leaf("dispatcherDocs.jsp");
	}

	public static class JspGenericFiles extends PathNode{
		public final PathNode baseHeadJsp = leaf("baseHead.jsp");
		public final PathNode newBaseHeadJsp = leaf("newBaseHead.jsp");
		public final PathNode codeJsp = leaf("code.jsp");
		public final PathNode datarouterHeadJsp = leaf("datarouterHead.jsp");
		public final PathNode jsonJsp = leaf("json.jsp");
		public final PathNode messageJsp = leaf("message.jsp");
		public final PathNode preludeJsp = leaf("prelude.jspf");
		public final PathNode stringJsp = leaf("string.jsp");
	}

	public static class JspMenuFiles extends PathNode{
		public final PathNode commonNavbarJsp = leaf("common-navbar.jsp");
		public final PathNode newCommonNavbarJsp = leaf("new-common-navbar.jsp");
	}


	public static class RequirejsFiles extends PathNode{
		public final RequirejsPluginsFiles plugins = branch(RequirejsPluginsFiles::new, "plugins");
		public final PathNode requireJs = leaf("require.js");
	}

	//used in datarouterHead.jsp
	public static class RequirejsPluginsFiles extends PathNode{
		public final PathNode asyncJs = leaf("async.js");
		public final PathNode googJs = leaf("goog.js");
		public final PathNode propertyParserJs = leaf("propertyParser.js");
	}

	//used in baseHead.jsp
	public static class SorttableFiles extends PathNode{
		public final PathNode sorttableJs = leaf("sorttable.js");
	}

}