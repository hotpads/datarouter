package com.hotpads.datarouter.config;




public class Config {
	
	/****************** static vars *******************************/
	
	public static final Boolean DEFAULT_CACHE_OK = true;
	public static final Isolation DEFAULT_ISOLATION = Isolation.readCommitted;
	public static final Boolean DEFAULT_AUTO_COMMIT = false;
	
	/*************** fields ********************************/
	
	protected Integer numAttempts = 1;
	
	protected Boolean slaveOk = false;
	protected Boolean cacheOk = DEFAULT_CACHE_OK;

	protected ConnectMethod connectMethod = ConnectMethod.tryExisting;
	protected Isolation isolation = DEFAULT_ISOLATION;
	protected Boolean autoCommit;//HibernateExecutor assumes this to be null unless explicitly set to false
	protected Boolean useSession = true;
	
	protected PutMethod putMethod = PutMethod.SELECT_FIRST_OR_LOOK_AT_PRIMARY_KEY;
	protected Integer commitBatchSize;
	protected Integer iterateBatchSize;

	protected Object startId;
	protected Boolean includeStartId = false;
	protected Integer limit;
	protected Integer offset;
	
	
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


	public Boolean getAutoCommitOrUse(Boolean theDefault){
		if(autoCommit==null){ return theDefault; }
		return autoCommit;
	}
	public Boolean getAutoCommit(){
		return autoCommit;
	}
	public Config setAutoCommit(Boolean autoCommit){
		this.autoCommit = autoCommit;
		return this;
	}

	public ConnectMethod getConnectMethod() {
		return connectMethod;
	}


	public Config setConnectMethod(ConnectMethod connectMethod) {
		this.connectMethod = connectMethod;
		return this;
	}


	public Integer getLimit() {
		return limit;
	}


	public Config setLimit(Integer limit) {
		this.limit = limit;
		return this;
	}


	public Integer getOffset() {
		return offset;
	}


	public Config setOffset(Integer offset) {
		this.offset = offset;
		return this;
	}


	public Integer getIterateBatchSize() {
		return iterateBatchSize;
	}


	public Config setIterateBatchSize(Integer iterateBatchSize) {
		this.iterateBatchSize = iterateBatchSize;
		return this;
	}

	public Integer getCommitBatchSize() {
		return commitBatchSize;
	}


	public Config setCommitBatchSize(Integer commitBatchSize) {
		this.commitBatchSize = commitBatchSize;
		return this;
	}



	public Boolean getSlaveOk() {
		return this.slaveOk;
	}



	public Config setSlaveOk(Boolean slaveOk) {
		this.slaveOk = slaveOk;
		return this;
	}




	public Boolean getCacheOk() {
		return this.cacheOk;
	}



	public Config setCacheOk(Boolean cacheOk) {
		this.cacheOk = cacheOk;
		return this;
	}


	public Boolean getUseSession() {
		return useSession;
	}



	public Config setUseSession(Boolean useSession) {
		this.useSession = useSession;
		return this;
	}



	public Object getStartId() {
		return startId;
	}



	public Config setStartId(Object startId) {
		this.startId = startId;
		return this;
	}



	public Boolean getIncludeStartId() {
		return includeStartId;
	}



	public Config setIncludeStartId(Boolean includeStartId) {
		this.includeStartId = includeStartId;
		return this;
	}



	public Integer getNumAttempts() {
		return numAttempts;
	}


	public Config setNumAttempts(Integer numAttempts) {
		this.numAttempts = numAttempts;
		return this;
	}

	public PutMethod getPutMethod() {
		return putMethod;
	}

	public Config setPutMethod(PutMethod putMethod) {
		this.putMethod = putMethod;
		return this;
	}
	
	
	
	
}
