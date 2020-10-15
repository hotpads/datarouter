<%@ include file="/jsp/generic/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<title>LongRunningTasks</title>
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
			LongRunningTasks
			<c:if test="${not empty param.name}">
				<small class="text-nowrap">
					named <code>${param.name}</code>
					<c:if test="${not empty filteringStatusName}"> with status <code>${filteringStatusName}</code></c:if>
				</small>
			</c:if>
		</h2>
		<form>
			<div class="input-group responsive-input-group form-group">
				<input type="text" class="form-control" name="name" placeholder="Job Class Name" value="${param.name}" ${empty param.name ? 'autofocus': ''}>
				<div id="status-selector" class="input-group-append">
					<select name="status" class="btn btn-outline-secondary form-control" style="border-radius: 0">
						<option value="${allStatusesValue}">ALL</option>
						<option value="---" disabled>---</option>
						<c:forEach items="${statuses}" var="status">
							<option value="${status.right}" <c:if test="${status.right == displayedStatus}">selected</c:if>>${status.left}</option>
						</c:forEach>
					</select>
					<input type="submit" value="Search" class="form-control btn btn-primary">
				</div>
			</div>
		</form>
		<c:if test="${empty longRunningTasks}">
			<div class="card bg-light text-center mb-4">
				<h4 class="card-body m-0">No Long Running Tasks matched this criteria</h4>
			</div>
		</c:if>
	</div>
	<c:if test="${not empty longRunningTasks}">
		<div class="container-fluid">
			<div class="table-responsive">
				<table class="sortable table table-bordered table-sm">
					<caption class="text-right">SORTABLE TABLE</caption>
					<thead>
						<tr>
							<th>name</th>
							<th>server</th>
							<th>start</th>
							<th>finish</th>
							<th class="text-right">duration</th>
							<th class="text-right">last heartbeat</th>
							<th class="sorttable_nosort">last item</th>
							<th class="text-right">item count</th>
							<th>status</th>
							<th>cause</th>
							<th>exception</th>
						</tr>
					</thead>
					<tbody>
					<c:forEach items="${longRunningTasks}" var="task">
						<tr>
							<td>
								<c:choose>
								<c:when test="${param.status == allStatusesValue}">
									${task.name}
								</c:when>
								<c:otherwise>
									<a title="Show all tasks for ${task.name}" href="${task.hrefForTasksWithSameName}">${task.name}</a>
								</c:otherwise>
								</c:choose>
							</td>
							<td>${task.serverName}</td>
							<td sorttable_customkey="${task.startTime}" title="${task.startSubtitle}">${task.startString}</td>
							<td sorttable_customkey="${task.sortableFinishTime}" title="${task.finishTimeString}">${task.finishTime}</td>
							<td sorttable_customkey="${task.sortableDuration}" class="text-right">${task.durationString}</td>
							<td sorttable_customkey="${task.sortableLastHeartbeat}" title="${task.lastHeartbeat}" class="text-right
								<c:choose>
									<c:when test="${task.heartbeatStatus == 'stalled'}">table-danger</c:when>
									<c:when test="${task.heartbeatStatus == 'warning'}">table-warning</c:when>
									<c:when test="${task.heartbeatStatus == 'ok'}">table-success</c:when>
								</c:choose>">
								${task.lastHeartbeatString}
							</td>
							<td>${task.lastItemProcessed}</td>
							<td class="text-right">${task.numItemsProcessed}</td>
							<td <c:if test="${param.status == allStatusesValue and task.status != 'success' and task.status != 'running'}">
								class="table-warning"
							</c:if>>${task.status}</td>
							<td>${task.triggeredBy}</td>
							<td>
								<a href="${task.hrefForException}">${task.exceptionRecordId}</a>
							</td>
						</tr>
					</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
	</c:if>
	<div class="container mt-5">
	${legend}
	</div>
</body>
</html>