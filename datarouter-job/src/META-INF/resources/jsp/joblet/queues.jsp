<%@ include file="/WEB-INF/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>Joblet Queues</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script>
	require(["sorttable"]);
	</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<%@ include file="/jsp/joblet/jobletsNavbar.jspf" %>
	<div class="container-fluid">
		<div class="page-content-container page-content-thicktop page-single-column">
			jobletType: ${jobletType} (${jobletTypeCode}), executionOrder: ${executionOrder}<br/><br/>
			<table class="sortable table table-bordered table-condensed" style="border-collapse:collapse;">
				<tr>
					<th>queueId</th>
					<th>status</th>
					<th>numFailures</th>
					<th>numJoblets</th>
					<th>firstReserved</th>
					<th>firstCreated</th>
					<th>sumItems</th>
					<th>avgItems</th>
					<th>sumTasks</th>
					<th>avgTasks</th>
				</tr>
				<c:forEach items="${summaries}" var="s">
					<tr>
						<td>${s.queueId}</td>
						<td>${s.status}</td>
						<td>${s.numFailures}</td>
						<td>${s.numType}</td>
						<td>${s.firstReservedAgo}</td>
						<td>${s.firstCreatedAgo}</td>
						<td>${s.sumItems}</td>
						<td>${s.avgItems}</td>
						<td>${s.sumTasks}</td>
						<td>${s.avgTasks}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</body>
</html>