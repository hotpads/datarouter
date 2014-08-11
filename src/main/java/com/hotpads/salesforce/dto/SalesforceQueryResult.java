package com.hotpads.salesforce.dto;

import java.util.List;

import com.hotpads.salesforce.databean.SalesforceDatabean;

public class SalesforceQueryResult<D extends SalesforceDatabean>{
	public int totalSize;
	public boolean done;
	public List<D> records;
}