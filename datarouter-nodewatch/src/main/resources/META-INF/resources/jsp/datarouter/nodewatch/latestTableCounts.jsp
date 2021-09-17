<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>TableSizeLister</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script>require(["sorttable"])</script>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
	<div class="container my-4">
		<h2>Latest counts by client</h2>
		<c:forEach items="${latestTableCountDtoMap}" var="mapEntry">
			<br>
			<h4>${mapEntry.key}</h4>
			<table class="sortable table table-striped table-bordered table-hover table-sm">
				<thead>
					<tr>
						<th>clientName</th>
						<th>tableName</th>
						<th>numRows</th>
						<th>countTimeSeconds</th>
						<th>dateUpdated</th>
						<th>numSpans</th>
						<th>numSlowSpans</th>
						<th></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${mapEntry.value}" var="count">
						<tr>
							<td>${count.clientName}</td>
							<td><a href="?submitAction=singleTable&tableName=${count.tableName}&clientName=${count.clientName}">${count.tableName}</a></td>
							<td style="text-align:right"><fmt:formatNumber pattern="#,##0" value="${count.numRows}" /></td>
							<td sorttable_customkey="${count.countTimeMs}">${count.countTime}</td>
							<td style="white-space: nowrap">${count.dateUpdated}</td>
							<td>${count.numSpans}</td>
							<td>${count.numSlowSpans}</td>
							<td>
								<a href="?submitAction=deleteAllMetadata&tableName=${count.tableName}&clientName=${count.clientName}" onclick="return window.confirm('Are you sure, you want to delete all entries of ${count.tableName}? from TableRowCount,TableRowSample, LatestTableRowCount tables');">
									<i class="far fa-trash-alt"></i>
								</a>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</c:forEach>
	</div>
</body>
</html>