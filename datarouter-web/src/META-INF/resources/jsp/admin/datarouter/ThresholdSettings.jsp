<%@ include file="../../generic/prelude-datarouter.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Threshold</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<h2>Set Threshold</h2>		
		<h3 class="">Routers and Clients</h3>
		
		<c:forEach items="${routers}" var="router">			
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			${router['class'].simpleName}
			<table class="table table-striped table-bordered table-hover table-condensed">					
				<c:forEach items="${nodeWrappers}" var="nodeWrapper">
					<tr>
						<td><a style="color: black;" href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&routerName=${param.routerName}&nodeName=${nodeWrapper.node.name}">${nodeWrapper.indentHtml}${nodeWrapper.node.name}</a></td>
						<td><a href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&routerName=${param.routerName}&nodeName=${nodeWrapper.node.name}">set Threshold</a></td>
					</tr>
				</c:forEach>
			</table>
			<br/>
		</c:forEach>
		
	</div>
</body>
</html>