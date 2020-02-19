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
package io.datarouter.web.user.authenticate;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import javax.inject.Singleton;

public interface PermissionRequestAdditionalEmailsSupplier extends Supplier<Set<String>>{

	@Singleton
	static class NoOpPermissionRequestAdditionalEmails implements PermissionRequestAdditionalEmailsSupplier{

		@Override
		public Set<String> get(){
			return Collections.emptySet();
		}

	}

	@Singleton
	static class PermissionRequestAdditionalEmails implements PermissionRequestAdditionalEmailsSupplier{

		private final Set<String> additionalPermissionRequestEmailAddress;

		public PermissionRequestAdditionalEmails(Set<String> additionalPermissionRequestEmailAddress){
			this.additionalPermissionRequestEmailAddress = additionalPermissionRequestEmailAddress;
		}

		@Override
		public Set<String> get(){
			return additionalPermissionRequestEmailAddress;
		}

	}

}
