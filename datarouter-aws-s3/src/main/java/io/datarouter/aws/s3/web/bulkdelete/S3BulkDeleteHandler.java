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

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.span;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.S3Limits;
import io.datarouter.aws.s3.client.S3ClientManager;
import io.datarouter.aws.s3.config.DatarouterAwsS3Executors.BulkDeleteExecutor;
import io.datarouter.aws.s3.config.DatarouterAwsS3Paths;
import io.datarouter.aws.s3.config.DatarouterAwsS3Plugin;
import io.datarouter.aws.s3.web.S3Html;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.file.BucketAndKey;
import io.datarouter.storage.file.BucketAndKeyVersion;
import io.datarouter.storage.file.BucketAndKeyVersionResult;
import io.datarouter.storage.file.BucketAndKeyVersions;
import io.datarouter.storage.file.BucketAndKeys;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;

public class S3BulkDeleteHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(S3BulkDeleteHandler.class);

	private static final String P_client = "client";
	private static final String P_bucket = "bucket";
	private static final String P_prefix = "prefix";
	private static final String P_limit = "limit";
	private static final String P_deleteAllVersions = "deleteAllVersions";
	private static final String P_submit = "submit";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterClients clients;
	@Inject
	private S3ClientManager s3ClientManager;
	@Inject
	private S3BulkDeleteHandlerLinks s3BulkDeleteHandlerLinks;
	@Inject
	private S3BulkDeleteValidation s3BulkDeleteValidation;
	@Inject
	private S3BulkDeleteConfirmationHtml s3BulkDeleteConfirmationHtml;
	@Inject
	private BulkDeleteExecutor bulkDeleteExec;

	@Handler
	public Mav form(
			Optional<String> client,
			Optional<String> bucket,
			Optional<String> prefix,
			Optional<Long> limit,
			Optional<Boolean> deleteAllVersions,
			Optional<Boolean> submit){
		logger.warn("request={}", request.getParameterMap());
		logger.warn("deleteAllVersions={}", deleteAllVersions);
		String title = DatarouterAwsS3Plugin.NAME + " - Bulk Delete";

		// make form
		boolean submitted = submit.orElse(false);
		var form = new HtmlForm()
				.withMethod("post");
		form.addTextField()
				.withDisplay("Datarouter Client Name")
				.withName(P_client)
				.withValue(
						client.orElse(""),
						submitted,
						s3BulkDeleteValidation::validateClientName);
		form.addTextField()
				.withDisplay("Bucket")
				.withName(P_bucket)
				.withValue(
						bucket.orElse(""),
						submitted,
						bucketName -> s3BulkDeleteValidation.validateBucketName(client, bucketName));
		form.addTextField()
				.withDisplay("Prefix")
				.withName(P_prefix)
				.withValue(prefix.orElse(""));
		form.addTextField()
				.withDisplay("Limit")
				.withName(P_limit)
				.withValue(limit.map(Number::toString).orElse(""));
		form.addCheckboxField()
				.withDisplay("Delete All Versions")
				.withName(P_deleteAllVersions)
				.withChecked(deleteAllVersions.orElse(false));
		form.addButtonWithoutSubmitAction()
				.withDisplay("Proceed to Confirmation")
				.withName(P_submit)
				.withValue(Boolean.TRUE.toString());
		var htmlForm = Bootstrap4FormHtml.render(form)
				.withClass("card card-body bg-light");

		// show form
		if(!submitted || form.hasErrors()){
			var content = div(
					S3Html.makeHeader(title, "Delete many objects based on key prefix"),
					br(),
					htmlForm)
					.withClass("container");
			return pageFactory.simplePage(request, title, content);
		}

		// proceed to confirmation
		String confirmHref = s3BulkDeleteHandlerLinks.confirmation(
				client.orElseThrow(),
				bucket.orElseThrow(),
				prefix,
				limit,
				deleteAllVersions);
		return new GlobalRedirectMav(confirmHref);
	}

	@Handler
	public Mav confirmation(
			String client,
			String bucket,
			Optional<String> prefix,
			Optional<Long> limit,
			Optional<Boolean> deleteAllVersions){
		String title = DatarouterAwsS3Plugin.NAME + " - Confirm Bulk Delete";
		var bucketAndPrefix = new BucketAndPrefix(bucket, prefix.orElse(""));

		var headerDiv = S3Html.makeHeader(title, "Please confirm the count and example keys look correct");
		var summaryDiv = s3BulkDeleteConfirmationHtml.makeConfirmationSummary(
				client,
				bucket,
				prefix,
				limit,
				deleteAllVersions);
		var warningsDiv = s3BulkDeleteConfirmationHtml.makeConfirmationWarningsDiv(
				client,
				bucketAndPrefix,
				prefix,
				limit,
				deleteAllVersions);
		var exampleKeysDiv = s3BulkDeleteConfirmationHtml.makeConfirmationExamplesDiv(
				client,
				bucketAndPrefix,
				deleteAllVersions.orElse(false));
		var confirmDiv = div(
				h5("Last Warning: "),
				span("Clicking this button will perform the deletion."),
				br(),
				br(),
				S3Html.makeDangerButton(
						"Confirm and Delete",
						s3BulkDeleteHandlerLinks.performDeletion(client, bucket, prefix, limit, deleteAllVersions)));
		var content = div(
				headerDiv,
				br(),
				summaryDiv,
				br(),
				warningsDiv,
				br(),
				exampleKeysDiv,
				br(),
				confirmDiv)
				.withClass("container");
		return pageFactory.simplePage(request, title, content);
	}

	@Handler
	public Mav performDeletion(
			String client,
			String bucket,
			Optional<String> prefix,
			Optional<Long> limit,
			Optional<Boolean> deleteAllVersions){
		String title = DatarouterAwsS3Plugin.NAME + " - Bulk Delete - Complete";
		var bucketAndPrefix = new BucketAndPrefix(bucket, prefix.orElse(""));

		// perform bulk delete
		DatarouterS3Client s3Client = getS3Client(client);
		var threads = new Threads(bulkDeleteExec, 2);// 4 was triggering the rate limiter
		var count = new AtomicLong();
		String message;
		if(deleteAllVersions.orElse(false)){
			s3Client.scanVersions(bucketAndPrefix)
					.include(BucketAndKeyVersionResult::isFile)
					.limit(limit.orElse(Long.MAX_VALUE))
					.map(version -> new BucketAndKeyVersion(
							bucketAndPrefix.bucket(),
							version.key(),
							version.version()))
					.each($ -> count.incrementAndGet())
					.each(version -> logger.warn("id={}, deleting {}", count, version))
					.batch(S3Limits.MAX_DELETE_MULTI_KEYS)
					.map(BucketAndKeyVersions::fromIndividualKeyVersions)
					.parallelOrdered(threads)
					.forEach(s3Client::deleteVersions);
			message = String.format("Deleted %s object versions", count);
		}else{
			s3Client.scan(bucketAndPrefix)
					.limit(limit.orElse(Long.MAX_VALUE))
					.map(object -> new BucketAndKey(
							bucketAndPrefix.bucket(),
							object.key()))
					.each($ -> count.incrementAndGet())
					.each(object -> logger.warn("id={}, deleting {}", count, object))
					.batch(S3Limits.MAX_DELETE_MULTI_KEYS)
					.map(BucketAndKeys::fromIndividualKeys)
					.parallelOrdered(threads)
					.forEach(s3Client::deleteMulti);
			message = String.format("Deleted %s objects", count);
		}
		logger.warn(message);

		// show results
		var headerDiv = S3Html.makeHeader(title, "Deletion has completed successfully");
		var messageDiv = div(
				h5(message));
		var content = div(
				headerDiv,
				br(),
				messageDiv)
				.withClass("container");
		return pageFactory.simplePage(request, title, content);
	}

	/*--------- private ----------*/

	// TODO move somewhere more generic?
	private DatarouterS3Client getS3Client(String clientName){
		ClientId clientId = clients.getClientId(clientName);
		return s3ClientManager.getClient(clientId);
	}

	/*--------- links -----------*/

	@Singleton
	public static class S3BulkDeleteHandlerLinks{

		@Inject
		private ServletContextSupplier contextSupplier;
		@Inject
		private DatarouterAwsS3Paths paths;

		public String form(BucketAndPrefix bucketAndPrefix){
			var uriBuilder = new URIBuilder()
					.setPath(contextSupplier.getContextPath()
							+ paths.datarouter.clients.awsS3.bulkDelete.form.toSlashedString())
					.addParameter(P_bucket, bucketAndPrefix.bucket())
					.addParameter(P_prefix, bucketAndPrefix.prefix());
			return uriBuilder.toString();
		}

		public String confirmation(
				String client,
				String bucket,
				Optional<String> optPrefix,
				Optional<Long> optLimit,
				Optional<Boolean> optDeleteAllVersions){
			var uriBuilder = new URIBuilder()
					.setPath(contextSupplier.getContextPath()
							+ paths.datarouter.clients.awsS3.bulkDelete.confirmation.toSlashedString())
					.addParameter(P_client, client)
					.addParameter(P_bucket, bucket);
			optPrefix.ifPresent(prefix -> uriBuilder.addParameter(P_prefix, optPrefix.orElseThrow()));
			optLimit.ifPresent(limit -> uriBuilder.addParameter(P_limit, Long.toString(limit)));
			optDeleteAllVersions.ifPresent(deleteAllVersions -> uriBuilder.addParameter(
					P_deleteAllVersions,
					Boolean.toString(deleteAllVersions)));
			return uriBuilder.toString();
		}

		public String performDeletion(
				String client,
				String bucket,
				Optional<String> optPrefix,
				Optional<Long> optLimit,
				Optional<Boolean> optDeleteAllVersions){
			var uriBuilder = new URIBuilder()
					.setPath(contextSupplier.getContextPath()
							+ paths.datarouter.clients.awsS3.bulkDelete.performDeletion.toSlashedString())
					.addParameter(P_client, client)
					.addParameter(P_bucket, bucket);
			optPrefix.ifPresent(prefix -> uriBuilder.addParameter(P_prefix, optPrefix.orElseThrow()));
			optLimit.ifPresent(limit -> uriBuilder.addParameter(P_limit, Long.toString(limit)));
			optDeleteAllVersions.ifPresent(deleteAllVersions -> uriBuilder.addParameter(
					P_deleteAllVersions,
					Boolean.toString(deleteAllVersions)));
			return uriBuilder.toString();
		}

	}

}
