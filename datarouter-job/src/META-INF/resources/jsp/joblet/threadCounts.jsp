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
	<div class="container">
		<h2 class="page-header">Joblet Thread Counts</h2>
		<div class="page-content-container page-content-thicktop page-single-column">
			numInstances: ${numInstances}<br/>
			<br/>
			<table class="sortable table table-bordered table-condensed">
				<tr>
					<th>jobletType</td>
					<th>clusterLimit</td>
					<th>instanceAvg</td>
					<th>instanceLimit</td>
					<th>numExtraThreads</td>
					<th>firstExtraInstanceIndex</td>
					<th>firstExtraInstanceServerName</td>
				</tr>
				<c:forEach items="${jobletThreadCountDtos}" var="dto">
					<tr>
						<td>${dto.jobletType}</td>
						<td>${dto.clusterLimit}</td>
						<td>${dto.instanceAvg}</td>
						<td>${dto.instanceLimit}</td>
						<td>${dto.numExtraThreads}</td>
						<td>${dto.firstExtraInstanceIndex}</td>
						<td>${dto.firstExtraInstanceServerName}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</body>
</html>