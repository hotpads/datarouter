<%@ include file="/jsp/generic/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<title>JobsHealth</title>
	<script>require(["sorttable"])</script>
	<style>
		@media only screen and (max-width: 600px){
			#status-selector{ width: 100px; }
			#status-selector > select { width: 100%; padding: 0; }
		}
	</style>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container mt-5">
		<h2>
			JobsHealth
		</h2>
		<div class="card bg-light text-center mb-4">
			<h4 class="card-body m-0">Number of Unique Jobs in the last 24 Hours: ${numUniqueJobs}</h4>
			<button type="button" data-toggle="collapse" data-target="#tasklist"> Show Unique Jobs 
			</button>
			<div id="tasklist" class="collapse">
		    <c:forEach items="${uniqueJobs}" var="task">
				<div>
					${task}
				</div>
			</c:forEach>
		  </div>
		</div>
		  
		  <div class="card bg-light text-center mb-4">
			<h4 class="card-body m-0">Number of Currently Running Jobs: ${numRunningJobs}</h4>
		  </div>
	</div>
	
	<div class="container mt-5">
		<h4> All Non-Successful/Running Tasks </h4>
	</div>
	
	<div>
		<c:if test="${not empty allBadTasks}">
			<div class="container-fluid">
				<div class="table-responsive">
					<table class="sortable table table-bordered table-sm">
						<caption class="text-right">SORTABLE TABLE</caption>
						<thead>
							<tr>
								<th>jobClass</th>
								<th>serverName</th>
								<th>startTime</th>
								<th>finishTime</th>
								<th class="text-right">duration</th>
								<th class="text-right">lastHeartbeat</th>
								<th class="sorttable_nosort">lastItemProcessed</th>
								<th class="text-right">numItemsProcessed</th>
								<th>jobExecutionStatus</th>
								<th>triggeredBy</th>
							</tr>
						</thead>
						<tbody>
						<c:forEach items="${allBadTasks}" var="task">
							<tr>
								<td>${task.name}</td>
								<td>${task.serverName}</td>
								<td sorttable_customkey="${task.startTime.time}" title="${task.startTimeString}">${task.startTime}</td>
								<td sorttable_customkey="${task.sortableFinishTime}" title="${task.finishTimeString}">${task.finishTime}</td>
								<td sorttable_customkey="${task.sortableDuration}" class="text-right">${task.durationString}</td>
								<td sorttable_customkey="${task.sortableLastHeartbeat}" class="text-right
									<c:choose>
										<c:when test="${task.heartbeatStatus == 'stalled'}">table-danger</c:when>
										<c:when test="${task.heartbeatStatus == 'warning'}">table-warning</c:when>
										<c:when test="${task.heartbeatStatus == 'ok'}">table-success</c:when>
									</c:choose>">${task.lastHeartbeatString}</td>
								<td>${task.lastItemProcessed}</td>
								<td class="text-right">${task.numItemsProcessed}</td>
								<td <c:if test="${task.status != 'success' and task.status != 'running'}">
									class="table-warning"
								</c:if>>${task.status}</td>
								<td>${task.triggeredBy}</td>
							</tr>
						</c:forEach>
						</tbody>
					</table>
				</div>
			</div>
		</c:if>
	</div>				
	
	<div class="container mt-5">
	${legend}
	</div>	
</body>
</html>