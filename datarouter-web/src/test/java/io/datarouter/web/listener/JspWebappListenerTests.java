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
package io.datarouter.web.listener;

import org.testng.Assert;
import org.testng.annotations.Test;

public class JspWebappListenerTests{

	@Test
	public void testServletContextString(){
		//used by prelude.jspf
		Assert.assertEquals(JspWebappListener.MAV_PROPERTIES_FACTORY, "mavPropertiesFactory");
		//used by css-import-b3.jspf
		Assert.assertEquals(JspWebappListener.CSS_IMPORT_CONTENT, "datarouterCssImportContent");
		//used by css-import-b4.jspf
		Assert.assertEquals(JspWebappListener.NEW_CSS_IMPORT_CONTENT, "datarouterNewCssImportContent");
		//used by baseHead-b3.jsp
		Assert.assertEquals(JspWebappListener.BASE_HEAD_CONTENT, "datarouterBaseHeadContent");
		//used by baseHead-b4.jsp
		Assert.assertEquals(JspWebappListener.NEW_BASE_HEAD_CONTENT, "datarouterNewBaseHeadContent");
		//used by common-navbar-b3.jsp, common-navbar-b4.jsp
		Assert.assertEquals(JspWebappListener.NAVBAR_FACTORY, "datarouterNavbarFactory");
	}

}
