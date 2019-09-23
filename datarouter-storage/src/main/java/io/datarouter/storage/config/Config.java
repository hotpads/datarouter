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
package io.datarouter.storage.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.datarouter.util.Require;
import io.datarouter.util.lang.LineOfCode;

public class Config implements Cloneable{

	public static final int DEFAULT_INPUT_BATCH_SIZE = 100;
	public static final int DEFAULT_OUTPUT_BATCH_SIZE = 100;
	public static final Boolean DEFAULT_CACHE_OK = true;

	/*-------------------------------- fields -------------------------------*/

	//trying to move away from setting any values here, so please don't add anything to the defaults

	private Boolean useSession = true;

	//slaves
	private Boolean slaveOk = false;

	//put options
	private PutMethod putMethod = PutMethod.DEFAULT_PUT_METHOD;
	private Boolean ignoreNullFields;
	private Integer commitBatchSize;
	private Boolean persistentPut = true;

	//table scans
	private Boolean scannerCaching;
	private Integer inputBatchSize;
	private Integer outputBatchSize;

	//error handling
	private Boolean ignoreException;

	//retrying
	private Long timeoutMs = Duration.ofMinutes(10).toMillis();
	private Integer numAttempts;//do not set default here.  do it per-client

	//paging
	private Integer limit;//TODO use Long
	private Integer offset;//TODO use Long

	//caching
	private Boolean cacheOk = DEFAULT_CACHE_OK;
	private Long ttlMs;// = 0L;//infinite

	//messaging
	private Long visibilityTimeoutMs;

	//callsite tracing
	private LineOfCode callsite;
	private LineOfCode customCallsite;

	private final Map<ConfigKey<?>,ConfigValue<?>> configuration;

	public Config(){
		this.configuration = new HashMap<>();
	}

	public Config addOption(ConfigValue<?> option){
		configuration.put(option.getKey(), option);
		return this;
	}

	@SuppressWarnings("unchecked")
	public final <T extends ConfigValue<T>> Optional<T> getOption(ConfigKey<T> key){
		return Optional.ofNullable((T)configuration.get(key));
	}

	/*-------------------------------- clone --------------------------------*/

	@Override
	public Config clone(){
		return getDeepCopy();
	}

	public Config getDeepCopy(){//get rid of the checked exception in the clone method
		Config clone = new Config();
		clone
			.setUseSession(useSession)

			.setSlaveOk(slaveOk)

			.setPutMethod(putMethod)
			.setIgnoreNullFields(ignoreNullFields)
			.setCommitBatchSize(commitBatchSize)
			.setPersistentPut(persistentPut)

			.setScannerCaching(scannerCaching)
			.setInputBatchSize(inputBatchSize)
			.setOutputBatchSize(outputBatchSize)

			.setIgnoreException(ignoreException)

			.setTimeoutMs(timeoutMs)
			.setNumAttempts(numAttempts)

			.setLimit(limit)
			.setOffset(offset)

			.setCacheOk(cacheOk)
			.setTtlMs(ttlMs)

			.setVisibilityTimeoutMs(visibilityTimeoutMs)

			.setCallsite(callsite)
			.setCustomCallsite(customCallsite);
		configuration.values().forEach(clone::addOption);
		return clone;
	}

	/*-------------------------------- limit --------------------------------*/

	public Integer getLimit(){
		return limit;
	}

	public Optional<Integer> optLimit(){
		return Optional.ofNullable(limit);
	}

	public Config setLimit(Integer limit){
		this.limit = limit;
		return this;
	}

	/*-------------------------------- offset -------------------------------*/

	public Integer getOffset(){
		return offset;
	}

	public Optional<Integer> optOffset(){
		return Optional.ofNullable(offset);
	}

	public Config setOffset(Integer offset){
		this.offset = offset;
		return this;
	}

	/*------------------------ batch size ---------------------------*/

	public Optional<Integer> optInputBatchSize(){
		return Optional.ofNullable(inputBatchSize);
	}

	public Config setInputBatchSize(Integer inputBatchSize){
		Require.isTrue(inputBatchSize == null || inputBatchSize > 0);
		this.inputBatchSize = inputBatchSize;
		return this;
	}

	public Optional<Integer> optOutputBatchSize(){
		return Optional.ofNullable(outputBatchSize);
	}

	public Config setOutputBatchSize(Integer outputBatchSize){
		Require.isTrue(outputBatchSize == null || outputBatchSize > 0);
		this.outputBatchSize = outputBatchSize;
		return this;
	}

	/*------------------------ commit batch size ----------------------------*/

