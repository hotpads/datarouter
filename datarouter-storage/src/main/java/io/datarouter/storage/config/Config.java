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
package io.datarouter.storage.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.datarouter.util.Require;
import io.datarouter.util.lang.LineOfCode;

public class Config implements Cloneable{

	public static final int DEFAULT_REQUEST_BATCH_SIZE = 100;
	public static final int DEFAULT_RESPONSE_BATCH_SIZE = 100;

	/*-------------------------------- fields -------------------------------*/

	//trying to move away from setting any values here, so please don't add anything to the defaults

	private Boolean useSession = true;

	//staleness
	private Boolean anyDelay = false;

	//put options
	private PutMethod putMethod = PutMethod.DEFAULT_PUT_METHOD;
	private Boolean persistentPut = true;

	//table scans
	private Boolean scannerPrefetching;
	private Boolean scannerCaching;
	private Boolean allowUnsortedScan;
	private Integer requestBatchSize;
	private Integer responseBatchSize;

	//error handling
	private Boolean ignoreException;

	//retrying
	private Duration timeout;
	private Integer numAttempts;//do not set default here.  do it per-client

	//paging
	private Integer limit;//TODO use Long
	private Integer offset;//TODO use Long

	//caching
	private Duration ttl;// = null;//infinite

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

			.setAnyDelay(anyDelay)

			.setPutMethod(putMethod)
			.setPersistentPut(persistentPut)

			.setScannerCaching(scannerCaching)
			.setAllowUnsortedScan(allowUnsortedScan)
			.setRequestBatchSize(requestBatchSize)
			.setResponseBatchSize(responseBatchSize)

			.setIgnoreException(ignoreException)

			.setTimeout(timeout)
			.setNumAttempts(numAttempts)

			.setLimit(limit)
			.setOffset(offset)

			.setTtl(ttl)

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

	public Optional<Integer> findLimit(){
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

	public Optional<Integer> findOffset(){
		return Optional.ofNullable(offset);
	}

	public Config setOffset(Integer offset){
		this.offset = offset;
		return this;
	}

	/*---------------------------- batch size -------------------------------*/

	public Optional<Integer> findRequestBatchSize(){
		return Optional.ofNullable(requestBatchSize);
	}

	public Config setRequestBatchSize(Integer requestBatchSize){
		Require.isTrue(requestBatchSize == null || requestBatchSize > 0);
		this.requestBatchSize = requestBatchSize;
		return this;
	}

	public Optional<Integer> findResponseBatchSize(){
		return Optional.ofNullable(responseBatchSize);
	}

	public Config setResponseBatchSize(Integer responseBatchSize){
		Require.isTrue(responseBatchSize == null || responseBatchSize > 0);
		this.responseBatchSize = responseBatchSize;
		return this;
	}

	/*---------------------------- staleness ---------------------------------*/

	public Boolean getAnyDelay(){
		return anyDelay;
	}

	public Config setAnyDelay(boolean anyDelay){
		this.anyDelay = anyDelay;
		return this;
	}

	public Config anyDelay(){
		return setAnyDelay(true);
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

	/*---------------------------- timeout ----------------------------------*/

	public Optional<Duration> findTimeout(){
		return Optional.ofNullable(timeout);
	}

	public Config setTimeout(Duration duration){
		this.timeout = duration;
		return this;
	}

	public Config setNoTimeout(){
		setTimeout(Duration.ofMillis(Long.MAX_VALUE));
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

	/*------------------------- scanner prefetching -----------------------------*/

	public Optional<Boolean> findScannerPrefetching(){
		return Optional.ofNullable(scannerPrefetching);
	}

	public Config setScannerPrefetching(Boolean scannerPrefetching){
		this.scannerPrefetching = scannerPrefetching;
		return this;
	}

	/*------------------------- scanner caching -----------------------------*/

	public Optional<Boolean> findScannerCaching(){
		return Optional.ofNullable(scannerCaching);
	}

	public Config setScannerCaching(Boolean scannerCaching){
		this.scannerCaching = scannerCaching;
		return this;
	}

	/*------------------------- unsorted scan -----------------------------*/

	public Optional<Boolean> findAllowUnsortedScan(){
		return Optional.ofNullable(allowUnsortedScan);
	}

	public Config setAllowUnsortedScan(Boolean allowUnsortedScan){
		this.allowUnsortedScan = allowUnsortedScan;
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

	/*------------------------------ ttl ------------------------------------*/

	public Optional<Duration> findTtl(){
		return Optional.ofNullable(ttl);
	}

	public Config setTtl(Duration ttl){
		this.ttl = ttl;
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

	public Config setIgnoreException(Boolean paramIgnoreException){
		this.ignoreException = paramIgnoreException;
		return this;
	}

	public Optional<Boolean> findIgnoreException(){
		return Optional.ofNullable(ignoreException);
	}

}
