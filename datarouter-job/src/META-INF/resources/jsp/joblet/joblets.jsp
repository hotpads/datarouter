<%@ include file="/WEB-INF/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>Joblets</title>
	<%@ include file="/WEB-INF/jsp/generic/jobHead.jsp" %>
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
			    //add function to tr
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
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/WEB-INF/jsp/generic/navBar.jsp" %>
	<div class="container" id="jobletsTable">
		<h2 class="page-header">Joblets</h2>
		<div class="page-content-container page-content-thicktop page-single-column">
			servers: min=${minServers}, max=${maxServers}, target=
				<a href="${contextPath}/datarouter/jobletScaling">view</a></p>

			<p><%@ include file="/jsp/joblet/monitoringLinkBar.jsp" %></p>
			<p>
			<a	class="<c:if test="${empty whereStatus}">selected</c:if> btn btn-mini"
				href="?submitAction=showJoblets">all</a>&nbsp;
			<c:forEach items="${jobletStatuses}" var="status">
				<a class="<c:if test="${whereStatus==status.persistentString}">selected</c:if> btn btn-mini"
				href="?submitAction=showJoblets&expanded=${expanded}&whereStatus=${status.persistentString}"
				>${status.persistentString}</a>
			</c:forEach>
			</p>
				<p>
				<a href="?submitAction=restartFailed" class="btn btn-mini" 
					onclick="return confirm('Are you sure you want to restart failed joblets?');">restart failed joblets</a>
				<a href="?submitAction=timeoutStuckRunning" class="btn btn-mini" 
					onclick="return confirm('Are you sure you want to timeout stuck running joblets?');">timeout stuck running joblets</a>
				<a href="?submitAction=deleteTimedOutJoblets" class="btn btn-mini" 
					onclick="return confirm('Are you sure you want to delete timed out joblets?');">delete timed out joblets</a>
				<a href="?submitAction=resetQueueTickets" class="btn btn-mini" 
					onclick="return confirm('Are you sure you want to reset queue tickets?');">reset queue tickets</a>
				<a href="?submitAction=restartTimedOut" class="btn btn-mini" 
					onclick="return confirm('Are you sure you want to restart timedOut joblets?');">restart timedOut joblets</a>
				<br/>
				<br/>
				<a href="?submitAction=showJoblets&expanded=true" class="btn btn-mini">expand queues</a>
				<br/> <br/>totalTickets:${totalTickets}</p>

			<table class="jobletTable sortable table table-bordered table-striped table-condensed" style="border-collapse:collapse;">
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
						<c:if
							test="${not empty s.queueId and not empty queuesById[s.queueId]}">
							<td><a
								href="?submitAction=alterQueueNumTickets&ref=showJoblets&whereStatus=${whereStatus}&queueId=${s.queueId}&numTickets=${s.numType}">=</a>
								<a
								href="?submitAction=alterQueueNumTickets&ref=showJoblets&whereStatus=${whereStatus}&queueId=${s.queueId}&diff=-1">-</a>
								${queuesById[s.queueId].numTickets} <a
								href="?submitAction=alterQueueNumTickets&ref=showJoblets&whereStatus=${whereStatus}&queueId=${s.queueId}&diff=1">+</a>
								<a href="/admin/feeds/edit/${s.queueId}">${queuesById[s.queueId].maxTickets}</a> 
							</td>
						</c:if>
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

			<p>
			<a	href="?submitAction=restartExecutor&jobletType=AreaLookup" class="btn btn-mini" 
				onclick="return confirm('Are you sure you want to restart AreaLookup executor?')">Restart
				AreaLookup Executor</a>
				
			<a	href="?submitAction=restartExecutor&jobletType=AreaListingsLookup" class="btn btn-mini" 
				onclick="return confirm('Are you sure you want to restart AreaListingsLookup executor?')">Restart
				AreaListingsLookup Executor</a>
				
			<a	href="?submitAction=restartExecutor&jobletType=AreaRelationshipLookup" class="btn btn-mini" 
				onclick="return confirm('Are you sure you want to restart AreaRelationshipLookup executor?')">Restart
				AreaRelationshipLookup Executor</a>

			<a	href="?submitAction=restartExecutor&jobletType=AreaBorderRendering" class="btn btn-mini" 
				onclick="return confirm('Are you sure you want to restart AreaBorderRendering executor?')">Restart
				AreaBorderRendering Executor</a>					
					
			<a href="?submitAction=restartExecutor&jobletType=FeedImport" class="btn btn-mini" 
				onclick="return confirm('Are you sure you want to restart FeedImport executor?')">Restart
				FeedImport Executor</a>
			<a href="?submitAction=restartExecutor&jobletType=ListingDeletion" class="btn btn-mini" 
				onclick="return confirm('Are you sure you want to restart ListingDeletion executor?')">Restart
				ListingDeletion Executor</a>
			<a href="?submitAction=restartExecutor&jobletType=ListingViewRendering" class="btn btn-mini" 
				onclick="return confirm('Are you sure you want to restart ListingViewRendering executor?')">Restart
				ListingViewRendering Executor</a>
			<a href="?submitAction=restartExecutor&jobletType=Geocoding" class="btn btn-mini" 
				onclick="return confirm('Are you sure you want to restart Geocoding executor?')">Restart
				Geocoding Executor</a>
			<a href="?submitAction=restartExecutor&jobletType=ListingDupeCheck" class="btn btn-mini" 
				onclick="return confirm('Are you sure you want to restart ListingDupeCheck executor?')">Restart
				ListingDupeCheck Executor</a>
			<a href="?submitAction=restartExecutor&jobletType=PhotoDownload" class="btn btn-mini" 
				onclick="return confirm('Are you sure you want to restart PhotoDownload executor?')">Restart
				PhotoDownload Executor</a>
			<a href="?submitAction=restartExecutor&jobletType=DailyPricingStats" class="btn btn-mini" 
				onclick="return confirm('Are you sure you want to restart DailyPricingStats executor?')">Restart
				DailyPricingStats Executor</a>
			<a href="?submitAction=restartExecutor&jobletType=ImageCaching" class="btn btn-mini" 
				onclick="return confirm('Are you sure you want to restart ImageCaching executor?')">Restart
				ImageCaching Executor</a>
			</p>
		</div>
	</div>
</body>
</html>