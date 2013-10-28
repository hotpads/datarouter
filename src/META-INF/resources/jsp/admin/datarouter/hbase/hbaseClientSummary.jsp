<%@ include file="/WEB-INF/prelude.jspf" %>
<html>
<head>
<title>DR ${param.clientName}</title>
<script>
require(["util/ext/sorttable"], function() {});
</script>

<%@ include file="/jsp/generic/css-import.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
<body>
<div class="container">
<h2>DR ${param.clientName}</h2>
<a href="/admin/dataRouter/menu.htm">DataRouter Home</a>
&nbsp;&nbsp;>>&nbsp;&nbsp;
<a href="/admin/dataRouter/menu.htm?submitAction=inspectRouter&routerName=${param.routerName}">Router: ${param.routerName}</a>
&nbsp;&nbsp;>>&nbsp;&nbsp;
Client: ${param.clientName}
<br/>
<br/>
zookeeper.quorum:${address}<br/>
<br/>
These are not node names.  They are table names from HBaseAdmin.listTables();<br/>
<br/>
	<table class="table table-striped table-bordered table-hover table-condensed sortable">
	<tr>
		<th>table name</th>
		<th>regions</th>
		<th>settings</th>
		<th>count cells</th>
	</tr>
<c:forEach items="${tableSummaryByName}" var="tableSummary">
	<tr>
		<td>
			${tableSummary.key}
		</td>
		<td>
			<a href="/admin/dataRouter/menu.htm?submitAction=viewHBaseTableRegions&routerName=${param.routerName}
					&clientName=${param.clientName}&tableName=${tableSummary.key}">regions</a>
		</td>
		<td>
			<a href="?submitAction=viewHBaseTableSettings&routerName=${param.routerName}
					&clientName=${param.clientName}&tableName=${tableSummary.key}">settings</a>
		</td>
		<td>
			<a href="/admin/dataRouter/menu.htm?submitAction=countHBaseTableCells&routerName=${param.routerName}
					&clientName=${param.clientName}&tableName=${tableSummary.key}">count cells</a>
		</td>
		<!--
		<td>
			<c:forEach items="${tableSummary.value}" var="attributeByName">
				${attributeByName.key}=${attributeByName.value}<br/>
			</c:forEach>
		</td>
		 -->
	</tr>
</c:forEach>
</table>

</div>
</body>
</html>