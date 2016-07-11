<%@ include file="/WEB-INF/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>Joblets</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script>
	require(['jquery','sorttable'], function(){
		var eventData = {
			<c:forEach items="${runningJobletThreads}" var="jobletThreads">
				<c:forEach items="${jobletThreads.value}" var="jobletThread">
					<c:set var="maxNumEvents" value="0"/>
					<c:set var="maxAvgDuration" value="0"/>
					${jobletThread.id}:{
						numEvents:"<c:forEach items="${jobletThread.stats.stratifiedEvents}" var="eventGroup" varStatus="status">${eventGroup.numEvents}<c:if test="${not status.last}">,</c:if><c:if test="${eventGroup.numEvents > maxNumEvents}"><c:set var="maxNumEvents" value="${eventGroup.numEvents}"/></c:if></c:forEach>",
						avgDuration:"<c:forEach items="${jobletThread.stats.stratifiedEvents}" var="eventGroup" varStatus="status">${eventGroup.averageExecutionTimeMillis}<c:if test="${not status.last}">,</c:if><c:if test="${eventGroup.averageExecutionTimeMillis > maxAvgDuration}"><c:set var="maxAvgDuration" value="${eventGroup.averageExecutionTimeMillis}"/></c:if></c:forEach>",
						labels:"<c:forEach items="${jobletThread.stats.stratumNames}" var="stratumName" varStatus="status">${stratumName}<c:if test="${not status.last}">|</c:if></c:forEach>",
						numEventsHighest: ${maxNumEvents},
						avgDurationHighest: ${maxAvgDuration}
					},
				</c:forEach>
				blank:""
			</c:forEach>
		}
		
		function generateChartUrl(threadId, type){
			var chartType = null;
			if(type=='avgDuration'){
				chartType = 'bvs';
			}
			else{
				chartType = 'lc';
			}
			return "http://chart.apis.google.com/chart?chs=1000x200&cht="+ chartType +"&chxt=x,y&chxl=0:|"+eventData[threadId].labels+"&chd=t:"
			+eventData[threadId][type]+"&chds=0,"+eventData[threadId][type+"Highest"]+"&chxr=1,0,"+eventData[threadId][type+"Highest"];
		}
	});
	</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<%@ include file="/jsp/joblet/jobletsNavbar.jspf" %>
	<div class="container-fluid" id="jobletsTable">
		<div class="page-content-container page-content-thicktop page-single-column">
			servers: min=${minServers}, max=${maxServers}, target=
				<a href="${contextPath}/datarouter/jobletScaling">view</a></p>
				<br/> <br/>totalTickets:${totalTickets}</p>

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
							<a href="/analytics/counters?submitAction=viewCounters&webApps=All&servers=All&periods=300000&counters=Joblet%20queue%20length%20${s.typeString}&frequency=second&archive=databean%20300000">${s.typeString}</a>
						</td>
						<td>${s.numType}</td>
						<td>
							<c:if test="${s.numQueueIds > 0}">
								<a href="joblets/queues?jobletType=${s.typeString}&executionOrder=${s.executionOrder}">${s.numQueueIds} queues</a>
							</c:if>
						</td>
						<td>${s.firstReservedAgo}</td>
						<td>${s.firstCreatedAgo}</td>
						<td>${s.sumItems}</td>
						<td>${s.avgItems}</td>
						<td>${s.sumTasks}</td>
						<td>${s.avgTasks}</td>
					</tr>
				</c:forEach>
			</table>

			<c:forEach items="${runningJobletThreads}" var="jobletThreads">

				<h4>${jobletThreads.key} (running)</h4>
				<%@ include file="/jsp/joblet/jobletThreadTable.jspf"%>
			</c:forEach>
			<c:forEach items="${waitingJobletThreads}" var="jobletThreads">

				<h4>${jobletThreads.key} (waiting)</h4>
				<%@ include file="/jsp/joblet/jobletThreadTable.jspf"%>
			</c:forEach>
		</div>
	</div>
</body>
</html>