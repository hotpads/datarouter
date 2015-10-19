<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<html>
<head>
<title>DR Servers</title>
<%@ include file="/jsp/generic/head.jsp" %>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
	require([
            "plugin/sorttable", "bootstrap/bootstrap"
    ], function($) {});
</script>
<%@ include file="/jsp/css/css-import.jspf" %>
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<h2 class="title">DR Servers</h2>
		<a href="${contextPath}/datarouter/routers">Datarouter Home</a> &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; 
		<a href="${contextPath}/datarouter/routers?submitAction=inspectRouter&routerName=${param.routerName}">router: ${param.routerName}</a> &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; 
		<a href="${contextPath}/datarouter/clients/hbase?submitAction=inspectClient&routerName=${param.routerName}
		&clientName=${param.clientName}">client: ${param.clientName}</a> &nbsp;&nbsp;>>&nbsp;&nbsp; table: ${param.tableName}
		<h3>Servers</h3>
		<b>routerName: </b>${param.routerName}<br /> <b>clientName: </b>${param.clientName}<br />
	</div>
	<div class="wide-container">
		<table class="table table-striped table-bordered table-hover table-condensed sortable">
			<thead>
				<tr>
					<th>serverName</th>
					<th>regions</th>
					<th>maxHeapMB</th>
					<th>memStoreSizeInMB</th>
					<th>requests</th>
					<th>storefileIndexSizeInMB</th>
					<th>storefiles</th>
					<th>storefileSizeInMB</th>
					<th>totalRequests</th>
					<th>usedHeapMB</th>
					<th>version</th>
					<th>coprocessors</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${servers}" var="server" varStatus="status">
					<tr <c:if test="${status.index%5==0}"> class="highlighted"</c:if>>
						<%
							/*<td>${fn:replace(server.hostname,'HadoopNode','')}</td>*/
						%>
						<td class="right">${server.serverName}</td>
						<td class="right">${server.hServerLoad.numberOfRegions}</td>
						<td class="right">${server.hServerLoad.maxHeapMB}</td>
						<td class="right">${server.hServerLoad.memStoreSizeInMB}</td>
						<td class="right">${server.hServerLoad.numberOfRequests}</td>
						<td class="right">${server.hServerLoad.storefileIndexSizeInMB}</td>
						<td class="right">${server.hServerLoad.storefiles}</td>
						<td class="right">${server.hServerLoad.storefileSizeInMB}</td>
						<td class="right">${server.hServerLoad.totalNumberOfRequests}</td>
						<td class="right">${server.hServerLoad.usedHeapMB}</td>
						<td class="right">${server.hServerLoad.version}</td>
						<td class="right">${fn:length(server.hServerLoad.coprocessors)}</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</body>
</html>