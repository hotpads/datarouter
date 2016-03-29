<%@ include file="/WEB-INF/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>Joblet Exception Records</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script type="text/javascript">
		require(['sorttable']);
	</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<div class="container" id="exceptionsTable">
		<h2 class="page-header">Joblet Exception Records</h2>
		<div class="page-content-container page-content-thicktop page-single-column">
			<table class="sortable table table-bordered table-condensed">
				<tr>
					<th>type</th>
					<th>queueId</th>
					<th>created</th>
					<th>numItems</th>
					<th>numTasks</th>
					<th>exceptionRecordId</th>
				</tr>
				<c:forEach items="${failedJoblets}" var="joblet">
					<tr>
						<td>${joblet.key.type}</td>
						<td>${joblet.queueId}</td>
						<td>${joblet.createdAgo}</td>
						<td>${joblet.numItems}</td>
						<td>${joblet.numTasks}</td>
						<td><a href="/analytics/exception/details?exceptionRecord=${joblet.exceptionRecordId}">${joblet.exceptionRecordId}</a></td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</body>
</html>