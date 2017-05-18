package com.hotpads.datarouter.client.imp.hibernate.util;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;

public class JdbcRollbackRetryingCallableTests{
	@Test
	public void testDeadlockExceptionNames(){
		//jdbc package
		Assert.assertTrue(SessionExecutorImpl.ROLLED_BACK_EXCEPTION_SIMPLE_NAMES.contains(
				com.mysql.jdbc.exceptions.MySQLTransactionRollbackException.class.getSimpleName()));
		//jdbc4 package
		Assert.assertTrue(SessionExecutorImpl.ROLLED_BACK_EXCEPTION_SIMPLE_NAMES.contains(
				com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException.class.getSimpleName()));
	}
}