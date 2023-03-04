<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Import</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container my-4">
		<h2>Show Import</h2>
		<form id="importform" name="importform" method="GET" action="?">
			<table class="table table-borderless table-sm">
				<thead>
					<tr>
						<td>Node name</td>
						<td>Start After Key</td>
						<td>End After Key</td>
						<td>Max Rows</td>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${nodeExportList}" var="node">
						<tr>
							<td><input name="nodeName" value="${node.getNodeName()}" class="form-control" readonly></td>
							<td><input name="startAfterKey" value="${node.getStartAfterKey()}" class="form-control" readonly></td>
							<td><input name="endBeforeKey" value="${node.getEndBeforeKey()}" class="form-control" readonly></td>
							<td><input name="maxRows" value="${node.getMaxRows()}" class="form-control" readonly></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</form>
		<br/>
		${exportResultHtml}
	</div>
</body>
</html>
