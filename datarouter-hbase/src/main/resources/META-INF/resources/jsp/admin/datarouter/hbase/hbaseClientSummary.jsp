<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter ${param.clientName}</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
	<div class="container my-4">
		<h2>Datarouter ${param.clientName}</h2>
		<nav>
			<ol class="breadcrumb">
				<li class="breadcrumb-item"><a href="${contextPath}/datarouter">Datarouter Home</a></li>
				<li class="breadcrumb-item active"><b>Client: </b>${param.clientName}</li>
			</ol>
		</nav>
		<b>zookeeper.quorum: </b> ${address}<br>
		<table class="table table-striped table-bordered table-sm">
			<caption style="caption-side: top">These are not node names. They are table names from HBaseAdmin.listTables();</caption>
			<thead>
				<tr>
					<th>Table name</th>
					<th>Links</th>
					<th>Table settings</th>
					<th>Family settings</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${tableSummaryByName}" var="tableSummary">
					<tr>
						<td>
							${tableSummary.key}
						</td>
						<td>
							<ul>
								<li><a href="${contextPath}${hbaseHandlerPath}?submitAction=viewHBaseTableRegions&clientName=${param.clientName}&tableName=${tableSummary.key}">regions</a></li>
								<li><a href="${contextPath}${hbaseHandlerPath}?submitAction=viewHBaseTableSettings&clientName=${param.clientName}&tableName=${tableSummary.key}">settings</a></li>
								<li><a href="${contextPath}${hbaseHandlerPath}?submitAction=viewHBaseServers&clientName=${param.clientName}">servers</a></li>
							</ul>
						</td>
						<td>
							<table class="table table-striped table-bordered table-sm">
								<tbody>
									<c:forEach items="${tableSummaryByName[tableSummary.key]}" var="entry">
										<tr>
											<td>${entry.key}</td>
											<td>${entry.value}</td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</td>
						<td>
							<c:forEach items="${familySummaryByTableName[tableSummary.key]}" var="familyEntry">
								<div><strong>Family: ${familyEntry.key}</strong></div>
								<table class="table table-striped table-bordered table-sm">
									<tbody>
										<c:forEach items="${familyEntry.value}" var="entry">
											<tr>
												<td>${entry.key}</td>
												<td>${entry.value}</td>
											</tr>
										</c:forEach>
									</tbody>
								</table>
							</c:forEach>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</body>
</html>