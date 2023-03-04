<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Show Import</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container my-4">
		<h2>Import</h2>
		<form id="importform" name="importform" method="GET" action="?">
			<input type="hidden" name="submitAction" value="importFromS3">
			<input type="hidden" name="exportId" value="${exportId}">
			<input type="hidden" name="s3Key" value="${s3Key}">
			<table class="table table-borderless table-sm">
				<thead><tr><td>Node name</td></tr></thead>
				<tbody>
					<c:forEach var="node" items="${nodes}" varStatus="status">
						<tr><td><input class="form-control" type="text" name="datarouterNodeName" value="${node}"></td></tr>
					</c:forEach>
				</tbody>
				<tfoot><tr><td><input class="btn btn-primary" type="submit" value="Import from S3"></td></tr></tfoot>
			</table>
		</form>
		<c:if test="${not empty importResult}">
			<pre class="bg-light border p-3">${importResult}</pre>
		</c:if>
	</div>
</body>
</html>
