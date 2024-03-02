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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.config.SecretClientSupplierConfig;
import io.datarouter.secret.config.SecretClientSupplierConfigHolder;
import io.datarouter.secret.exception.NoConfiguredSecretClientSupplierException;
import io.datarouter.secret.exception.SecretClientException;
import io.datarouter.secret.op.adapter.SecretOpAdapter;
import io.datarouter.secret.op.client.SecretClientOp;
import io.datarouter.secret.op.client.SecretClientOp.SecretClientOpResult;
import io.datarouter.secret.op.client.SecretClientOpType;
import io.datarouter.secret.service.SecretNamespacer;

/**
 * SecretOp encapsulates the configuration, execution, and result of a single secret operation instance. The op is
 * executed idempotently (and the result stored) by calling {@link SecretOp#execute()}, {@link SecretOp#getOutput()}, or
 * {@link SecretOp#getAllOutputs()}. {@link SecretClientException} and {@link NoConfiguredSecretClientSupplierException}
 * are thrown when success is not possible, and other {@link RuntimeException}s may be thrown in the case of bad
 * configuration.
 *
 * Built-in ops can easily be obtained from {@link SecretOpFactory}, but full flexibility is also allowed in SecretOp's
 * constructor. Execution involves configurable logging, recording, IO adapting and client targeting. See
 * {@link SecretOpConfig}, {@link SecretClientOp}, {@link SecretOpAdapter}, and {@link SecretClientSupplierConfigHolder}
 * for more information.
 *
 * @param <I> the convenient input type of this class
 * @param <CI> the input type of the {@link SecretClientOp}
 * @param <CO> the output type of the {@link SecretClientOp}
 * @param <O> the convenient output type of this class
 */
public class SecretOp<I,CI,CO,O>{
	private static final Logger logger = LoggerFactory.getLogger(SecretOp.class);

	private final String name;
	private final String namespace;
	private final I input;
	private final SecretOpConfig config;
	private final DatarouterInjector injector;
	private final SecretClientSupplierConfigHolder secretClientConfigHolder;
	private final boolean isDevelopment;
	private final SecretClientOp<CI,CO> clientOp;
	private final SecretOpAdapter<I,CI> clientInputAdapter;
	private final SecretOpAdapter<CO,O> clientOutputAdapter;

	//populated after execution
	private boolean wasExecuted = false;
	private List<SecretClientOpAttempt<CO>> attemptedOps = new ArrayList<>();

	/**
	 * @param name name of the secret
	 * @param namespacer used while determining secret namespace and {@link SecretClientSupplierConfig}s
	 * @param input input to be adapted
	 * @param config general configuration of available behavior and features, including targeting a specific client
	 * @param injector used to inject the {@link SecretClient}s
	 * @param secretClientConfigHolder configuration of available {@link SecretClient}s
	 * @param clientOp {@link SecretClientOp#validateAndExecute(SecretClient, Object)} will be invoked on this
	 * @param clientInputAdapter adapts I to clientOp
	 * @param clientOutputAdapter adapts clientOp's output to O
	 */
	public SecretOp(
			String name,
			SecretNamespacer namespacer,
			I input,
			SecretOpConfig config,
			DatarouterInjector injector,
			SecretClientSupplierConfigHolder secretClientConfigHolder,
			SecretClientOp<CI,CO> clientOp,
			SecretOpAdapter<I,CI> clientInputAdapter,
			SecretOpAdapter<CO,O> clientOutputAdapter){
		this.name = name;
		this.namespace = namespacer.getConfigNamespace(config);
		this.input = input;
		this.config = config;
		this.injector = injector;
		this.secretClientConfigHolder = secretClientConfigHolder;
		this.isDevelopment = namespacer.isDevelopment();
		this.clientOp = clientOp;
		this.clientInputAdapter = clientInputAdapter;
		this.clientOutputAdapter = clientOutputAdapter;
	}

