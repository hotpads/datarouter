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
package io.datarouter.web.email;

import java.util.Date;

import javax.inject.Inject;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.email.html.EmailDto.EmailDtoBuilder;
import io.datarouter.web.test.DatarouterWebTestNgModuleFactory;

@Guice(moduleFactory = DatarouterWebTestNgModuleFactory.class)
public class DatarouterEmailServiceIntegrationTester{

	@Inject
	private DatarouterHtmlEmailService emailService;

	@Test
	public void trySendEmailTest(){
		var emailDto = new EmailDtoBuilder()
				.fromAdmin()
				.toAdmin()
				.withSubject(getClass().getName())
				.withContent("Hello there, it's " + new Date(), false);
		emailService.trySend(emailDto.build());
	}

}
