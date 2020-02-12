<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Threshold</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script>require(['sorttable'])</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
	<div class="container my-4">
		<h2>NodeWatch Thresholds</h2>
		<table class="table table-hover table-sm sortable">
			<thead class="thead-dark">
				<tr>
					<th>Dao name</th>
					<th>Node name</th>
					<th>Threshold value</th>
					<th class="sorttable_nosort"></th>
				</tr>
			</thead>
			<tbody>
			<c:forEach items="${thresholdSettings}" var="setting">
				<form method="get" action="${contextPath}${thresholdPath}">
					<tr>
						<td><input type="text" name="clientName" class="form-control-plaintext" value='${setting.key.getClientName()}' readonly></td>
						<td><input type="text" name="tableName" class="form-control-plaintext" value='${setting.key.getTableName()}' readonly></td>
						<td sorttable_customkey="${setting.getMaxRows()}"><input type="number" name="threshold" class="form-control" value='${setting.getMaxRows()}'></td>
						<td class="text-center">
							<input type="hidden" value="updateThreshold" name="submitAction">
							<button class="btn btn-warning">Update</button>
						</td>
					</tr>
				</form>
			</c:forEach>
			</tbody>
		</table>
	</div>
</body>
</html>
