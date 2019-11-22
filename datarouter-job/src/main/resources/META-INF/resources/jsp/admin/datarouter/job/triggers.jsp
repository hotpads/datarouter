<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<title>Job Triggers</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script>require(['sorttable'])</script>
	<script type="text/javascript">
		function confirmRunJob(jobName, shouldRun, serverName) {
			var displayText = "shouldRun is " + shouldRun +  ". Are you sure you want to run "+jobName+" on "+serverName+"?";
			return confirm(displayText);
		}

		function displayMessageOnload(msg) {
			if (msg != "") {
				alert('Message : ' + msg);
			}
			clearQueryParams();
		}
		function clearQueryParams(){
			var uri = window.location.toString();
			if (uri.indexOf("?") > 0) {
				var clean_uri = uri.substring(0, uri.indexOf("?"));
				window.history.replaceState({}, document.title, clean_uri);
			}
		}
	</script>
</head>
<body onload="displayMessageOnload('${message}');">
	<%@ include file="/jsp/menu/new-common-navbar.jsp"%>
	<div class="container mt-5">
		<h2 class="pb-3 border-bottom">Job triggers</h2>
		<div class="card card-body bg-light my-3">
			<form method="get" action="?">
				<div class="form-inline">
					<c:forEach items="${categoryRows}" var="row">
						<a class="btn btn-outline-secondary mr-1 mb-1 <c:if test="${row.selected}">active</c:if>" href="?category=${row.name}">
							${row.name}
						</a>
					</c:forEach>
				</div>
				<br />
				<div class="form-inline">
					<input type="hidden" name="category" value="${param.category}" />
					<input class="form-control mr-3" type="text" name="keyword" placeholder="[partial name]" autofocus="autofocus" />
					<div class="form-check mr-3">
						<input class="form-check-input" type="checkbox" id="disable" name="disabled" <c:if test="${param.disabled=='on'}">checked</c:if>>
						<label class="form-check-label" for="disable">Hide Disabled</label>
					</div>
					<div class="form-check mr-3">
						<input class="form-check-input" type="checkbox" id="enabled" name="enabled" <c:if test="${param.enabled=='on'}">checked</c:if>>
						<label class="form-check-label" for="enabled">Hide Enabled</label>
					</div>
					<input type="submit" class="btn btn-primary" value="Filter">
					<a class="btn btn-link" href="list">Clear</a>
				</div>
			</form>
		</div>
		<table class="table table-sm table-bordered sortable">
			<caption class="text-uppercase">Sortable</caption>
			<thead class="thead-dark">
				<tr>
					<th class="sorttable_nosort">#</th>
					<th class="sorttable_nosort">Run</th>
					<th>Job</th>
					<th class="sorttable_nosort">CronExpression</th>
					<th>Category</th>
					<th>LastCompletion</th>
					<th>RunningOn</th>
					<th class="sorttable_nosort">Interrupt</th>
				</tr>
			</thead>
			<tbody>
			<c:forEach items="${triggerRows}" var="row">
				<tr>
					<td>${row.rowId}</td>
					<td>
						<a class="btn text-monospace 
							<c:choose>
								<c:when test="${row.shouldRun}">btn-success</c:when>
								<c:otherwise>btn-warning</c:otherwise>
							</c:choose>"
							href="run?name=${row.className}"
							onclick="return confirmRunJob('${row.classSimpleName}', '${row.shouldRun}', '${serverName}')">
							run
						</a>
					</td>
					<td <c:choose>
							<c:when test="${row.heartbeatStatus == 'stalled'}">class="table-danger"</c:when>
							<c:when test="${row.heartbeatStatus == 'warning'}">class="table-warning"</c:when>
							<c:when test="${row.heartbeatStatus == 'ok'}">class="table-success"</c:when>
						</c:choose>>
						${row.classSimpleName}
					</td>
					<td><code class="text-nowrap">${row.cronExpression}</code></td>
					<td>${row.categoryName}</td>
					<td sorttable_customkey="${row.lastFinishSortableTime}">${row.lastFinishTime}</td>
					<td>${row.runningOnServers}</td>
					<td>
						<a class="btn btn-danger text-monospace" 
								href="interrupt?name=${row.className}" 
								onclick="return window.confirm('Are you sure?');">
							interrupt
						</a>
					</td>
				</tr>
			</c:forEach>
			</tbody>
		</table>
		<br>
		<table class="table table-bordered table-sm">
			<thead class="thead-dark"><tr><th>Color Codes</th></tr></thead>
			<tbody>
				<tr class="table-success"><td>Running job, last heartbeat within 2 seconds</td></tr>
				<tr class="table-warning"><td>Running job, last heartbeat within 2-10 seconds</td></tr>
				<tr class="table-danger"><td>Running job, last heartbeat over 10 seconds ago</td></tr>
			</tbody>
		</table>
	</div>
</body>
</html>