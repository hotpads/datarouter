package com.hotpads.datarouter.connection;

import java.sql.Connection;

import com.mchange.v2.c3p0.AbstractConnectionCustomizer;

public class ReadOnlyConnectionCustomizer extends AbstractConnectionCustomizer {
	@Override
	public void onAcquire(Connection c, String parentDataSourceIdentityToken)
			throws Exception {
		c.setReadOnly(true);
	}
	@Override
	public void onCheckOut(Connection c, String parentDataSourceIdentityToken)
			throws Exception {
		c.setReadOnly(true);
	}
}
