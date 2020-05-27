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
package io.datarouter.aws.secretsmanager;

public interface AwsCredentialsSupplier{

	String getDevAccessKey();
	String getDevSecretKey();

	String getProdAccessKey();
	String getProdSecretKey();

	class DatarouterAwsCredentials implements AwsCredentialsSupplier{

		private final String devAccessKey;
		private final String devSecretKey;
		private final String prodAccessKey;
		private final String prodSecretkey;

		public DatarouterAwsCredentials(String devAccessKey, String devSecretKey, String prodAccessKey,
				String prodSecretkey){
			this.devAccessKey = devAccessKey;
			this.devSecretKey = devSecretKey;
			this.prodAccessKey = prodAccessKey;
			this.prodSecretkey = prodSecretkey;
		}

		@Override
		public String getDevAccessKey(){
			return devAccessKey;
		}

		@Override
		public String getDevSecretKey(){
			return devSecretKey;
		}


		@Override
		public String getProdAccessKey(){
			return prodAccessKey;
		}

		@Override
		public String getProdSecretKey(){
			return prodSecretkey;
		}

	}

}
