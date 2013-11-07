<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<html>
<head>
<title>DataRouter</title>
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
		<h2>Datarouter</h2>
		<a href="${contextPath}/datarouter/routers">DataRouter Home</a> &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; routerName:<b>${param.routerName}</b>
		<h3>
			Nodes in Router: <b>${param.routerName}</b>
		</h3>
		<table class="table table-striped table-bordered table-hover table-condensed sortable">
			<thead>
				<tr>
					<th>node name</th>
					<th>data</th>
					<th>count keys</th>
					<th>count txn</th>
<!-- 					<th>export to S3</th> -->
<!-- 					<th>import from S3</th> -->
					<th>copy table</th>
					<th>export HFiles</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${nodeWrappers}" var="nodeWrapper">
					<tr>
						<td><a style="color: black;" href="${contextPath}/datarouter/viewNodeData?submitAction=browseData&routerName=${param.routerName}
							&nodeName=${nodeWrapper.node.name}"> ${nodeWrapper.indentHtml}${nodeWrapper.node.name}</a></td>
						<td><a href="${contextPath}/datarouter/viewNodeData?submitAction=browseData&routerName=${param.routerName}
							&nodeName=${nodeWrapper.node.name}"> data</a></td>
						<td><c:if test="${nodeWrapper.sorted}">
								<a href="${contextPath}/datarouter/viewNodeData?submitAction=countKeys&routerName=${param.routerName}
								&nodeName=${nodeWrapper.node.name}"> count keys</a>
							</c:if></td>
						<td><c:if test="${true}">
								<a href="${contextPath}/datarouter/viewNodeData?submitAction=countWhere&routerName=${param.routerName}
								&nodeName=${nodeWrapper.node.name}"> count txn</a>
							</c:if></td>
<!-- 						<td> -->
<%-- 						<a href="/admin/dataRouter/export.htm?submitAction=exportToS3&routerName=${param.routerName} --%>
<%-- 						&nodeName=${nodeWrapper.node.name}"> export to S3 </a></td> --%>
<%-- 						<td><a href="/admin/dataRouter/export.htm?submitAction=showImportForm&routerName=${param.routerName} --%>
<%-- 						&nodeName=${nodeWrapper.node.name}"> import from S3 </a></td> --%>
						<td><c:if test="${nodeWrapper.isHBaseNode}">
								<a href="/admin/dataRouter/menu.htm?submitAction=copyHBaseTable&routerName=${param.routerName}
							&nodeName=${nodeWrapper.node.name}&destinationTableName="> copy table </a>
							</c:if></td>
						<td><c:if test="${nodeWrapper.isHBaseNode}">
								<a href="/admin/dataRouter/menu.htm?submitAction=exportNodeToHFile&routerName=${param.routerName}
							&nodeName=${nodeWrapper.node.name}"> export to HFile </a>
							</c:if></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</body>
</html>