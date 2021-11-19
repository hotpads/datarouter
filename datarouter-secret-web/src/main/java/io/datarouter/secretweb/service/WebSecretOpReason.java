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
package io.datarouter.secretweb.service;

import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.op.SecretOpReason.SecretOpReasonType;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.user.session.service.Session;

public class WebSecretOpReason{

	public static SecretOpReason apiOp(String apiKey, String reason){
		Require.isTrue(StringTool.notEmptyNorWhitespace(apiKey));
		Require.isTrue(StringTool.notEmptyNorWhitespace(reason));
		return new SecretOpReason(SecretOpReasonType.API, null, null, apiKey, reason);
	}

	public static SecretOpReason manualOp(Session session, String reason){
		Require.noNulls(session, session.getUserToken(), session.getUsername());
		Require.isTrue(StringTool.notEmptyNorWhitespace(session.getUserToken()));
		Require.isTrue(StringTool.notEmptyNorWhitespace(session.getUsername()));
		Require.isTrue(StringTool.notEmptyNorWhitespace(reason));
		return new SecretOpReason(
				SecretOpReasonType.MANUAL,
				session.getUsername(),
				session.getUserToken(),
				null,
				reason);
	}

}
