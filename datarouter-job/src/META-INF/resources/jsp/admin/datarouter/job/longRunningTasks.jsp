<%@ include file="/WEB-INF/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>Currently Running LongRunningTasks</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp"%>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<div class="container" id="jobletsTable">
		<h2 class="page-header">Currently Running LongRunningTasks</h2>
		<div class="page-content-container page-content-thicktop page-single-column">
			<table class="sortable table table-bordered table-condensed">
				<tr>
					<th>triggerTime</th>
					<th>jobClass</th>
					<th>serverName</th>
					<th>startTime</th>
					<th>duration</th>
					<th>lastHeartbeat</th>
					<th>numItemsProcessed</th>
					<th>jobExecutionStatus</th>
					<th>lastFinishTime</th>
				</tr>
				<c:forEach items="${currentlyRunningTasks}" var="task">
					<tr <c:choose><c:when test="${task.status == 2}">style="background: #FF9999;"</c:when>
						<c:when test="${task.status == 1}">style="background: #FFEE00;"</c:when></c:choose>>
						<td>${task.key.triggerTime}</td>
						<td>${task.key.jobClass}</td>
						<td>${task.key.serverName}</td>
						<td>${task.startTime}</td>
						<td>${task.durationString}</td>
						<td>${task.lastHeartbeatString}</td>
						<td>${task.numItemsProcessed}</td>
						<td>${task.jobExecutionStatus}</td>
						<td>${lastCompletions[task.key.jobClass].finishTimeString}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</body>
</html>