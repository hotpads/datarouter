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
package io.datarouter.secret.op;

import java.util.Optional;

import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.config.SecretClientSupplierConfig;
import io.datarouter.secret.config.SecretClientSupplierConfigHolder;
import io.datarouter.secret.op.adapter.TypedSecret;
import io.datarouter.secret.op.client.SecretClientOp;
import io.datarouter.util.Require;

/**
 * Holds type and op agnostic configuration for {@link SecretOp}s. See {@link Builder} for field information.
 */
public class SecretOpConfig{

	public final SecretOpConfig.Namespace namespaceType;
	public final String manualNamespace;
	public final SecretOpReason reason;
	public final boolean shouldLog;
	public final boolean shouldSkipSerialization;
	public final boolean shouldApplyToAllClients;
	public final Optional<String> targetSecretClientConfig;

	private SecretOpConfig(
			SecretOpConfig.Namespace namespaceType,
			String manualNamespace,
			SecretOpReason reason,
			boolean shouldLog,
			boolean shouldSkipSerialization,
			boolean shouldApplyToAllSuppliers,
			Optional<String> targetSecretClientConfig){
		this.namespaceType = namespaceType;
		this.manualNamespace = manualNamespace;
		this.reason = reason;
		this.shouldLog = shouldLog;
		this.shouldSkipSerialization = shouldSkipSerialization;
		this.shouldApplyToAllClients = shouldApplyToAllSuppliers;
		this.targetSecretClientConfig = targetSecretClientConfig;
	}

	/**
	 * creates a default {@link Builder}. Defaults are to use {@link Namespace#APP} namespace; to not specify a target
	 * {@link SecretClientSupplierConfig}; to stop execution after the first successful {@link SecretClientOp} result;
	 * and to perform all recording, logging, and serialization normally.
	 * @param reason reason for {@link SecretOp}
	 * @return {@link Builder}
	 */
	public static Builder builder(SecretOpReason reason){
		return new Builder(reason);
	}

	public static SecretOpConfig getDefault(SecretOpReason reason){
		return SecretOpConfig.builder(reason).build();
	}

	public static class Builder{

		private SecretOpConfig.Namespace namespaceType;
		private String manualNamespace;
		private final SecretOpReason reason;
		private boolean shouldLog;
		private boolean shouldApplyToAllSuppliers;
		private boolean shouldSkipSerialization;
		private Optional<String> targetSecretClientConfig;

		private Builder(SecretOpReason reason){
			this.namespaceType = Namespace.APP;
			this.manualNamespace = "";
			this.reason = reason;
			this.shouldLog = true;
			this.shouldSkipSerialization = false;
			this.shouldApplyToAllSuppliers = false;
			this.targetSecretClientConfig = Optional.empty();
		}

		public SecretOpConfig build(){
			return new SecretOpConfig(
					namespaceType,
					manualNamespace,
					reason,
					shouldLog,
					shouldSkipSerialization,
					shouldApplyToAllSuppliers,
					targetSecretClientConfig);
		}

		/**
		 * use the specified namespace. "/" will be appended if manualNamespace does not end with "/"
		 * @param manualNamespace cannot be null or empty
		 */
		public Builder useManualNamespace(String manualNamespace){
			String endSlash = !Require.notBlank(manualNamespace).endsWith("/") ? "/" : "";
			this.manualNamespace = manualNamespace + endSlash;
			this.namespaceType = Namespace.MANUAL;
			return this;
		}

		/**
		 * use {@link Namespace#SHARED}
		 */
		public Builder useSharedNamespace(){
			this.manualNamespace = "";
			this.namespaceType = Namespace.SHARED;
			return this;
		}

		/**
		 * only allow execution on the specified {@link SecretClientSupplierConfig}
		 */
		public Builder useTargetSecretClientConfig(Optional<String> targetSecretClientConfig){
			this.targetSecretClientConfig = Require.notNull(targetSecretClientConfig)
					.map(Require::notBlank);
			return this;
		}

		/**
		 * skip all logging. This can be useful to avoid noise when {@link SecretOp} failures can be handled externally.
		 */
		public Builder disableLogging(){
			this.shouldLog = false;
			return this;
		}

		/**
		 * skips {@link TypedSecret} serialization. This is FOR STRINGS ONLY, and is silently ignored for other types.
		 */
		public Builder disableSerialization(){
			this.shouldSkipSerialization = true;
			return this;
		}

		/**
		 * instead of allowing {@link SecretOp} execution to halt and succeed upon its first {@link SecretClientOp}
		 * success, execute and require success on every configured {@link SecretClient}s in the
		 * {@link SecretClientSupplierConfigHolder}
		 */
		public Builder applyToAllSuppliers(){
			this.shouldApplyToAllSuppliers = true;
			return this;
		}

	}

	public enum Namespace{
		APP,
		SHARED,
		MANUAL,
		;
	}

}
