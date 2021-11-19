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
package io.datarouter.secret.op.adapter;

/**
 * Allows chaining together multiple {@link SecretOpAdapter}s in a typesafe way using
 * {@link SecretOpAdapterChain#chain(SecretOpAdapter)}
 *
 * @param <I> input of the chain
 * @param <O> output of the chain
 */
public class SecretOpAdapterChain<I,O> implements SecretOpAdapter<I,O>{

	private final SecretOpAdapter<I,O> adapter;

	public SecretOpAdapterChain(SecretOpAdapter<I,O> adapter){
		this.adapter = adapter;
	}

	public <T> SecretOpAdapterChain<I,T>chain(SecretOpAdapter<O,T> nextAdapter){
		return new SecretOpAdapterChain<>(new SecretOpAdapterChainLink<>(adapter, nextAdapter));
	}

	@Override
	public O adapt(I input){
		return adapter.adapt(input);
	}

	private static class SecretOpAdapterChainLink<I,M,O> implements SecretOpAdapter<I,O>{

		private final SecretOpAdapter<I,M> previous;
		private final SecretOpAdapter<M,O> current;

		private SecretOpAdapterChainLink(SecretOpAdapter<I,M> previous, SecretOpAdapter<M,O> current){
			this.previous = previous;
			this.current = current;
		}

		@Override
		public O adapt(I input){
			return current.adapt(previous.adapt(input));
		}

	}

}