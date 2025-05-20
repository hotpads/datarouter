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
package io.datarouter.web.util.http;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.util.GcpInstanceTool;
import io.datarouter.util.net.Subnet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class IpAddressService{
	private static final Logger logger = LoggerFactory.getLogger(IpAddressService.class);

	private static final boolean IS_GCP = GcpInstanceTool.isGcp();

	@Inject
	private TrustedProxy trustedProxy;

	public IpDetectionDto getIpDetectionDto(HttpServletRequest request){
		return new IpDetectionDto(
				Collections.list(request.getHeaders(HttpHeaders.X_CLIENT_IP)),
				Collections.list(request.getHeaders(HttpHeaders.X_FORWARDED_FOR)),
				request.getRemoteAddr(),
				getIpAddress(request));
	}

	public record IpDetectionDto(
			List<String> clientIpHeaders,
			List<String> forwardedForHeaders,
			String remoteAddr,
			String detectedIp){
	}

	public String getIpAddress(HttpServletRequest request){
		return getIpAddress(request, IS_GCP);
	}

	public String getIpAddress(
			HttpServletRequest request,
			boolean isGcp){
		if(request == null){
			return null;
		}

		//Node servers send in the original X-Forwarded-For as X-Client-IP
		List<String> clientIp = RequestTool.getAllHeaderValuesOrdered(request, HttpHeaders.X_CLIENT_IP);
		Optional<String> lastNonInternalIp = getLastNonInternalIp(clientIp, 0);
		if(lastNonInternalIp.isPresent()){
			return lastNonInternalIp.get();
		}

		//no x-client-ip present, check x-forwarded-for
		List<String> forwardedFor = RequestTool.getAllHeaderValuesOrdered(request, HttpHeaders.X_FORWARDED_FOR);
		lastNonInternalIp = getLastNonInternalIp(forwardedFor, isGcp ? 1 : 0); // GCP adds LB IP
		if(lastNonInternalIp.isPresent()){
			return lastNonInternalIp.get();
		}

		String traceId = TracerTool.getCurrentTraceparent()
				.map(Traceparent::toString)
				.orElse("");
		logger.debug("Unusable IPs included, falling back to remoteAddr. "
				+ HttpHeaders.X_CLIENT_IP + "={} "
				+ HttpHeaders.X_FORWARDED_FOR + "={} "
				+ "path={} "
				+ "traceId={}",
				clientIp,
				forwardedFor,
				RequestTool.getPath(request),
				traceId);
		logger.debug("", new Exception());
		return request.getRemoteAddr();
	}

	private Optional<String> getLastNonInternalIp(
			List<String> headerValues,
			int ipsToSkipFromEnd){
		return Scanner.of(headerValues)
				.reverse()
				.include(RequestTool::isAValidIpV4)
				.exclude(ip -> {
					Optional<Subnet> matchingSubnet = trustedProxy.findInternalProxy(ip);
					if(matchingSubnet.isPresent()){
						logger.info("IpDetection internalProxy ip=" + ip + " subnet=" + matchingSubnet.get().cidr);
						Metrics.count("IpDetection internalProxy " + matchingSubnet.get().cidr);
					}
					return matchingSubnet.isPresent();
				})
				.skip(ipsToSkipFromEnd)
				.exclude(ip -> {
					Optional<Subnet> matchingSubnet = trustedProxy.findCloudfront(ip);
					if(matchingSubnet.isPresent()){
						Metrics.count("IpDetection cloudfront " + matchingSubnet.get().cidr);
					}
					return matchingSubnet.isPresent();
				})
				.findFirst();
	}

}
