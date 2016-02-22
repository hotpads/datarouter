<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<html>
<head>
	<title>DR ${param.clientName}</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<div class="container">
		<h2>DR ${param.clientName}</h2>
		<a href="${contextPath}/datarouter/routers">Datarouter Home</a>
		&nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; <a
			href="${contextPath}/datarouter/routers?submitAction=inspectRouter&routerName=${param.routerName}">Router:
			${param.routerName}</a> &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; <b>Client:
		</b> ${param.clientName} <br /> <br /> <b>zookeeper.quorum: </b>
		${address}<br /> <br /> These are not node names. They are table names
		from HBaseAdmin.listTables();<br /> <br />
		<table class="table table-striped table-bordered table-hover table-condensed sortable">
			<thead>
				<tr>
					<th>table name</th>
					<th>links</th>
					<th>table settings</th>
					<th>family settings</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${tableSummaryByName}" var="tableSummary">
					<tr>
						<td>
							${tableSummary.key}
						</td>
						<td>
							<a href="hbase?submitAction=viewHBaseTableRegions&routerName=${param.routerName}
								&clientName=${param.clientName}&tableName=${tableSummary.key}">regions</a>
							<br/>
							<a href="hbase?submitAction=viewHBaseTableSettings&routerName=${param.routerName}
								&clientName=${param.clientName}&tableName=${tableSummary.key}">settings</a>
							<br/>
							<a href="hbase?submitAction=viewHBaseServers&routerName=${param.routerName}
								&clientName=${param.clientName}">servers</a>
						</td>
						<td>
							<table class="table table-striped table-bordered table-hover table-condensed sortable">
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
								family: ${familyEntry.key}<br/>
								<table class="table table-striped table-bordered table-hover table-condensed sortable">
									<tbody>
										<c:forEach items="${familyEntry.value}" var="entry">
											<tr>
												<td>${entry.key}</td>
												<td>${entry.value}</td>
											</tr>
										</c:forEach>
									</tbody>
								</table>
								<br/>
							</c:forEach>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</body>
</html>