package com.hotpads.datarouter.config;

import java.util.concurrent.TimeUnit;




public class Config implements Cloneable{
	
	/****************** static vars *******************************/
	
	public static final Boolean DEFAULT_CACHE_OK = true;
	public static final Isolation DEFAULT_ISOLATION = Isolation.readCommitted;
	public static final Boolean DEFAULT_AUTO_COMMIT = false;
	
	/*************** fields ********************************/
	
	//i am trying to move away from setting any values here, so please don't add anything to the defaults!

	protected ConnectMethod connectMethod = ConnectMethod.tryExisting;
	protected Boolean useSession = true;
	
	//transactions
	protected Isolation isolation = DEFAULT_ISOLATION;
//	protected Boolean autoCommit;//HibernateExecutor assumes this to be null unless explicitly set to false
	
	//slaves
	protected Boolean slaveOk = false;
	
	//put options
	protected PutMethod putMethod = PutMethod.SELECT_FIRST_OR_LOOK_AT_PRIMARY_KEY;
	protected Boolean ignoreNullFields = false;
	protected Integer commitBatchSize;
	protected Boolean persistentPut = true;

	//table scans
	protected Boolean scannerCaching = true;
	protected Integer iterateBatchSize;
	
	//retrying
	protected Long timeoutMs;
	protected Integer numAttempts;//do not set default here.  do it per-client

	//paging
	protected Object startId;
	protected Boolean includeStartId = false;
	protected Integer limit;
	protected Integer offset;
	
	//caching
	protected Boolean cacheOk = DEFAULT_CACHE_OK;
	protected Long cacheTimeoutMs = Long.MAX_VALUE;
	
	
	/******************* clone ******************************************/
	
	@Override
	public Config clone() throws CloneNotSupportedException{
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
			
			.setStartId(startId)
			.setIncludeStartId(includeStartId)
			.setLimit(limit)
			.setOffset(offset)
			
			.setCacheOk(cacheOk)
			.setCacheTimeoutMs(cacheTimeoutMs);
		
		return clone;
	}
	
	
	/***************** constructors ********************************/
	
	public static Config create(){
		Config config = new Config();
		return config;
	}
	
	public static Config nullSafe(Config in){
		if(in != null){ return in; }
		return new Config();
	}
	
	
	/********************* accessors **************************************/
	

	public Isolation getIsolationOrUse(Isolation theDefault) {
		if(isolation==null){ return theDefault; }
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
	
	public Integer getLimitOrUse(int pLimit){
		if(limit==null){ return pLimit; }
		return limit;
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
	
	public Integer getIterateBatchSizeOverrideNull(Integer overrideIfNull){
		return iterateBatchSize == null ? overrideIfNull : iterateBatchSize;
	}

	public Config setIterateBatchSize(Integer iterateBatchSize) {
		this.iterateBatchSize = iterateBatchSize;
		return this;
	}

	public Config setIterateBatchSizeIfNull(Integer iterateBatchSize) {
		if(this.iterateBatchSize!=null){ return this; }
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


	/************** startId *************************/

	public Object getStartId() {
		return startId;
	}

	public Config setStartId(Object startId) {
		this.startId = startId;
		return this;
	}


	/************** includeStartId *************************/

	public Boolean getIncludeStartId() {
		return includeStartId;
	}

	public Config setIncludeStartId(Boolean includeStartId) {
		this.includeStartId = includeStartId;
		return this;
	}


	/************** includeStartId *************************/

	public Integer getNumAttempts() {
		return numAttempts;
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
	
	
	
}
