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

import java.util.List;
import java.util.Optional;

import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.Secret;
import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.service.SecretNamespacer;
import io.datarouter.util.Require;

/**
 * adds or removes the secret namespace determined by {@link SecretNamespacer} and {@link SecretOpConfig}
 * @param <T> type that holds the name. implementations must override mapping functions between T and a String Scanner
 */
public abstract class NamespacingAdapter<T> implements SecretOpAdapter<T,T>{

	private final String namespace;
	private final NamespacingMode mode;

	protected NamespacingAdapter(SecretNamespacer secretNamespacer, SecretOpConfig config, NamespacingMode mode){
		Require.noNulls(secretNamespacer, config, mode);
		this.namespace = secretNamespacer.getConfigNamespace(config);
		this.mode = mode;
	}

	@Override
	public T adapt(T input){
		if(namespace.isEmpty()){
			return input;
		}
		var names = getNames(input);
		var newNames = mode == NamespacingMode.REMOVING ? removeNamespaces(names) : addNamespaces(names);
		return buildOutput(newNames, input);
	}

	public String getNamespace(){
		return namespace;
	}

	protected abstract Scanner<String> getNames(T input);
	protected abstract T buildOutput(Scanner<String> newNames, T input);

	private Scanner<String> addNamespaces(Scanner<String> names){
		return names.map(namespace::concat);
	}

	private Scanner<String> removeNamespaces(Scanner<String> names){
		return names
				.each(name -> {
					if(!name.startsWith(namespace)){
						throw new RuntimeException("missing namespace. name=" + name + " namespace=" + namespace);
					}
				}).map(name -> name.substring(namespace.length()));
	}

	public static enum NamespacingMode{
		ADDING,
		REMOVING,
		;
	}

	public static class StringNamespacingAdapter extends NamespacingAdapter<String>{

		public StringNamespacingAdapter(SecretNamespacer secretNamespacer, SecretOpConfig config, NamespacingMode mode){
			super(secretNamespacer, config, mode);
		}

		@Override
		protected Scanner<String> getNames(String input){
			return Scanner.of(input);
		}

		@Override
		protected String buildOutput(Scanner<String> newNames, String input){
			return newNames.findFirst().get();
		}

	}

	public static class OptionalStringNamespacingAdapter extends NamespacingAdapter<Optional<String>>{

		public OptionalStringNamespacingAdapter(SecretNamespacer secretNamespacer, SecretOpConfig config,
				NamespacingMode mode){
			super(secretNamespacer, config, mode);
		}

		@Override
		protected Scanner<String> getNames(Optional<String> input){
			return Scanner.of(input.orElse(""));
		}

		@Override
		protected Optional<String> buildOutput(Scanner<String> newNames, Optional<String> input){
			return Optional.of(newNames.findFirst().get());
		}

	}

	public static class SecretNamespacingAdapter extends NamespacingAdapter<Secret>{

		public SecretNamespacingAdapter(SecretNamespacer secretNamespacer, SecretOpConfig config, NamespacingMode mode){
			super(secretNamespacer, config, mode);
		}

		@Override
		protected Scanner<String> getNames(Secret input){
			return Scanner.of(input.getName());
		}

		@Override
		protected Secret buildOutput(Scanner<String> newNames, Secret input){
			return new Secret(newNames.findFirst().get(), input.getValue());
		}

	}

	public static class ListNamespacingAdapter extends NamespacingAdapter<List<String>>{

		public ListNamespacingAdapter(SecretNamespacer secretNamespacer, SecretOpConfig config, NamespacingMode mode){
			super(secretNamespacer, config, mode);
		}

		@Override
		protected Scanner<String> getNames(List<String> input){
			return Scanner.of(input);
		}

		@Override
		protected List<String> buildOutput(Scanner<String> newNames, List<String> input){
			return newNames.list();
		}

	}

}