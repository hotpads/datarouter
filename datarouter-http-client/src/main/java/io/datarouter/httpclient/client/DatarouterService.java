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
package io.datarouter.httpclient.client;

import java.time.ZoneId;

public interface DatarouterService{

	String getServiceName();
	String getPrivateDomain();// includes host and port, like localhost:8443
	String getPublicDomain();// includes host and port, like localhost:8443
	String getContextName();// root path segment

	default String getDomainPreferPrivate(){
		return getPrivateDomain() != null ? getPrivateDomain() : getPublicDomain();
	}

	default String getDomainPreferPublic(){
		return getPublicDomain() != null ? getPublicDomain() : getPrivateDomain();
	}

	default boolean hasPublicDomain(){
		return getPublicDomain() != null;
	}

	default boolean hasPrivateDomain(){
		return getPrivateDomain() != null;
	}

	default String getContextPath(){
		return getContextName() == null ? "" : "/" + getContextName();
	}

	default ZoneId getZoneId(){
		return ZoneId.systemDefault();
	}

	class NoOpDatarouterService implements DatarouterService{

		@Override
		public String getServiceName(){
			return "";
		}

		@Override
		public String getPrivateDomain(){
			return "";
		}

		@Override
		public String getPublicDomain(){
			return "";
		}

		@Override
		public String getContextName(){
			return "";
		}

	}

}
