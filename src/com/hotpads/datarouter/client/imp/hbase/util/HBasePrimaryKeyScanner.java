package com.hotpads.datarouter.client.imp.hbase.util;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.iterable.PeekableIterable;
import com.hotpads.util.core.iterable.PeekableIterator;

@Deprecated//use HBaseManualPrimaryKeyScanner
public class HBasePrimaryKeyScanner<PK extends PrimaryKey<PK>>
implements PeekableIterable<PK>{

	protected DatabeanFieldInfo<PK,?,?> fieldInfo;
	protected HTable hTable;
	protected Scan scan;
	
	public HBasePrimaryKeyScanner(DatabeanFieldInfo<PK,?,?> fieldInfo, HTable hTable, Scan scan){
		this.fieldInfo = fieldInfo;
		this.hTable = hTable;
		this.scan = scan;
		scan.setFilter(new FirstKeyOnlyFilter());
	}
	
	@Override
	public PeekableIterator<PK> iterator() {
		return new HBasePrimaryKeyIterator<PK>(fieldInfo, hTable, scan);
	}
	
	
	
	public static class HBasePrimaryKeyIterator<PK extends PrimaryKey<PK>>
	implements PeekableIterator<PK>{

		protected DatabeanFieldInfo<PK,?,?> fieldInfo;
		protected HTable hTable;
		protected Scan scan;
		protected ResultScanner scanner;
		protected boolean initialized = false;//avoid RPC in constructor
		protected PK peeked;
		
		public HBasePrimaryKeyIterator(DatabeanFieldInfo<PK,?,?> fieldInfo, HTable hTable, Scan scan){
			this.fieldInfo = fieldInfo;
			this.hTable = hTable;
			this.scan = scan;
		}
		
		protected void initialize(){
			try{
				scanner = hTable.getScanner(scan);
			}catch(IOException ioe){
				throw new DataAccessException(ioe);
			}
			Assert.assertNull(next());
			initialized = true;
		}
		
		@Override
		public boolean hasNext() {
			if(!initialized){
				initialize();
			}
			return peeked != null;
		}
		
		@Override
		public PK peek() {
			return peeked;
		}
		
		@Override
		public PK next() {
			if(initialized && peeked==null){ return null; }
			PK ret = peeked;
			Result row;
			try{
				row = scanner.next();
			}catch(IOException ioe){
				throw new DataAccessException(ioe);
			}
			if(row!=null){
				peeked = HBaseResultTool.getPrimaryKey(row.getRow(), fieldInfo);
			}else{
				peeked = null;
			}
			return ret;
		}
		
		@Override
		public void remove() {
			throw new RuntimeException("can't remove from this iterator");
		}
	}
	
}
