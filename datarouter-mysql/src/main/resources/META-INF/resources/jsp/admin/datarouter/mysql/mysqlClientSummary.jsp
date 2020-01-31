<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container my-3">
		<h2>Datarouter ${param.clientName}</h2>
		<nav>
			<ol class="breadcrumb">
				<li class="breadcrumb-item"><a href="${contextPath}/datarouter">Datarouter Home</a></li>
				<li class="breadcrumb-item active"><b>Client: </b>${param.clientName}</li>
			</ol>
		</nav>
		<h3 class="mt-2">Client Summary</h3>
		<p>
			<b>clientName: </b>${param.clientName}
		</p>

		<h3 class="mt-2">Physical Nodes</h3>
		<table class="table table-striped table-sm">
			<tbody>
				<c:forEach items="${nodes}" var="node">
					<tr>
						<td><a title="browse data" href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&nodeName=${node.name}">${node.name}</a></td>
						<td>${node['class'].simpleName}</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</body>
</html>
