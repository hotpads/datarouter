package com.hotpads.datarouter.config;

import java.util.Map;



public class Config {
	
	public static final Boolean defaultCacheOk = true;
	
	protected Integer numAttempts = 1;
	
	protected Boolean slaveOk = false;
	protected Boolean cacheOk = defaultCacheOk;

	protected Map<String,String> connectionNameByClientName;
	protected TxnControl txnControl = TxnControl.support;
	protected Isolation isolation = Isolation.readCommitted;
	protected Boolean useSession = true;
	
	protected String forceIndex;
	
	protected PutMethod putMethod = PutMethod.selectFirstOrLookAtPrimaryKey;
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
	
	
	public Isolation getIsolation() {
		return isolation;
	}
	public Config setIsolation(Isolation isolation) {
		this.isolation = isolation;
		return this;
	}


	public TxnControl getTxnControl() {
		return txnControl;
	}


	public Config setTxnControl(TxnControl txnControl) {
		this.txnControl = txnControl;
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


	public String getForceIndex() {
		return forceIndex;
	}


	public Config setForceIndex(String forceIndex) {
		this.forceIndex = forceIndex;
		return this;
	}

	public Map<String, String> getConnectionNameByClientName() {
		return connectionNameByClientName;
	}

	public Config setConnectionNameByClientName(
			Map<String, String> connectionNameByClientName) {
		this.connectionNameByClientName = connectionNameByClientName;
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
