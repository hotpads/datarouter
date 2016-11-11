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
	<%@ include file="/jsp/joblet/jobletsNavbar.jspf" %>
	<div class="container" id="exceptionsTable">
		<h2 class="page-header">Joblet Exception Records</h2>
		<div class="page-content-container page-content-thicktop page-single-column">
			<table class="sortable table table-bordered table-condensed">
				<tr>
					<th>exceptionRecordId</th>
					<th>typeCode</th>
					<th>executionOrder</th>
					<th>batchSequence</th>
					<th>dataId</th>
					<th>reservedBy</th>
					<th>createdAgo</th>
					<th>restartable</th>
					<th>numItems</th>
					<th>numTasks</th>
					<th>queueId</th>
				</tr>
				<c:forEach items="${failedJoblets}" var="joblet">
					<tr>
						<td><a href="/analytics/exception/details?exceptionRecord=${joblet.exceptionRecordId}">${joblet.exceptionRecordId}</a></td>
						<td>${joblet.key.typeCode}</td>
						<td>${joblet.key.executionOrder}</td>
						<td>${joblet.key.batchSequence}</td>
						<td>${joblet.jobletDataId}</td>
						<td>${joblet.reservedBy}</td>
						<td>${joblet.createdAgo}</td>
						<td>${joblet.restartable}</td>
						<td>${joblet.queueId}</td>
						<td>${joblet.numItems}</td>
						<td>${joblet.numTasks}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</body>
</html>