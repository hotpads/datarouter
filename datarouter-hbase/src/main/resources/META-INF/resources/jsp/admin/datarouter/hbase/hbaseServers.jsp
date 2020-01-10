<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>HBase Servers</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script>require(["sorttable"])</script>
</head>
<body>
	<%@ include file="/jsp/menu/new-common-navbar.jsp"%>
	<div class="container-fluid my-4">
		<h2>
			HBase Servers
			<small class="text-muted"> - ${param.clientName}</small>
		</h2>
		<nav>
			<ol class="breadcrumb">
				<li class="breadcrumb-item"><a href="${contextPath}/datarouter">Datarouter Home</a></li>
				<li class="breadcrumb-item">
					<a href="${contextPath}/datarouter/clients?submitAction=inspectClient&clientName=${param.clientName}">
						client: ${param.clientName}
					</a>
				</li>
				<li class="breadcrumb-item active">table: ${param.tableName}</li>
			</ol>
		</nav>
		<div class="table-responsive">
			<table class="table table-striped table-bordered table-hover table-sm sortable">
				<thead>
					<tr>
						<th>serverName</th>
						<th>regions</th>
						<th>stores</th>
						<th>storeUncompressedSizeMB</th>
						<th>storefileIndexSizeInMB</th>
						<th>storefiles</th>
						<th>storefileSizeInMB</th>
						<th>rootIndexSizeKB</th>
						<th>totalStaticIndexSizeKB</th>
						<th>totalStaticBloomSizeKB</th>
						<th>maxHeapMB</th>
						<th>usedHeapMB</th>
						<th>memstoreSizeInMB</th>
						<th>readRequestsCount</th>
						<th>writeRequestsCount</th>
						<th>totalRequests</th>
						<th>totalCompactingKVs</th>
						<th>currentCompactedKVs</th>
						<th>coprocessors</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${servers}" var="server" varStatus="status">
						<tr>
							<td>${server.serverName}</td>
							<td class="text-right">${server.serverLoad.numberOfRegions}</td>
							<td class="text-right">${server.serverLoad.stores}</td>
							<td class="text-right">${server.serverLoad.storeUncompressedSizeMB}</td>
							<td class="text-right">${server.serverLoad.storefileIndexSizeInMB}</td>
							<td class="text-right">${server.serverLoad.storefiles}</td>
							<td class="text-right">${server.serverLoad.storefileSizeInMB}</td>
							<td class="text-right">${server.serverLoad.rootIndexSizeKB}</td>
							<td class="text-right">${server.serverLoad.totalStaticIndexSizeKB}</td>
							<td class="text-right">${server.serverLoad.totalStaticBloomSizeKB}</td>
							<td class="text-right">${server.serverLoad.maxHeapMB}</td>
							<td class="text-right">${server.serverLoad.usedHeapMB}</td>
							<td class="text-right">${server.serverLoad.memstoreSizeInMB}</td>
							<td class="text-right">${server.serverLoad.readRequestsCount}</td>
							<td class="text-right">${server.serverLoad.writeRequestsCount}</td>
							<td class="text-right">${server.serverLoad.totalNumberOfRequests}</td>
							<td class="text-right">${server.serverLoad.totalCompactingKVs}</td>
							<td class="text-right">${server.serverLoad.currentCompactedKVs}</td>
							<td class="text-right">${fn:length(server.serverLoad.rsCoprocessors)}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</body>
</html>