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
package io.datarouter.aws.s3.web.bulkcopy;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.aws.s3.client.S3ClientManager;
import io.datarouter.aws.s3.config.DatarouterAwsS3Executors.DatarouterS3BulkCopyReadExecutor;
import io.datarouter.aws.s3.config.DatarouterAwsS3Executors.DatarouterS3BulkCopyWriteExecutor;
import io.datarouter.aws.s3.config.DatarouterAwsS3Paths;
import io.datarouter.aws.s3.config.DatarouterAwsS3Plugin;
import io.datarouter.aws.s3.web.S3Html;
import io.datarouter.aws.s3.web.bulkcopy.S3BulkCopyConfirmationHtml.S3BulkCopyConfirmationHtmlFactory;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.io.MultiByteArrayInputStream;
import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.link.LinkType.NoOpLinkType;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.file.BucketAndKey;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public class S3BulkCopyHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(S3BulkCopyHandler.class);

	private static final int NUM_READ_THREADS = 32;
	private static final ByteLength READ_CHUNK_SIZE = ByteLength.ofMiB(16);
	private static final int NUM_WRITE_THREADS = 16;
	private static final ByteLength WRITE_MIN_PART_SIZE = ByteLength.ofMiB(64);

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterClients clients;
	@Inject
	private S3ClientManager s3ClientManager;
	@Inject
	private S3BulkCopyHandlerLinks s3BulkCopyHandlerLinks;
	@Inject
	private DatarouterS3BulkCopyReadExecutor readExec;
	@Inject
	private DatarouterS3BulkCopyWriteExecutor writeExec;
	@Inject
	private S3BulkCopyConfirmationHtmlFactory confirmationHtmlFactory;

	/*-------- form ----------*/

	public static class S3BulkCopyHandlerFormParams extends BaseLink<NoOpLinkType>{

		private static final String P_client = "client";
		private static final String P_fromBucket = "fromBucket";
		private static final String P_fromPrefix = "fromPrefix";
		private static final String P_toBucket = "toBucket";
		private static final String P_toPrefix = "toPrefix";
		private static final String P_submitButton = "submitButton";

		public S3BulkCopyHandlerFormParams(){
			super(new DatarouterAwsS3Paths().datarouter.clients.awsS3.bulkCopy.form);
		}

		public Optional<String> client = Optional.empty();
		public Optional<String> fromBucket = Optional.empty();
		public Optional<String> fromPrefix = Optional.empty();
		public Optional<String> toBucket = Optional.empty();
		public Optional<String> toPrefix = Optional.empty();
		public Optional<Boolean> submitButton = Optional.empty();

		public S3BulkCopyHandlerConfirmationParams toConfirmationParams(){
			return new S3BulkCopyHandlerConfirmationParams(
					client.orElseThrow(),
					fromBucket.orElseThrow(),
					fromPrefix.orElseThrow(),
					toBucket.orElseThrow(),
					toPrefix.orElseThrow());
		}
	}

	@Handler
	public Mav form(S3BulkCopyHandlerFormParams params){
		String title = DatarouterAwsS3Plugin.NAME + " - Bulk Copy";

		// make form
		boolean submitted = params.submitButton.orElse(false);
		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addTextField()
				.withLabel("Datarouter Client Name")
				.withName(S3BulkCopyHandlerFormParams.P_client)
				.withValue(params.client.orElse(""));
		form.addTextField()
				.withLabel("From Bucket")
				.withName(S3BulkCopyHandlerFormParams.P_fromBucket)
				.withValue(params.fromBucket.orElse(""));
		form.addTextField()
				.withLabel("From Prefix")
				.withName(S3BulkCopyHandlerFormParams.P_fromPrefix)
				.withValue(params.fromPrefix.orElse(""));
		form.addTextField()
				.withLabel("To Bucket")
				.withName(S3BulkCopyHandlerFormParams.P_toBucket)
				.withValue(params.toBucket.orElse(""));
		form.addTextField()
				.withLabel("To Prefix")
				.withName(S3BulkCopyHandlerFormParams.P_toPrefix)
				.withValue(params.toPrefix.orElse(""));
		form.addButtonWithoutSubmitAction()
				.withLabel("Proceed to Confirmation")
				.withName(S3BulkCopyHandlerFormParams.P_submitButton)
				.withValue(Boolean.TRUE.toString());
		var htmlForm = Bootstrap4FormHtml.render(form)
				.withClass("card card-body bg-light");

		// show form
		if(!submitted || form.hasErrors()){
			var content = div(
					S3Html.makeHeader(title, "Copy many S3 objects from one place to another"),
					br(),
					htmlForm)
					.withClass("container");
			return pageFactory.simplePage(request, title, content);
		}

		// proceed to confirmation
		String confirmHref = s3BulkCopyHandlerLinks.confirmation(params.toConfirmationParams());
		return new GlobalRedirectMav(confirmHref);
	}

	/*-------- confirmation ----------*/

	public static class S3BulkCopyHandlerConfirmationParams extends BaseLink<NoOpLinkType>{

		private static final String P_client = "client";
		private static final String P_fromBucket = "fromBucket";
		private static final String P_fromPrefix = "fromPrefix";
		private static final String P_toBucket = "toBucket";
		private static final String P_toPrefix = "toPrefix";

		public final String client;
		public final String fromBucket;
		public final String fromPrefix;
		public final String toBucket;
		public final String toPrefix;

		public S3BulkCopyHandlerConfirmationParams(
				String client,
				String fromBucket,
				String fromPrefix,
				String toBucket,
				String toPrefix){
			super(new DatarouterAwsS3Paths().datarouter.clients.awsS3.bulkCopy.confirmation);
			this.client = client;
			this.fromBucket = fromBucket;
			this.fromPrefix = fromPrefix;
			this.toBucket = toBucket;
			this.toPrefix = toPrefix;
		}

		public BucketAndPrefix toFromToBucketAndPrefix(){
			return new BucketAndPrefix(fromBucket, fromPrefix);
		}

		public BucketAndPrefix toToBucketAndPrefix(){
			return new BucketAndPrefix(toBucket, toPrefix);
		}

		public S3BulkCopyHandlerPerformCopyParams toPerformCopyParams(){
			return new S3BulkCopyHandlerPerformCopyParams(client, fromBucket, fromPrefix, toBucket, toPrefix);
		}
	}

	@Handler
	public Mav confirmation(S3BulkCopyHandlerConfirmationParams params){
		String title = DatarouterAwsS3Plugin.NAME + " - Confirm Bulk Copy";
		var headerDiv = S3Html.makeHeader(title, "Please confirm the from/to locations look correct");
		var bodyDiv = confirmationHtmlFactory.create(params).makeAll();
		var content = div(
				headerDiv,
				br(),
				bodyDiv)
				.withClass("container");
		return pageFactory.simplePage(request, title, content);
	}

	/*-------- performCopy ----------*/

	public static class S3BulkCopyHandlerPerformCopyParams extends BaseLink<NoOpLinkType>{

		private static final String P_client = "client";
		private static final String P_fromBucket = "fromBucket";
		private static final String P_fromPrefix = "fromPrefix";
		private static final String P_toBucket = "toBucket";
		private static final String P_toPrefix = "toPrefix";

		public final String client;
		public final String fromBucket;
		public final String fromPrefix;
		public final String toBucket;
		public final String toPrefix;

		public S3BulkCopyHandlerPerformCopyParams(
				String client,
				String fromBucket,
				String fromPrefix,
				String toBucket,
				String toPrefix){
			super(new DatarouterAwsS3Paths().datarouter.clients.awsS3.bulkCopy.performCopy);
			this.client = client;
			this.fromBucket = fromBucket;
			this.fromPrefix = fromPrefix;
			this.toBucket = toBucket;
			this.toPrefix = toPrefix;
		}

		public BucketAndPrefix toFromBucketAndPrefix(){
			return new BucketAndPrefix(fromBucket, fromPrefix);
		}

		public BucketAndPrefix toToBucketAndPrefix(){
			return new BucketAndPrefix(toBucket, toPrefix);
		}
	}

	@Handler
	public Mav performCopy(S3BulkCopyHandlerPerformCopyParams params){
		String title = DatarouterAwsS3Plugin.NAME + " - Bulk Copy - Complete";
		DatarouterS3Client s3Client = getS3Client(params.client);
		var count = new AtomicLong();
		String message;
		s3Client.scan(params.toFromBucketAndPrefix())
				//TODO batch; split into small and large objects; parallelize the small objects
				.each(object -> {
					ByteLength size = ByteLength.ofBytes(object.size());
					var fromBucketAndKey = new BucketAndKey(params.fromBucket, object.key());
					var to = toNewLocation(
							fromBucketAndKey,
							params.toFromBucketAndPrefix(),
							params.toToBucketAndPrefix());
					copy(params.client, fromBucketAndKey, to, size);
					logger.warn(
							"copied id={}, size={}, from={}, to={}",
							count,
							size.toDisplay(),
							fromBucketAndKey,
							to);
				})
				.each($ -> count.incrementAndGet())
				.count();
		message = String.format("Copied %s objects", count);
		logger.warn(message);

		// show results
		var headerDiv = S3Html.makeHeader(title, "Copying has completed successfully");
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

	public static BucketAndKey toNewLocation(
			BucketAndKey bucketAndKey,
			BucketAndPrefix fromBucketAndPrefix,
			BucketAndPrefix toBucketAndPrefix){
		int fromPrefixLength = fromBucketAndPrefix.prefix().length();
		String suffix = bucketAndKey.key().substring(fromPrefixLength);
		String toKey = toBucketAndPrefix.prefix() + suffix;
		return new BucketAndKey(toBucketAndPrefix.bucket(), toKey);
	}

	// TODO move somewhere more generic?
	private DatarouterS3Client getS3Client(String clientName){
		ClientId clientId = clients.getClientId(clientName);
		return s3ClientManager.getClient(clientId);
	}

	private void copy(
			String datarouterClientName,
			BucketAndKey from,
			BucketAndKey to,
			ByteLength size){
		DatarouterS3Client s3Client = getS3Client(datarouterClientName);
		if(size.toBytes() <= READ_CHUNK_SIZE.toBytes()){
			byte[] bytes = s3Client.getObjectAsBytes(from);
			s3Client.putObject(to, ContentType.BINARY, bytes);
		}else{
			Scanner<byte[]> chunks = s3Client.scanObjectChunks(
					from,
					Range.everything(),
					new Threads(readExec, NUM_READ_THREADS),
					READ_CHUNK_SIZE.toBytesInt());
			s3Client.multithreadUpload(
					to,
					ContentType.BINARY,
					chunks.apply(MultiByteArrayInputStream::new),
					new Threads(writeExec, NUM_WRITE_THREADS),
					WRITE_MIN_PART_SIZE);
		}
	}

	/*--------- links -----------*/

	@Singleton
	public static class S3BulkCopyHandlerLinks{

		@Inject
		private ServletContextSupplier contextSupplier;

		public String form(S3BulkCopyHandlerFormParams params){
			var uriBuilder = new URIBuilder()
					.setPath(contextSupplier.getContextPath() + params.pathNode.toSlashedString());
			params.client.ifPresent(client -> uriBuilder.addParameter(
					S3BulkCopyHandlerFormParams.P_client,
					client));
			params.fromBucket.ifPresent(fromBucket -> uriBuilder.addParameter(
					S3BulkCopyHandlerFormParams.P_fromBucket,
					fromBucket));
			params.fromPrefix.ifPresent(fromPrefix -> uriBuilder.addParameter(
					S3BulkCopyHandlerFormParams.P_fromPrefix,
					fromPrefix));
			params.toBucket.ifPresent(toBucket -> uriBuilder.addParameter(
					S3BulkCopyHandlerFormParams.P_toBucket,
					toBucket));
			params.toPrefix.ifPresent(toPrefix -> uriBuilder.addParameter(
					S3BulkCopyHandlerFormParams.P_toPrefix,
					toPrefix));
			params.submitButton.ifPresent(submitButton -> uriBuilder.addParameter(
					S3BulkCopyHandlerFormParams.P_submitButton,
					submitButton.toString()));
			return uriBuilder.toString();
		}

		public String confirmation(S3BulkCopyHandlerConfirmationParams params){
			var uriBuilder = new URIBuilder()
					.setPath(contextSupplier.getContextPath() + params.pathNode.toSlashedString())
					.addParameter(S3BulkCopyHandlerConfirmationParams.P_client, params.client)
					.addParameter(S3BulkCopyHandlerConfirmationParams.P_fromBucket, params.fromBucket)
					.addParameter(S3BulkCopyHandlerConfirmationParams.P_fromPrefix, params.fromPrefix)
					.addParameter(S3BulkCopyHandlerConfirmationParams.P_toBucket, params.toBucket)
					.addParameter(S3BulkCopyHandlerConfirmationParams.P_toPrefix, params.toPrefix);
			return uriBuilder.toString();
		}

		public String performCopy(S3BulkCopyHandlerPerformCopyParams params){
			var uriBuilder = new URIBuilder()
					.setPath(contextSupplier.getContextPath() + params.pathNode.toSlashedString())
					.addParameter(S3BulkCopyHandlerPerformCopyParams.P_client, params.client)
					.addParameter(S3BulkCopyHandlerPerformCopyParams.P_fromBucket, params.fromBucket)
					.addParameter(S3BulkCopyHandlerPerformCopyParams.P_fromPrefix, params.fromPrefix)
					.addParameter(S3BulkCopyHandlerPerformCopyParams.P_toBucket, params.toBucket)
					.addParameter(S3BulkCopyHandlerPerformCopyParams.P_toPrefix, params.toPrefix);
			return uriBuilder.toString();
		}

	}

}
