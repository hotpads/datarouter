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
package io.datarouter.secret.client;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SecretClientOps{

	public static class CreateOp extends SecretClientOp<Secret,Void>{

		public CreateOp(SecretClient secretClient){
			super("validateSecret", "create", secretClient::validateSecret, SecretClientOp.wrapVoid(secretClient
					::create));
		}

	}

	public static class ReadOp extends SecretClientOp<String,Secret>{

		public ReadOp(SecretClient secretClient){
			super("validateName", "read", secretClient::validateName, secretClient::read);
		}

	}

	public static class UpdateOp extends SecretClientOp<Secret,Void>{

		public UpdateOp(SecretClient secretClient){
			super("validateSecret", "update", secretClient::validateSecret, SecretClientOp.wrapVoid(secretClient
					::update));
		}

	}

	public static class DeleteOp extends SecretClientOp<String,Void>{

		public DeleteOp(SecretClient secretClient){
			super("validateName", "delete", secretClient::validateName, SecretClientOp.wrapVoid(secretClient::delete));
		}

	}

	public static class ListNamesOp extends SecretClientOp<Optional<String>,List<String>>{

		public ListNamesOp(SecretClient secretClient){
			super("list", secretClient::listNames);
		}

	}

	public static class PutOp extends SecretClientOp<Secret,Void>{

		public PutOp(SecretClient secretClient){
			super("validateSecret", "create", secretClient::validateSecret, PutOp.getPutFunction(secretClient));
		}

		private static Function<Secret,Void> getPutFunction(SecretClient secretClient){
			return secret -> {
				try{
					secretClient.create(secret);
				}catch(RuntimeException e){
					secretClient.update(secret);
				}
				return null;
			};
		}

	}

}
