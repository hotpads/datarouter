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
package io.datarouter.aws.s3.web.bulkdelete;

import static j2html.TagCreator.b;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.li;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.tr;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.client.S3ClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.file.BucketAndKeyVersionResult;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.s3.model.S3Object;

@Singleton
public class S3BulkDeleteConfirmationHtml{

	@Inject
	private DatarouterClients clients;
	@Inject
	private S3ClientManager s3ClientManager;

	/*--------- confirmation summary ----------*/

	public DivTag makeConfirmationSummary(
			String client,
			String bucket,
			Optional<String> prefix,
			Optional<Long> limit,
			Optional<Boolean> deleteAllVersions){
		var tableTag = table(
				tr(
						td(b("Datarouter Client Name"))
						.withStyle("width:200px"),
						td(client)),
				tr(
						td(b("Bucket")),
						td(bucket)),
				tr(
						td(b("Key Prefix")),
						td(prefix.orElse(""))),
				tr(
						td(b("Limit")),
						td(limit.map(NumberFormatter::addCommas).orElse("[not specified]"))),
				tr(
						td(b("Delete All Versions")),
						td(Boolean.toString(deleteAllVersions.orElse(false)))))
				.withClasses("table table-sm table-striped border")
				.withStyle("width:600px;");
		return div(
				h5("Summary"),
				tableTag);
	}

	/*--------- confirmation warnings ----------*/

	public DivTag makeConfirmationWarningsDiv(
			String client,
			BucketAndPrefix bucketAndPrefix,
			Optional<String> optPrefix,
			Optional<Long> optLimit,
			Optional<Boolean> optDeleteAllVersions){
		// gather warnings
		Optional<DivTag> optEmptyPrefixDiv = makeConfirmationEmptyPrefixWarningDiv(optPrefix);
		Optional<DivTag> optEmptyLimitDiv = makeConfirmationEmptyLimitWarningDiv(optLimit);
		Optional<DivTag> optObjectVersionsDiv = makeConfirmationObjectVersionsDiv(
				optDeleteAllVersions.orElse(false));
		var keyCountDiv = makeConfirmationCountDiv(
				client,
				bucketAndPrefix,
				optDeleteAllVersions.orElse(false));

		// assemble
		var warningsDiv = div(h5("Warnings:"));
		optEmptyPrefixDiv.ifPresent(emptyPrefixDiv -> warningsDiv.with(div(emptyPrefixDiv)));
		optEmptyLimitDiv.ifPresent(emptyLimitDiv -> warningsDiv.with(div(emptyLimitDiv)));
		warningsDiv.with(div(keyCountDiv));
		optObjectVersionsDiv.ifPresent(objectVersionsDiv -> warningsDiv.with(div(objectVersionsDiv)));
		return warningsDiv;
	}

	private Optional<DivTag> makeConfirmationEmptyPrefixWarningDiv(Optional<String> prefix){
		if(prefix.isPresent() && !prefix.orElseThrow().isEmpty()){
			return Optional.empty();
		}
		var divTag = div(li(
				text("You are about to delete from the bucket's"),
				b("root prefix!"),
				text("Please be certain!")));
		return Optional.of(divTag);
	}

	private Optional<DivTag> makeConfirmationEmptyLimitWarningDiv(Optional<Long> optLimit){
		if(optLimit.isPresent()){
			return Optional.empty();
		}
		var divTag = div(li(
				text("You haven't specified a"),
				b("limit"),
				text("which could help reduce the damage from a faulty request.")));
		return Optional.of(divTag);
	}

	private Optional<DivTag> makeConfirmationObjectVersionsDiv(boolean deleteAllVersions){
		if(deleteAllVersions){
			var divTag = div(li(
					text("You are about to delete"),
					b("all object versions."),
					text("Are you sure?")));
			return Optional.of(divTag);
		}
		return Optional.empty();
	}

	private DivTag makeConfirmationCountDiv(
			String clientName,
			BucketAndPrefix bucketAndPrefix,
			boolean includeVersions){
		DatarouterS3Client s3Client = getS3Client(clientName);
		long maxScanCount = 10_000;// Scanning can take a while
		long count;
		if(includeVersions){
			count = s3Client.scanVersions(bucketAndPrefix)
					.limit(maxScanCount + 1)
					.count();
		}else{
			count = s3Client.scan(bucketAndPrefix)
					.limit(maxScanCount + 1)
					.count();
		}
		boolean exceededMaxScan = count > maxScanCount;
		String countMessage = exceededMaxScan
				? String.format("More than %s", NumberFormatter.addCommas(maxScanCount))
				: String.format("%s", NumberFormatter.addCommas(count));
		String objectsOrVersionsMessage = includeVersions ? "object versions" : "objects";
		var messageListItem = li(
				b(countMessage),
				text(objectsOrVersionsMessage),
				text("will be deleted"));
		return div(messageListItem);
	}

	/*-------- confirmation examples ---------*/

	public DivTag makeConfirmationExamplesDiv(
			String clientName,
			BucketAndPrefix bucketAndPrefix,
			boolean includeVersions){
		return includeVersions
				? makeConfirmationExampleVersionsDiv(clientName, bucketAndPrefix)
				: makeConfirmationExampleKeysDiv(clientName, bucketAndPrefix);
	}

	private DivTag makeConfirmationExampleKeysDiv(
			String clientName,
			BucketAndPrefix bucketAndPrefix){
		DatarouterS3Client s3Client = getS3Client(clientName);
		int numExamples = 10;
		List<S3Object> examples = s3Client.scan(bucketAndPrefix)
				.limit(numExamples)
				.list();
		var rowId = new AtomicInteger(1);
		var tableBuilder = new J2HtmlTable<S3Object>()
				.withClasses("table table-sm table-striped border")
				.withColumn("#", $ -> rowId.getAndIncrement() + "")
				.withColumn("Key", S3Object::key);
		return div(
				h5(String.format("Showing the first %s keys that will be deleted.", examples.size())),
				tableBuilder.build(examples));
	}

	private DivTag makeConfirmationExampleVersionsDiv(
			String clientName,
			BucketAndPrefix bucketAndPrefix){
		DatarouterS3Client s3Client = getS3Client(clientName);
		int numExamples = 10;
		List<BucketAndKeyVersionResult> examples = s3Client.scanVersions(bucketAndPrefix)
				.include(BucketAndKeyVersionResult::isFile)
				.limit(numExamples)
				.list();
		var rowId = new AtomicInteger(1);
		var tableBuilder = new J2HtmlTable<BucketAndKeyVersionResult>()
				.withClasses("table table-sm table-striped border")
				.withColumn("#", $ -> rowId.getAndIncrement() + "")
				.withColumn("Key", BucketAndKeyVersionResult::key)
				.withColumn("Version", BucketAndKeyVersionResult::version);
		return div(
				h5(String.format("Showing the first %s object versions that will be deleted.", examples.size())),
				tableBuilder.build(examples));
	}

	/*--------- get ----------*/

	// TODO move somewhere more generic?
	private DatarouterS3Client getS3Client(String clientName){
		ClientId clientId = clients.getClientId(clientName);
		return s3ClientManager.getClient(clientId);
	}

}
