<%@ include file="/WEB-INF/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>Joblets</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script>
	require(['sorttable']);
	</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<%@ include file="/jsp/joblet/jobletsNavbar.jspf" %>
	<div class="container-fluid" id="jobletsTable">
		<div class="page-content-container page-content-thicktop page-single-column">
			servers: min=${minServers}, max=${maxServers}<br/>

			<table class="jobletTable sortable table table-bordered table-condensed" style="border-collapse:collapse;">
				<tr>
					<th>numFailures</th>
					<th>executionOrder</th>
					<th>status</th>
					<th>type(click for queue/processed counts)</th>
					<th>numType</th>
					<th>queueId</th>
					<th>firstReserved</th>
					<th>firstCreated</th>
					<th>sumItems</th>
					<th>avgItems</th>
					<th>sumTasks</th>
					<th>avgTasks</th>
				</tr>
				<c:forEach items="${summaries}" var="s">
					<tr>
						<td>${s.numFailures}</td>
						<td>${s.executionOrder}</td>
						<td>${s.status}</td>
						<td>
							<a href="/analytics/counters?submitAction=viewCounters&webApps=All&servers=All&periods=300000&counters=Joblet%20queue%20length%20items%20${s.typeString}&frequency=second&archive=databean%20300000">${s.typeString}</a>
						</td>
						<td>${s.numType}</td>
						<td>
							<c:if test="${s.numQueueIds > 0}">
								<a href="joblets/queues?jobletType=${s.typeString}&jobletTypeCode=${s.typeCode}&executionOrder=${s.executionOrder}">${s.numQueueIds} queues</a>
							</c:if>
						</td>
						<td>${s.firstReservedAgo}</td>
						<td>${s.firstCreatedAgo}</td>
						<td>${s.sumItems}</td>
						<td>${s.avgItemsString}</td>
						<td>${s.sumTasks}</td>
						<td>${s.avgTasksString}</td>
					</tr>
				</c:forEach>
			</table>
			
			<br/>
			<br/>
			RunningJoblets:
			<%@ include file="/jsp/joblet/runningJoblets.jspf"%>
		</div>
	</div>
</body>
</html>