package com.hotpads.handler.account;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.encoder.JsonEncoder;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.InContextRedirectMav;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;

public class DatarouterAccountManagerHandler extends BaseHandler{

	@Inject
	private DatarouterAccountNodes datarouterAccountNodes;
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;

	@Handler(defaultHandler = true)
	public Mav index(){
		return new InContextRedirectMav(params, authenticationConfig.getAccountManagerPath() + "/manage");
	}

	@Handler
	public Mav manage(){
		return new Mav("/jsp/authentication/accountManager.jsp");
	}

	@Handler(encoder = JsonEncoder.class)
	public List<DatarouterAccount> list(){
		return datarouterAccountNodes.datarouterAccount().stream(null, null)
				.collect(Collectors.toList());
	}

	@Handler(encoder = JsonEncoder.class)
	public DatarouterAccount add(String accountName){
		DatarouterAccount account = new DatarouterAccount(accountName);
		datarouterAccountNodes.datarouterAccount().put(account, null);
		return account;
	}

	@Handler(encoder = JsonEncoder.class)
	public DatarouterAccount resetApiKey(String accountName){
		DatarouterAccount account = datarouterAccountNodes.datarouterAccount().get(new DatarouterAccountKey(
				accountName), null);
		account.resetApiKey();
		datarouterAccountNodes.datarouterAccount().put(account, null);
		return account;
	}

	@Handler
	public void delete(String accountName){
		DatarouterAccountKey accountKey = new DatarouterAccountKey(accountName);
		datarouterAccountNodes.datarouterAccount().delete(accountKey, null);
	}

}
