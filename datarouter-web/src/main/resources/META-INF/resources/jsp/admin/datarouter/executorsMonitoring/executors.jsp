<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Executors</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
	<style>
		.executor-details{
			display: none;
			height: 230px;
		}
		.table-sm td{
			padding: 0.2rem !important;
		}
		.executor-row{
			cursor: pointer;
		}
	</style>
	<script>
		window.contextPath = window.contextPath  || "${contextPath}";
	</script>
	<script src="${contextPath}/js/executorsMonitoring/executors.js"></script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container my-4">
		<h1>Executors</h1>
		<div class="d-flex flex-row align-items-center mb-2">
			<div class="mr-3">
				<input id="executor-filter" class="form-control form-control-sm" type="text" placeholder="Filter by name">
			</div>
			<label class="mb-0">
				<input type="checkbox" id="auto-refresh" checked> Auto refresh
			</label>
		</div>

		<table id="executor-table" class="table table-sm table-bordered" style="display: none;">
			<thead>
				<tr>
					<th>Name</th>
					<th>Active threads</th>
					<th>Pool size</th>
					<th>Maximum pool size</th>
					<th>Queue size</th>
					<th>Remaining queue size</th>
					<th>Completed tasks</th>
				</tr>
			</thead>
			<c:forEach items="${executors}" var="executor">
				<tr id="executor-${executor.name}" class="executor-row">
					<td class="executor-name">${executor.name}</td>
					<td>${executor.activeCount}</td>
					<td>${executor.poolSize}</td>
					<td>${executor.maxPoolSize}</td>
					<td>${executor.queueSize}</td>
					<td>${executor.remainingQueueCapacity}</td>
					<td>${executor.completedTaskCount}</td>
				</tr>
				<tr data-name="${executor.name}" class="executor-details"><td colspan="7"></td></tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>
