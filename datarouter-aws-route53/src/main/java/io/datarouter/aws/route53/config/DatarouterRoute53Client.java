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
package io.datarouter.aws.route53.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import jakarta.inject.Inject;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.internal.LazyAwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.Change;
import software.amazon.awssdk.services.route53.model.ChangeAction;
import software.amazon.awssdk.services.route53.model.ChangeBatch;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.ListHostedZonesRequest;
import software.amazon.awssdk.services.route53.model.ListHostedZonesResponse;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

public class DatarouterRoute53Client{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterRoute53Client.class);

	public static final int MAX_ITEMS_TO_FETCH = 1000;

	private final Route53Client route53ClientSupplier;

	@Inject
	public DatarouterRoute53Client(DatarouterRoute53Options route53Options){
		route53ClientSupplier = Route53Client.builder()
				.region(Region.AWS_GLOBAL)
				.credentialsProvider(LazyAwsCredentialsProvider.create(() -> StaticCredentialsProvider.create(
						AwsBasicCredentials.create(
								route53Options.getAccessKey(),
								route53Options.getSecretAccessKey()))))
						.build();
	}

	public ListHostedZonesResponse getHostedZones(){
		return getHostedZones(null, MAX_ITEMS_TO_FETCH);
	}

	public ListHostedZonesResponse getHostedZones(String startAfter, int maxItems){
		return route53ClientSupplier.listHostedZones(ListHostedZonesRequest.builder()
				.marker(startAfter)
				.maxItems(Integer.toString(maxItems))
				.build());
	}

	public Optional<HostedZone> getHostedZone(String hostedZoneName){
		//the hostedZoneName is not available as a query criteria so have to get all zones and filter
		try{
			return Scanner.of(getHostedZones().hostedZones())
					.include(hostedZone -> hostedZone.name().equals(hostedZoneName + "."))
					.findFirst();
		}catch(Exception e){
			return Optional.empty();
		}
	}

	public void createOrUpdateCname(HostedZone hostedZone, String recordFqdn, String cnameValue, Long ttlSeconds){
		var resourceRecordSet = ResourceRecordSet.builder()
				.name(recordFqdn)
				.type(RRType.CNAME)
				.ttl(ttlSeconds)
				.resourceRecords(ResourceRecord.builder()
						.value(cnameValue)
						.build())
				.build();

		upsertResourceRecordSet(hostedZone, resourceRecordSet);
		logger.warn("Submitted Route53 request to associate via CNAME: {} with {}", recordFqdn, cnameValue);
	}

	public void deleteRecord(HostedZone hostedZone, String recordFqdn, String cnameValue, Long ttlSeconds){
		var resourceRecordSet = ResourceRecordSet.builder()
				.name(recordFqdn)
				.type(RRType.CNAME)
				.ttl(ttlSeconds)
				.resourceRecords(ResourceRecord.builder()
						.value(cnameValue)
						.build())
				.build();

		upsertResourceRecordSet(hostedZone, resourceRecordSet);
		logger.warn("Submitted Route53 request to associate via CNAME: {} with {}", recordFqdn, cnameValue);
	}

	public void deleteRecord(String recordFqdn, HostedZone hostedZone){
		ResourceRecordSet resourceRecordSet = getResourceRecordSet(hostedZone, recordFqdn)
				.orElseThrow(() -> new RuntimeException("Unable to find hosted records for " + recordFqdn));

		ChangeResourceRecordSetsRequest request = ChangeResourceRecordSetsRequest.builder()
				.hostedZoneId(hostedZone.id())
				.changeBatch(ChangeBatch.builder()
						.changes(Change.builder()
								.action(ChangeAction.DELETE)
								.resourceRecordSet(resourceRecordSet)
								.build())
						.build())
				.build();

		route53ClientSupplier.changeResourceRecordSets(request);
		logger.warn("Submitted Route53 request to delete alias : type {} | {} | {}",
				resourceRecordSet.type().name(), resourceRecordSet.name(), resourceRecordSet.aliasTarget().dnsName());
	}

	private Optional<ResourceRecordSet> getResourceRecordSet(HostedZone hostedZone, String recordFqdn){
		return Scanner.of(getResourceRecordSets(hostedZone.id(), recordFqdn, 1).resourceRecordSets())
				.findFirst();
	}

	public ListResourceRecordSetsResponse getResourceRecordSets(
			String domainHostedZoneId,
			String startRecordName,
			int maxItems){
		ListResourceRecordSetsRequest request = ListResourceRecordSetsRequest.builder()
				.hostedZoneId(domainHostedZoneId)
				.startRecordName(startRecordName)
				.maxItems(Integer.toString(maxItems))
				.build();
		return route53ClientSupplier.listResourceRecordSets(request);
	}

	private void upsertResourceRecordSet(
			HostedZone hostedZone,
			ResourceRecordSet resourceRecordSet){
		var change = Change.builder()
				.action(ChangeAction.UPSERT)
				.resourceRecordSet(resourceRecordSet)
				.build();
		var changeBatch = ChangeBatch.builder()
				.changes(change)
				.build();
		var changeRequest = ChangeResourceRecordSetsRequest.builder()
				.hostedZoneId(hostedZone.id())
				.changeBatch(changeBatch)
				.build();
		route53ClientSupplier.changeResourceRecordSets(changeRequest);
	}

}
