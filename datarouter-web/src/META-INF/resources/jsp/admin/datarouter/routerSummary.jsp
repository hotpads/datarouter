<%@ include file="/WEB-INF/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container-fluid">
		<h2>Datarouter</h2>
		<a href="${contextPath}/datarouter/routers">Datarouter Home</a> &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; routerName:<b>${param.routerName}</b>
		<h3>Nodes in Router: <b>${param.routerName}</b></h3>
		<table class="table table-striped table-bordered table-hover table-condensed">
			<thead>
				<tr>
					<th>node name</th>
					<th>data</th>
					<th>row count chart</th>
					<th>count keys</th>
					<th>count txn</th>
					<th>node type</th>
					<th>export to s3 </th>
					<th>import from S3</th> 		
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${nodeWrappers}" var="nodeWrapper">
					<tr>
						<td><a style="color: black;" href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&routerName=${param.routerName}&nodeName=${nodeWrapper.node.name}">${nodeWrapper.indentHtml}${nodeWrapper.node.name}</a></td>
						<td><a href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&routerName=${param.routerName}&nodeName=${nodeWrapper.node.name}">data</a></td>
						<td><a href="${contextPath}/datarouter/rowCountChart?submitAction=viewRowCountChart&routerName=${param.routerName}&nodeName=${nodeWrapper.node.name}">row count chart</a></td>
						<td>
							<c:if test="${nodeWrapper.sorted}">
								<a href="${contextPath}/datarouter/nodes/browseData?submitAction=countKeys&routerName=${param.routerName}&nodeName=${nodeWrapper.node.name}">count keys</a>
							</c:if>
						</td>
						<td><a href="${contextPath}/datarouter/nodes/browseData?submitAction=countWhere&routerName=${param.routerName}&nodeName=${nodeWrapper.node.name}">count txn</a></td>
						<td>${nodeWrapper.className}</td>						
						<td><a href="${contextPath}/datarouter/dataMigration?submitAction=handleDefault&routerName=${param.routerName}&nodeName=${nodeWrapper.node.name}">export to s3</a></td>					
						<td><a href="${contextPath}/datarouter/dataMigration?submitAction=showImportForm&routerName=${param.routerName}&nodeName=${nodeWrapper.node.name}">import from s3</a></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</body>
</html>