	public Integer getCommitBatchSize(){
		return commitBatchSize;
	}

	public Config setCommitBatchSize(Integer commitBatchSize){
		this.commitBatchSize = commitBatchSize;
		return this;
	}

	/*---------------------------- slave ok ---------------------------------*/

	public Boolean getSlaveOk(){
		return slaveOk;
	}

	public Config setSlaveOk(Boolean slaveOk){
		this.slaveOk = slaveOk;
		return this;
	}

	/*---------------------------- cache ok ---------------------------------*/

	public Boolean getCacheOk(){
		return cacheOk;
	}

	public Config setCacheOk(Boolean cacheOk){
		this.cacheOk = cacheOk;
		return this;
	}

	/*---------------------------- use session ------------------------------*/

	public Boolean getUseSession(){
		return useSession;
	}

	public Config setUseSession(Boolean useSession){
		this.useSession = useSession;
		return this;
	}

	/*------------------------- include start id ----------------------------*/

	public Integer getNumAttempts(){
		return numAttempts;
	}

	public Integer getNumAttemptsOrUse(int alternative){
		if(numAttempts != null){
			return numAttempts;
		}
		return alternative;
	}

	public Config setNumAttempts(Integer numAttempts){
		this.numAttempts = numAttempts;
		return this;
	}

	/*---------------------------- timeoutMs --------------------------------*/

	public Long getTimeoutMs(){
		return timeoutMs;
	}

	public long getTimeoutMsOrUse(long alternative){
		return timeoutMs == null ? alternative : timeoutMs;
	}

	public Config setTimeoutMs(Long timeoutMs){
		this.timeoutMs = timeoutMs;
		return this;
	}

	public Config setTimeout(Integer timeout, TimeUnit timeUnit){
		setTimeoutMs(timeUnit.toMillis(timeout));
		return this;
	}

	public Config setNoTimeout(){
		setTimeoutMs(Long.MAX_VALUE);
		return this;
	}

	/*---------------------------- put method -------------------------------*/

	public PutMethod getPutMethod(){
		return putMethod;
	}

	public Config setPutMethod(PutMethod putMethod){
		this.putMethod = putMethod;
		return this;
	}

	/*--------------------------- ignore null fields ------------------------*/

	public Optional<Boolean> optIgnoreNullFields(){
		return Optional.ofNullable(ignoreNullFields);
	}

	public Config setIgnoreNullFields(Boolean ignoreNullFields){
		this.ignoreNullFields = ignoreNullFields;
		return this;
	}

	/*------------------------- scanner caching -----------------------------*/

	public Optional<Boolean> optScannerCaching(){
		return Optional.ofNullable(scannerCaching);
	}

	public Config setScannerCaching(Boolean scannerCaching){
		this.scannerCaching = scannerCaching;
		return this;
	}

	/*---------------------------- persistent put ---------------------------*/

	public Boolean getPersistentPut(){
		return persistentPut;
	}

	public Config setPersistentPut(Boolean persistentPut){
		this.persistentPut = persistentPut;
		return this;
	}


	/*------------------------------ ttlMs ----------------------------------*/

	public Long getTtlMs(){
		return ttlMs;
	}

	public Config setTtlMs(Long ttlMs){
		this.ttlMs = ttlMs;
		return this;
	}

	/*---------------------------- messaging --------------------------------*/

	public long getVisibilityTimeoutMsOrUse(long alternative){
		if(visibilityTimeoutMs != null){
			return visibilityTimeoutMs;
		}
		return alternative;
	}

	public Long getVisibilityTimeoutMs(){
		return visibilityTimeoutMs;
	}

	public Config setVisibilityTimeoutMs(Long visibilityTimeoutMs){
		this.visibilityTimeoutMs = visibilityTimeoutMs;
		return this;
	}


	/*------------------------------- callsite ------------------------------*/

	public LineOfCode getCallsite(){
		return callsite;
	}

	public Config setCallsite(LineOfCode callsite){
		this.callsite = callsite;
		return this;
	}

	public LineOfCode getCustomCallsite(){
		return customCallsite;
	}

	public Config setCustomCallsite(LineOfCode customCallsite){
		this.customCallsite = customCallsite;
		return this;
	}

	/*-------------------------- error handling -----------------------------*/

	public Boolean getIgnoreException(){
		return ignoreException;
	}

	public Config setIgnoreException(Boolean paramIgnoreException){
		this.ignoreException = paramIgnoreException;
		return this;
	}

	public Boolean ignoreExceptionOrUse(Boolean alternative){
		if(ignoreException != null){
			return ignoreException;
		}
		return alternative;
	}

}
