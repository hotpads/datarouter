package com.hotpads.datarouter.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.util.core.lang.LineOfCode;


public class Config
extends BaseDatabean<ConfigKey,Config>
implements Cloneable{

	/****************** static vars *******************************/

	public static final int DEFAULT_ITERATE_BATCH_SIZE = 100;
	public static final Boolean DEFAULT_CACHE_OK = true;
	public static final Isolation DEFAULT_ISOLATION = Isolation.readCommitted;
	public static final Boolean DEFAULT_AUTO_COMMIT = false;
	public static final Integer LENGTH_CALLSITE = MySqlColumnType.INT_LENGTH_LONGTEXT;


	/*************** fields ********************************/

	private ConfigKey key;

	//i am trying to move away from setting any values here, so please don't add anything to the defaults!

	private ConnectMethod connectMethod = ConnectMethod.tryExisting;
	private Boolean useSession = true;

	//transactions
	private Isolation isolation = DEFAULT_ISOLATION;

	//slaves
	private Boolean slaveOk = false;

	//put options
	private PutMethod putMethod = PutMethod.DEFAULT_PUT_METHOD;
	private Boolean ignoreNullFields = false;
	private Integer commitBatchSize;
	private Boolean persistentPut = true;

	//table scans
	private Boolean scannerCaching = true;
	private Integer iterateBatchSize = DEFAULT_ITERATE_BATCH_SIZE;

	//retrying
	private Long timeoutMs = Duration.ofMinutes(10).toMillis();
	private Integer numAttempts;//do not set default here.  do it per-client

	//paging
	private Integer limit;
	private Integer offset;

	//caching
	private Boolean cacheOk = DEFAULT_CACHE_OK;
	private Long cacheTimeoutMs = 0L;//infinite

	//callsite tracing
	private LineOfCode callsite;
	private LineOfCode customCallsite;


	/**************************** columns *******************************/

	public static final Integer
		LEN_default = StringField.DEFAULT_STRING_LENGTH;

	public static class F{
		public static final String
			KEY_key = "key",
			connectMethod = "connectMethod",
			useSession = "useSession",
			isolation = "isolation",
			slaveOk = "slaveOk",
			putMethod = "putMethod",
			ignoreNullFields = "ignoreNullFields",
			commitBatchSize = "commitBatchSize",
			persistentPut = "persistentPut",
			scannerCaching = "scannerCaching",
			iterateBatchSize = "iterateBatchSize",
			timeoutMs = "timeoutMs",
			numAttempts = "numAttempts",
			limit = "limit",
			offset = "offset",
			cacheOk = "cacheOk",
			cacheTimeoutMs = "cacheTimeoutMs",
			callsite = "callsite",
			customCallsite = "customCallsite"
			;
	}

	public static class ConfigFielder extends BaseDatabeanFielder<ConfigKey,Config>{

		public ConfigFielder(){
			super(ConfigKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Config config){
			return Arrays.asList(
					new StringEnumField<>(ConnectMethod.class, F.connectMethod, config.connectMethod, LEN_default),
					new BooleanField(F.useSession, config.useSession),
					new StringEnumField<>(Isolation.class, F.isolation, config.isolation, LEN_default),
					new BooleanField(F.slaveOk, config.slaveOk),
					new StringEnumField<>(PutMethod.class, F.putMethod, config.putMethod, LEN_default),
					new BooleanField(F.ignoreNullFields, config.ignoreNullFields),
					new UInt31Field(F.commitBatchSize, config.commitBatchSize),
					new BooleanField(F.persistentPut, config.persistentPut),
					new BooleanField(F.scannerCaching, config.scannerCaching),
					new UInt31Field(F.iterateBatchSize, config.iterateBatchSize),
					new UInt63Field(F.timeoutMs, config.timeoutMs),
					new UInt31Field(F.numAttempts, config.numAttempts),
					new UInt31Field(F.limit, config.limit),
					new UInt31Field(F.offset, config.offset),
					new BooleanField(F.cacheOk, config.cacheOk),
					new UInt63Field(F.cacheTimeoutMs, config.cacheTimeoutMs),
					new StringField(F.callsite, config.callsite.getPersistentString(), LENGTH_CALLSITE),
					new StringField(F.customCallsite, config.customCallsite.getPersistentString(), LENGTH_CALLSITE)
					);
		}
	}


	/******************* clone ******************************************/

	@Override
	public Config clone(){
		return getDeepCopy();
	}

	public Config getDeepCopy(){//get rid of the checked exception in the clone method
		Config clone = new Config();
		clone
			.setConnectMethod(connectMethod)
			.setUseSession(useSession)

			.setIsolation(isolation)

			.setSlaveOk(slaveOk)

			.setPutMethod(putMethod)
			.setIgnoreNullFields(ignoreNullFields)
			.setCommitBatchSize(commitBatchSize)
			.setPersistentPut(persistentPut)

			.setScannerCaching(scannerCaching)
			.setIterateBatchSize(iterateBatchSize)

			.setTimeoutMs(timeoutMs)
			.setNumAttempts(numAttempts)

			.setLimit(limit)
			.setOffset(offset)

			.setCacheOk(cacheOk)
			.setCacheTimeoutMs(cacheTimeoutMs)

			.setCallsite(callsite)
			.setCustomCallsite(customCallsite);

		return clone;
	}


	/************************** databean **************************************/

	@Override
	public Class<ConfigKey> getKeyClass() {
		return ConfigKey.class;
	};

	@Override
	public ConfigKey getKey() {
		return key;
	}


	/***************** constructors ********************************/

	public Config(){
		this.key = new ConfigKey();
	}

	public static Config create(){
		Config config = new Config();
		return config;
	}

	public static Config nullSafe(Config in){
		if (in != null) {
			return in;
		}
		return new Config();
	}


	/********************* accessors **************************************/


	public Isolation getIsolationOrUse(Isolation theDefault) {
		if (isolation == null) {
			return theDefault;
		}
		return isolation;
	}

	public Isolation getIsolation() {
		return isolation;
	}

	public Config setIsolation(Isolation isolation) {
		this.isolation = isolation;
		return this;
	}


	/**************** connectMethod **********************/

	public ConnectMethod getConnectMethod() {
		return connectMethod;
	}

	public Config setConnectMethod(ConnectMethod connectMethod) {
		this.connectMethod = connectMethod;
		return this;
	}


	/**************** limit **********************/

	public Integer getLimit() {
		return limit;
	}

	public Integer getLimitOrUse(int alternative){
		if (limit != null) {
			return limit;
		}
		return alternative;
	}

	public Config setLimit(Integer limit) {
		this.limit = limit;
		return this;
	}


	/**************** offset **********************/

	public Integer getOffset() {
		return offset;
	}

	public Config setOffset(Integer offset) {
		this.offset = offset;
		return this;
	}


	/**************** iterateBatchSize **********************/

	public Integer getIterateBatchSize() {
		return iterateBatchSize;
	}

	public Config setIterateBatchSize(Integer iterateBatchSize) {
		this.iterateBatchSize = iterateBatchSize;
		return this;
	}

	/************** commitBatchSize *************************/

	public Integer getCommitBatchSize() {
		return commitBatchSize;
	}

	public Config setCommitBatchSize(Integer commitBatchSize) {
		this.commitBatchSize = commitBatchSize;
		return this;
	}


	/************** slaveOk *************************/

	public Boolean getSlaveOk() {
		return slaveOk;
	}

	public Config setSlaveOk(Boolean slaveOk) {
		this.slaveOk = slaveOk;
		return this;
	}


	/************** cacheOk *************************/

	public Boolean getCacheOk() {
		return cacheOk;
	}

	public Config setCacheOk(Boolean cacheOk) {
		this.cacheOk = cacheOk;
		return this;
	}


	/************** useSession *************************/

	public Boolean getUseSession() {
		return useSession;
	}
	public Config setUseSession(Boolean useSession) {
		this.useSession = useSession;
		return this;
	}


	/************** includeStartId *************************/

	public Integer getNumAttempts() {
		return numAttempts;
	}

	public Integer getNumAttemptsOrUse(int alternative){
		if (numAttempts != null){
			return numAttempts;
		}
		return alternative;
	}

	public Config setNumAttempts(Integer numAttempts) {
		this.numAttempts = numAttempts;
		return this;
	}


	/************** timeoutMs *************************/

	public Long getTimeoutMs() {
		return timeoutMs;
	}

	public Config setTimeoutMs(Long timeoutMs) {
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


	/************** putMethod *************************/

	public PutMethod getPutMethod() {
		return putMethod;
	}

	public Config setPutMethod(PutMethod putMethod) {
		this.putMethod = putMethod;
		return this;
	}


	/************** ignoreNullFields *************************/

	public Boolean getIgnoreNullFields() {
		return ignoreNullFields;
	}

	public Config setIgnoreNullFields(Boolean ignoreNullFields) {
		this.ignoreNullFields = ignoreNullFields;
		return this;
	}


	/************** scannerCaching *************************/

	public Boolean getScannerCaching() {
		return scannerCaching;
	}

	public Config setScannerCaching(Boolean scannerCaching) {
		this.scannerCaching = scannerCaching;
		return this;
	}


	/************** persistentPut *************************/

	public Boolean getPersistentPut(){
		return persistentPut;
	}

	public Config setPersistentPut(Boolean persistentPut){
		this.persistentPut = persistentPut;
		return this;
	}


	/************** cacheTimeoutMs *************************/

	public Long getCacheTimeoutMs(){
		return cacheTimeoutMs;
	}

	public Config setCacheTimeoutMs(Long cacheTimeoutMs){
		this.cacheTimeoutMs = cacheTimeoutMs;
		return this;
	}


	/************* callsite ******************************/

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
}
