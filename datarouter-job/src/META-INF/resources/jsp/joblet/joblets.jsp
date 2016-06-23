<%@ include file="/WEB-INF/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>Joblets</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script>
	require(['jquery'], function(){
		$(document).ready(function() {
			$(".jobletTable tr[class^='aggregatedSummaryRow']").each(function() {
				//add function to tr
				$(this).click(function() {
					//toggle all elements to next Level1 class
					$(this).nextUntil("[class^='aggregatedSummaryRow']").toggle();
				});
			});
			$(".jobletTable tr[class^='detailedSummaryRow']").each(function() {
				$(this).hide();
			});
		});
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
	<style>
		.includesMultipleQueues{
			background-color: #F0F8FF;
			cursor: pointer;
		}
	</style>
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
					<th>tickets</th>
					<th>queueId</th>
					<th>firstReserved</th>
					<th>firstCreated</th>
					<th>sumItems</th>
					<th>avgItems</th>
					<th>sumTasks</th>
					<th>avgTasks</th>
				</tr>
				<c:forEach items="${summaries}" var="s">
					<c:choose>
						<c:when test="${expanded || lastType!=s.typeString || lastExecutionOrder!=s.executionOrder}">
							<c:set var="lastExecutionOrder" value="${s.executionOrder}"/>
							<c:set var="lastType" value="${s.typeString}"/>
							<tr class="aggregatedSummaryRow <c:if test="${s.expandable}">includesMultipleQueues</c:if>">
						</c:when>
						<c:otherwise>
							<tr class="detailedSummaryRow">
						</c:otherwise>
					</c:choose>
						<td>${s.numFailures}</td>
						<td>${s.executionOrder}</td>
						<td>${s.status}</td>
						<c:choose>
							<c:when test="${s.status == 'created'}">
								<td><a href="/analytics/counters?submitAction=viewCounters&webApps=All&servers=All&periods=300000&counters=Joblet%20queue%20length%20${s.typeString}&frequency=second&archive=databean%20300000">${s.typeString}</a></td>
							</c:when>
							<c:when test="${s.status == 'running'}">
								<td><a href="/analytics/counters?submitAction=viewCounters&webApps=All&servers=All&periods=300000&counters=Joblet%20items%20processed%20${s.typeString}&frequency=second&archive=databean%20300000">${s.typeString}</a></td>
							</c:when>
							<c:otherwise>
								<td>${s.typeString}</td>
							</c:otherwise>
						</c:choose>
						<td>${s.numType}</td>
						<td>
							<a href="joblets/queues?jobletType=${s.typeString}&executionOrder=${s.executionOrder}">queues</a>
						</td>
						<c:if test="${empty s.queueId or empty queuesById[s.queueId]}">
							<td></td>
						</c:if>
						<td>${s.queueId}</td>
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