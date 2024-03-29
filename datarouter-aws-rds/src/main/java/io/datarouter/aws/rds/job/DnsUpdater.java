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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.Update;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DnsUpdater{
	private static final Logger logger = LoggerFactory.getLogger(DnsUpdater.class);

	@Inject
	private DnsUpdateSettings settings;

	public String addCname(String subdomain, String target){
		if(!target.endsWith(".")){
			target = target + ".";
		}
		return addRecord(Type.CNAME, subdomain, target);
	}

	public String deleteCname(String subdomain){
		return deleteRecord(Type.CNAME, subdomain);
	}

	public String addA(String domain, String ip){
		return addRecord(Type.A, domain, ip);
	}

	public String deleteA(String domain){
		return deleteRecord(Type.A, domain);
	}

	private String addRecord(int type, String subdomain, String target){
		logger.warn("creating record type={} subdomain={} target={}", type, subdomain, target);
		Name zone;
		try{
			zone = new Name(settings.zone.get());
		}catch(TextParseException e){
			throw new RuntimeException(e);
		}
		var update = new Update(zone);
		Message response;
		try{
			update.add(new Name(subdomain, zone), type, 0, target);
			SimpleResolver resolver = makeResolver();
			response = resolver.send(update);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		checkSuccess(response);
		return response.toString();
	}

	private String deleteRecord(int type, String subdomain){
		logger.warn("deleting record type={} subdomain={}", type, subdomain);
		Name zone;
		try{
			zone = new Name(settings.zone.get());
		}catch(TextParseException e){
			throw new RuntimeException(e);
		}
		var update = new Update(zone);
		Message response;
		try{
			update.delete(new Name(subdomain, zone), type);
			SimpleResolver resolver = makeResolver();
			response = resolver.send(update);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		checkSuccess(response);
		return response.toString();
	}

	private void checkSuccess(Message response){
		int responseCode = response.getRcode();
		if(responseCode == 0){
			return;
		}
		logger.warn("errorCode={} response={}", responseCode, response);
		throw new RuntimeException("dns update failed errorCode=" + responseCode);
	}

	private SimpleResolver makeResolver() throws UnknownHostException{
		SimpleResolver resolver = new SimpleResolver(settings.bindAddress.get());
		resolver.setTSIGKey(new TSIG(TSIG.HMAC_MD5, settings.tsigKey.get(), settings.tsigSecret.get()));
		return resolver;
	}

}
