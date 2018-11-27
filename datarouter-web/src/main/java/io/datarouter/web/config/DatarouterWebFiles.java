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

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.util.SystemTool;
import io.datarouter.util.io.FileTool;

@Singleton
public class DatarouterWebFiles extends PathNode{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterWebFiles.class);

	public static final String ROOT_IN_WEBAPP = "/src/main/resources/META-INF/resources";

	public static final String JSP_CodeMav = "/jsp/generic/code.jsp";
	public static final String JSP_JsonMav = "/jsp/generic/json.jsp";
	public static final String JSP_MessageMav = "/jsp/generic/message.jsp";
	public static final String JSP_StringMav = "/jsp/generic/string.jsp";

	public final AutocompleteFiles autocomplete = branch(AutocompleteFiles::new, "autocomplete");
	public final BootstrapFiles bootstrap = branch(BootstrapFiles::new, "bootstrap");
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
		public final BootstrapCssFiles css = branch(BootstrapCssFiles::new, "css");
		public final BootstrapFontsFiles fonts = branch(BootstrapFontsFiles::new, "fonts");
		public final BootstrapJsFiles js = branch(BootstrapJsFiles::new, "js");
	}

	public static class BootstrapCssFiles extends PathNode{
		public final PathNode bootstrapThemeCss = leaf("bootstrap-theme.css");
		public final PathNode bootstrapCss = leaf("bootstrap.css");
	}

	public static class BootstrapFontsFiles extends PathNode{
		public final PathNode glyphiconsHalflingsRegularEot = leaf("glyphicons-halflings-regular.eot");
		public final PathNode glyphiconsHalflingsRegularSvg = leaf("glyphicons-halflings-regular.svg");
		public final PathNode glyphiconsHalflingsRegularTtf = leaf("glyphicons-halflings-regular.ttf");
		public final PathNode glyphiconsHalflingsRegularWoff = leaf("glyphicons-halflings-regular.woff");
		public final PathNode glyphiconsHalflingsRegularWoff2 = leaf("glyphicons-halflings-regular.woff2");
	}

	public static class BootstrapJsFiles extends PathNode{
		public final PathNode bootstrapJs = leaf("bootstrap.js");
	}

	public static class CssFiles extends PathNode{
		public final PathNode commonCss = leaf("common.css");
	}

	public static class DygraphFiles extends PathNode{
		public final PathNode dygraphCombinedJs = leaf("dygraph-combined.js");
		public final PathNode dygraphExtraJs = leaf("dygraph-extra.js");
	}

	public static class JeeAssetsFiles extends PathNode{
		public final JeeAssetsMultipleSelectFiles multipleSelect = branch(JeeAssetsMultipleSelectFiles::new,
				"multiple-select");
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
//		ui-bg_diagonals-thick_18_b81900_40x40.png
//		ui-bg_glass_100_f6f6f6_1x400.png
//		ui-bg_gloss-wave_35_f6a828_500x100.png
//		ui-icons_222222_256x240.png
//		ui-icons_ffd27a_256x240.png
//		ui-bg_diagonals-thick_20_666666_40x40.png
//		ui-bg_glass_100_fdf5ce_1x400.png
//		ui-bg_highlight-soft_100_eeeeee_1x100.png
//		ui-icons_228ef1_256x240.png
//		ui-icons_ffffff_256x240.png
//		ui-bg_flat_10_000000_40x100.png
//		ui-bg_glass_65_ffffff_1x400.png
//		ui-bg_highlight-soft_75_ffe45c_1x100.png
//		ui-icons_ef8c08_256x240.png
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
		public final PathNode viewDatabeanJsp = leaf("viewDatabean.jsp");
		public final PathNode viewNodeDataJsp = leaf("viewNodeData.jsp");
		public final PathNode getNodeDataJsp = leaf("getNodeData.jsp");
	}

	public static class JspAdminDatarouterFiles extends PathNode{
		public final PathNode datarouterMenuJsp = leaf("datarouterMenu.jsp");
		public final JspAdminDatarouterExecutorsMonitoringFiles executorsMonitoring = branch(
				JspAdminDatarouterExecutorsMonitoringFiles::new, "executorsMonitoring");
		public final JspAdminDatarouterMemoryFiles memory = branch(JspAdminDatarouterMemoryFiles::new, "memory");
		public final JspAdminDatarouterMemoryStatsFiles memoryStats = branch(JspAdminDatarouterMemoryStatsFiles::new,
				"memoryStats");
		public final PathNode routerSummaryJsp = leaf("routerSummary.jsp");
		public final PathNode thresholdSettingsJsp = leaf("thresholdSettings.jsp");//TODO move to webapp-utils
	}

	public static class JspAdminDatarouterExecutorsMonitoringFiles extends PathNode{
		public final PathNode executorsJsp = leaf("executors.jsp");
	}

	public static class JspAdminDatarouterMemoryFiles extends PathNode{
		public final PathNode memoryClientSummaryJsp = leaf("memoryClientSummary.jsp");//TODO move to datarouter-memory
	}

	public static class JspAdminDatarouterMemoryStatsFiles extends PathNode{
		public final PathNode librariesJsp = leaf("libraries.jsp");
		public final PathNode memoryJsp = leaf("memory.jsp");
		public final PathNode memoryPoolJsp = leaf("memoryPool.jsp");
		public final PathNode memoryUsageJsp = leaf("memoryUsage.jsp");
	}

	public static class JspAuthenticationFiles extends PathNode{
		public final PathNode accountManagerJsp = leaf("accountManager.jsp");
		public final PathNode createUserFormJsp = leaf("createUserForm.jsp");
		public final PathNode editUserFormJsp = leaf("editUserForm.jsp");
		public final PathNode permissionRequestJsp = leaf("permissionRequest.jsp");
		public final PathNode resetPasswordFormJsp = leaf("resetPasswordForm.jsp");
		public final PathNode signinFormJsp = leaf("signinForm.jsp");
		public final PathNode viewUsersJsp = leaf("viewUsers.jsp");
	}

	public static class JspCssFiles extends PathNode{
		public final PathNode cssImportJspf = leaf("css-import.jspf");
	}

	public static class JspDocsFiles extends PathNode{
		public final PathNode dispatcherDocsJsp = leaf("dispatcherDocs.jsp");
	}

	public static class JspGenericFiles extends PathNode{
		public final PathNode baseHeadJsp = leaf("baseHead.jsp");
		public final PathNode codeJsp = leaf("code.jsp");
		public final PathNode datarouterHeadJsp = leaf("datarouterHead.jsp");
		public final PathNode jsonJsp = leaf("json.jsp");
		public final PathNode messageJsp = leaf("message.jsp");
		public final PathNode navbarJsp = leaf("navbar.jspf");
		public final PathNode preludeJsp = leaf("prelude.jspf");
		public final PathNode stringJsp = leaf("string.jsp");
	}

	public static class JspMenuFiles extends PathNode{
		public final PathNode commonNavbarJsp = leaf("common-navbar.jsp");
		public final PathNode drNavbarJsp = leaf("dr-navbar.jsp");
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


	@Guice
	public static class DatarouterWebFilesTests{

		@Inject
		private DatarouterWebFiles files;

		@Test
		public void printPaths(){
			files.paths().stream()
					.map(PathNode::toSlashedString)
					.forEach(logger::warn);
		}

		@Test
		public void testAuroraInstances(){
			Assert.assertEquals("/autocomplete/autocomplete.js",
					files.autocomplete.autocompleteJs.toSlashedString());
		}

		@Test
		public void testConstants(){
			Assert.assertEquals(JSP_CodeMav, files.jsp.generic.codeJsp.toSlashedString());
			Assert.assertEquals(JSP_JsonMav, files.jsp.generic.jsonJsp.toSlashedString());
			Assert.assertEquals(JSP_MessageMav, files.jsp.generic.messageJsp.toSlashedString());
			Assert.assertEquals(JSP_StringMav, files.jsp.generic.stringJsp.toSlashedString());
		}

		@Test(enabled = false)
		public void testFilesExist(){
			String root = SystemTool.getUserHome() + "/workspace/datarouter/datarouter-web"
					+ DatarouterWebFiles.ROOT_IN_WEBAPP;
			files.paths().stream()
					.map(PathNode::toSlashedString)
					.map(root::concat)
					.map(Paths::get)
					.map(Path::toFile)
					.forEach(FileTool::requireExists);
		}

	}

}
