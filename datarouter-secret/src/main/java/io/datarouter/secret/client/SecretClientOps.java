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
package io.datarouter.secret.client;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import io.datarouter.secret.op.SecretOpType;

public class SecretClientOps{

	public static final CreateOp CREATE = new CreateOp();
	public static final ReadOp READ = new ReadOp();
	public static final UpdateOp UPDATE = new UpdateOp();
	public static final DeleteOp DELETE = new DeleteOp();
	public static final ListNamesOp LIST = new ListNamesOp();
	public static final PutOp PUT = new PutOp();

	public static final class CreateOp extends SecretClientOp<Secret,Void>{

		private CreateOp(){
			super(
					"validateSecret",
					"create",
					(client, input) -> client.validateSecret(input),
					SecretClientOp.wrapVoid((client, input) -> client.create(input)),
					SecretOpType.CREATE);
		}

	}

	public static class ReadOp extends SecretClientOp<String,Secret>{

		private ReadOp(){
			super(
					"validateName",
					"read",
					(client, input) -> client.validateName(input),
					(client, input) -> client.read(input),
					SecretOpType.READ);
		}

	}

	public static class UpdateOp extends SecretClientOp<Secret,Void>{

		private UpdateOp(){
			super(
					"validateSecret",
					"update",
					(client, input) -> client.validateSecret(input),
					SecretClientOp.wrapVoid((client, input) -> client.update(input)),
					SecretOpType.UPDATE);
		}

	}

	public static class DeleteOp extends SecretClientOp<String,Void>{

		private DeleteOp(){
			super(
					"validateName",
					"delete",
					(client, input) -> client.validateName(input),
					SecretClientOp.wrapVoid((client, input) -> client.delete(input)),
					SecretOpType.DELETE);
		}

	}

	public static class ListNamesOp extends SecretClientOp<Optional<String>,List<String>>{

		private ListNamesOp(){
			super(
					"list",
					(client, input) -> client.listNames(input),
					SecretOpType.LIST);
		}

	}

	public static class PutOp extends SecretClientOp<Secret,Void>{

		private PutOp(){
			super(
					"validateSecret",
					"create",
					(client, input) -> client.validateSecret(input),
					getPutFunction(),
					SecretOpType.PUT);
		}

		private static BiFunction<SecretClient,Secret,Void> getPutFunction(){
			return (client, input) -> {
				try{
					client.create(input);
				}catch(RuntimeException e){
					client.update(input);
				}
				return null;
			};
		}

	}

}
