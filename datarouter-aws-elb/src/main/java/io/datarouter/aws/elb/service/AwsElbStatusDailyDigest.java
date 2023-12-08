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
package io.datarouter.aws.elb.service;

import static j2html.TagCreator.div;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancer;
import com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancerSchemeEnum;

import io.datarouter.aws.elb.config.DatarouterAwsElbSettingRoot;
import io.datarouter.storage.config.properties.DatarouterServerTypeSupplier;
import io.datarouter.storage.util.Ec2InstanceDetailsDto;
import io.datarouter.storage.util.Ec2InstanceTool;
import io.datarouter.web.config.service.PrivateDomain;
import io.datarouter.web.config.service.PublicDomain;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AwsElbStatusDailyDigest implements DailyDigest{

	private static final String HAS_PUBLIC_LOAD_BALANCER_KEY = "hasPublicAlb";
	private static final String HAS_PRIVATE_LOAD_BALANCER_KEY = "hasPrivateAlb";
	private static final String UNKNOWN_LOAD_BALANCER = "unknownAlb";

	@Inject
	private DatarouterServerTypeSupplier serverTypeSupplier;
	@Inject
	private ElbService elbService;
	@Inject
	private PrivateDomain privateDomain;
	@Inject
	private PublicDomain publicDomain;
	@Inject
	private DailyDigestService dailyDigestService;
	@Inject
	private DatarouterAwsElbSettingRoot settings;

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		return checkAlbSchemeForEc2Instance();
	}

	@Override
	public String getTitle(){
		return "ELB status";
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	private Optional<DivTag> checkAlbSchemeForEc2Instance(){
		if(!settings.enableDailyDigest.get()){
			return Optional.empty();
		}

		Optional<Ec2InstanceDetailsDto> ec2InstanceDetailsDto = Ec2InstanceTool.getEc2InstanceDetails(false);
		String serverType = serverTypeSupplier.getServerTypeString();

		if(ec2InstanceDetailsDto.isEmpty()){
			return Optional.empty();
		}

		String ec2InstanceId = ec2InstanceDetailsDto.get().instanceId;
		Map<String,Boolean> privatePublicAlbsMap = getAlbSchemeForEc2Instance(serverType, ec2InstanceId);
		boolean hasPublicAlb = privatePublicAlbsMap.get(HAS_PUBLIC_LOAD_BALANCER_KEY);
		boolean hasPrivateAlb = privatePublicAlbsMap.get(HAS_PRIVATE_LOAD_BALANCER_KEY);
		boolean hasUnknownAlb = privatePublicAlbsMap.get(UNKNOWN_LOAD_BALANCER);

		var header = dailyDigestService.makeHeader("ELB Status", "");

		if(hasUnknownAlb){
			DivTag tag = div("Server does not have any known load balancers or"
					+ " something might be wrong, e.g ALB name does not match, instance not registered with"
					+ " target group, etc");
			return Optional.of(div(header, tag));
		}

		if(publicDomain.hasPublicDomain() && !hasPublicAlb){
			DivTag tag = div("Server expects public load balancer");
			return Optional.of(div(header, tag));
		}
		if(!publicDomain.hasPublicDomain() && hasPublicAlb){
			DivTag tag = div("Server has unexpected public load balancer");
			return Optional.of(div(header, tag));
		}
		if(publicDomain.hasPublicDomain()
				&& !hasPublicAlb
				&& privateDomain.hasPrivateDomain()
				&& !hasPrivateAlb){
			DivTag tag = div("Server expects both public and private load"
					+ " balancers but it is " + hasPublicAlb + " for public ALB and " + hasPrivateAlb
					+ " for private ALB");
			return Optional.of(div(header, tag));
		}
		return Optional.empty();
	}

	private Map<String,Boolean> getAlbSchemeForEc2Instance(String serverType, String ec2InstanceId){
		Map<String,Boolean> privatePublicAlbsMap = new HashMap<>();
		boolean unknownAlb = true;
		boolean instanceHasPublicAlb = false;
		boolean instanceHasPrivateAlb = false;

		for(LoadBalancer loadBalancer : elbService.getLoadBalancers()){
			// due to teraform/aws naming character limitation, the ALB for some services with long names will have
			// the to be truncated - "some-example-service-joblet-privat" (missing last char), so can't check for
			// exact ALB name
			if(!loadBalancer.getLoadBalancerName().equals(serverType)
					&& !loadBalancer.getLoadBalancerName().startsWith(serverType + "-privat")){
				continue;
			}
			for(String targetGroupArn : elbService.getTargetGroupsArn(loadBalancer.getLoadBalancerArn())){
				for(String targetEc2InstanceId : elbService.getTargetEc2InstancesId(targetGroupArn)){
					if(!targetEc2InstanceId.equals(ec2InstanceId)){
						continue;
					}
					LoadBalancerSchemeEnum loadBalancerScheme = LoadBalancerSchemeEnum.fromValue(
							loadBalancer.getScheme());
					if(LoadBalancerSchemeEnum.InternetFacing == loadBalancerScheme){
						instanceHasPublicAlb = true;
					}
					if(LoadBalancerSchemeEnum.Internal == loadBalancerScheme){
						instanceHasPrivateAlb = true;
					}
					unknownAlb = false;
				}
			}
		}
		privatePublicAlbsMap.put(HAS_PUBLIC_LOAD_BALANCER_KEY, instanceHasPublicAlb);
		privatePublicAlbsMap.put(HAS_PRIVATE_LOAD_BALANCER_KEY, instanceHasPrivateAlb);
		privatePublicAlbsMap.put(UNKNOWN_LOAD_BALANCER, unknownAlb);
		return privatePublicAlbsMap;
	}

}
