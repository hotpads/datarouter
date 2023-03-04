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
package io.datarouter.aws.s3.client;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.td;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.S3ClientType;
import io.datarouter.aws.s3.config.DatarouterAwsS3Executors.BucketRegionExecutor;
import io.datarouter.aws.s3.config.DatarouterAwsS3Paths;
import io.datarouter.aws.s3.node.S3Node;
import io.datarouter.aws.s3.web.S3BucketHandler;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;
import software.amazon.awssdk.regions.Region;

public class S3WebInspector implements DatarouterClientWebInspector{

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClientOptions clientOptions;
	@Inject
	private S3ClientManager s3ClientManager;
	@Inject
	private DatarouterAwsS3Paths paths;
	@Inject
	private BucketRegionExecutor bucketRegionExecutor;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		String contextPath = request.getContextPath();
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(params, S3ClientType.class);
		var clientId = clientParams.getClientId();
		if(clientId == null){
			return new MessageMav("Client not found");
		}

		var clientName = clientId.getName();
		Map<String,String> allClientOptions = clientOptions.getAllClientOptions(clientName);
		var content = div(
				buildClientPageHeader(clientName),
				buildClientOptionsTable(allClientOptions),
				buildNodeTable(contextPath, clientId),
				buildBucketTable(contextPath, clientId))
				.withClass("container my-3");

		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - S3")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private DivTag buildNodeTable(String contextPath, ClientId clientId){
		List<S3NodeDto> nodeDtos = Scanner.of(nodes.getTableNamesForClient(clientId.getName()))
				.map(tableName -> nodes.getPhysicalNodeForClientAndTable(
						clientId.getName(),
						tableName))
				.map(NodeTool::extractSinglePhysicalNode)
				.map(S3Node.class::cast)
				.map(s3Node -> new S3NodeDto(s3Node.getBucket(), s3Node.getRootPath().toString()))
				.list();
		var table = new J2HtmlTable<S3NodeDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withHtmlColumn("Browse", row -> {
					String href = new URIBuilder()
							.setPath(contextPath + paths.datarouter.clients.awsS3.listObjects.toSlashedString())
							.addParameter(S3BucketHandler.P_client, clientId.getName())
							.addParameter(S3BucketHandler.P_bucket, row.bucket)
							.addParameter(S3BucketHandler.P_prefix, row.rootPath)
							.toString();
					return td(a("Browse").withHref(href));
				})
				.withColumn("Bucket", row -> row.bucket)
				.withColumn("Root Path", row -> row.rootPath)
				.build(nodeDtos);
		return div(h4("Nodes - " + nodeDtos.size()), table)
				.withClass("container-fluid my-4")
				.withStyle("padding-left: 0px");
	}

	private static class S3NodeDto{

		final String bucket;
		final String rootPath;

		S3NodeDto(String bucket, String rootPath){
			this.bucket = bucket;
			this.rootPath = rootPath;
		}

	}

	private DivTag buildBucketTable(String contextPath, ClientId clientId){
		DatarouterS3Client client = s3ClientManager.getClient(clientId);
		List<S3BucketDto> buckets = client.scanBuckets()
				.parallelUnordered(new Threads(
						bucketRegionExecutor,
						bucketRegionExecutor.getMaximumPoolSize()))
				.map(bucket -> {
					Region region = client.getRegionForBucket(bucket.name()); // RPC
					return new S3BucketDto(
							clientId.getName(),
							bucket.name(),
							region,
							bucket.creationDate());
				})
				.sort(Comparator.comparing(bucket -> bucket.bucketName.toLowerCase()))
				.list();
		var table = new J2HtmlTable<S3BucketDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withHtmlColumn("Name", bucket -> {
					String href = new URIBuilder()
							.setPath(contextPath + paths.datarouter.clients.awsS3.listObjects.toSlashedString())
							.addParameter(S3BucketHandler.P_client, bucket.clientName)
							.addParameter(S3BucketHandler.P_bucket, bucket.bucketName)
							.addParameter(S3BucketHandler.P_delimiter, "/")
							.toString();
					return td(a(bucket.bucketName).withHref(href));
				})
				.withColumn("Region", bucket -> bucket.region)
				.withColumn("Created", bucket -> bucket.creationDate)
				.build(buckets);
		return div(h4("Buckets - " + buckets.size()), table)
				.withClass("container-fluid my-4")
				.withStyle("padding-left: 0px");
	}

	private static class S3BucketDto{

		final String clientName;
		final String bucketName;
		final Region region;
		final Instant creationDate;

		public S3BucketDto(String clientName, String bucketName, Region region, Instant creationDate){
			this.clientName = clientName;
			this.bucketName = bucketName;
			this.region = region;
			this.creationDate = creationDate;
		}

	}

}
