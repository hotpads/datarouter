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
package io.datarouter.aws.rds.job;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.Update;

@Singleton
public class DnsUpdater{
	private static final Logger logger = LoggerFactory.getLogger(DnsUpdater.class);

	private final Resolver resolver;
	private final Name zone;

	@Inject
	public DnsUpdater() throws TextParseException, UnknownHostException{
		this.resolver = new SimpleResolver("TODO pass the bind ip");
		this.resolver.setTSIGKey(new TSIG(TSIG.HMAC_MD5, "TODO pass key name", "TODO pass the secret"));
		this.zone = new Name("TODO pass the zone with trailing dot");
	}

	public void addCname(String subdomain, String target){
		if(!target.endsWith(".")){
			target = target + ".";
		}
		var update = new Update(zone);
		Message response;
		try{
			update.add(new Name(subdomain, zone), Type.CNAME, 300, target);
			response = resolver.send(update);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		checkSuccess(response);
	}

	public void delete(String subdomain){
		var update = new Update(zone);
		Message response;
		try{
			update.delete(new Name(subdomain, zone), Type.CNAME);
			response = resolver.send(update);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		checkSuccess(response);
	}

	private void checkSuccess(Message response){
		int responseCode = response.getRcode();
		if(responseCode == 0){
			return;
		}
		logger.warn("errorCode={} response={}", responseCode, response);
		throw new RuntimeException("dns update failed errorCode=" + responseCode);
	}

}