	//multiple calls and multi-threaded usage are not expected, but this is idempotent and thread safe
	public synchronized SecretOp<I,CI,CO,O> execute(){
		if(wasExecuted){
			return this;
		}
		wasExecuted = true;
		CI secretClientInput = clientInputAdapter.adapt(input);

		for(Iterator<SecretClientSupplierConfig> iter = getSecretClientConfigs(); iter.hasNext();){
			SecretClientSupplierConfig supplierConfig = iter.next();
			var clientSupplier = injector.getInstance(supplierConfig.getSecretClientSupplierClass());
			SecretClientOpResult<CO> result = clientOp.validateAndExecute(clientSupplier.get(), secretClientInput);
			//TODO add supplier counters?
			attemptedOps.add(new SecretClientOpAttempt<>(supplierConfig, result));

			//give up on applying to all suppliers if one fails
			if(config.shouldApplyToAllClients && !result.isSuccess()){
				break;
			}
			//continue to next supplier if this one failed or if applying to all supplier should continue
			if(!result.isSuccess() || config.shouldApplyToAllClients){
				continue;
			}

			//success (short circuit the rest of the suppliers, if any)
			//log details if there were any failures (but skip that in dev)
			if(attemptedOps.size() > 1 && !isDevelopment){
				String attemptedSecretClientConfigs = Scanner.of(attemptedOps)
						.map(opAttempt -> opAttempt.secretClientConfig)
						.map(SecretClientSupplierConfig::getConfigName)
						.collect(Collectors.joining(","));
				logger.warn("Secret op succeeded after multiple attempts. attemptedSecretClientConfigs="
						+ attemptedSecretClientConfigs);
			}
			return this;
		}
		//all suppliers successfully applied
		if(!attemptedOps.isEmpty() && config.shouldApplyToAllClients && Scanner.of(attemptedOps)
				.allMatch(SecretClientOpAttempt::isSuccess)){
			return this;
		}
		//overall failure
		if(!attemptedOps.isEmpty()){
			attemptedOps.forEach(this::logError);
			//throw the most recent failed op exception
			throw attemptedOps.getLast().getException();
		}
		//nothing attempted because no suppliers are configured to handle this op
		throw new NoConfiguredSecretClientSupplierException(this);
	}

	public O getOutput(){
		if(!wasExecuted){
			execute();
		}
		return clientOutputAdapter.adapt(attemptedOps.getLast().getResult());
	}

	public Map<String,O> getAllOutputs(){
		if(!wasExecuted){
			execute();
		}
		return Scanner.of(attemptedOps)
				.toMap(
						opAttempt -> opAttempt.secretClientConfig.getConfigName(),
						opAttempt -> clientOutputAdapter.adapt(opAttempt.getResult()));
	}

	public SecretClientOpType getSecretClientOpType(){
		return clientOp.getOpType();
	}

	public String getName(){
		return name;
	}

	public String getNamespace(){
		return namespace;
	}

	public SecretOpReason getSecretOpReason(){
		return config.reason;
	}

	public Optional<String> getTargetSecretClientConfig(){
		return config.targetSecretClientConfig;
	}

	@Override
	public String toString(){
		return String.format("{op=%s, namespace=%s, name=%s, reason=\"%s\" targetSecretClientConfig=%s}",
				getSecretClientOpType(), getNamespace(), getName(), getSecretOpReason(), getTargetSecretClientConfig()
				.orElse(""));
	}

	private Iterator<SecretClientSupplierConfig> getSecretClientConfigs(){
		return secretClientConfigHolder.getAllowedConfigs(isDevelopment, this).iterator();
	}

	private void logError(SecretClientOpAttempt<CO> opAttempt){
		if(opAttempt.isSuccess() || !config.shouldLog){
			return;
		}
		logger.error(opAttempt.secretClientConfig.getConfigName() + " failed op", opAttempt.getException());
	}

	public static class SecretClientOpAttempt<T>{

		public final SecretClientSupplierConfig secretClientConfig;
		private final SecretClientOpResult<T> opResult;

		public SecretClientOpAttempt(SecretClientSupplierConfig secretClientConfig, SecretClientOpResult<T> opResult){
			this.secretClientConfig = secretClientConfig;
			this.opResult = opResult;
		}

		public boolean isSuccess(){
			return opResult.isSuccess();
		}

		public T getResult(){
			if(!opResult.isSuccess()){
				throw getException();
			}
			return opResult.result.orElse(null);//allows Void ops
		}

		public SecretClientException getException(){
			return opResult.exception.orElseGet(() -> new SecretClientException("unknown error"));
		}

	}

}
