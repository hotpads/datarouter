<%@ include file="/WEB-INF/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>Joblet Threads</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<%@ include file="/jsp/joblet/jobletsNavbar.jspf" %>
	<div class="container" id="jobletsTable">
		<h2 class="page-header">Joblet Threads</h2>
		<div class="page-content-container page-content-thicktop page-single-column">
			CPU and Memory Permit info: throttling enabled: ${isThrottling}
			<table class="sortable table table-bordered table-condensed">
				<tr>
					<th>type</td>
					<th>numRunningPermits</td>
					<th>numAvailablePermits</td>
				</tr>
				<tr>
					<td>CPU</td>
					<td>${totalRunningCpuPermits}</td>
					<td>${numCpuPermits}</td>
				</tr>
				<tr>
					<td>Memory</td>
					<th>${totalRunningMemoryPermits}</th>
					<td>${numMemoryPermits}</td>
				</tr>
			</table>
		
			By type on this server:
			<table class="sortable table table-bordered table-condensed">
				<tr>
					<th>jobletType</th>
					<th>numThreads</th>
					<th>numRunning</th>
					<th>numRunningCpuPermits</th>
					<th>numRunningMemoryPermits</th>
				</tr>
				<tr>
					<th>Total:</th>
					<th>${totalThreads}</th>
					<th>${totalRunning}</th>
					<th>${totalRunningCpuPermits}</th>
					<th>${totalRunningMemoryPermits}</th>
				</tr>
				<c:forEach items="${typeSummaryDtos}" var="dto">
					<tr>
						<td>${dto.jobletType}</td>
						<td>${dto.numThreads}</td>
						<td>${dto.numRunning}</td>
						<td>${dto.numRunningCpuPermits}</td>
						<td>${dto.numRunningMemoryPermits}</td>
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