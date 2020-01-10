<%@ include file="/jsp/generic/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>HBase Table Settings</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
<%@ include file="/jsp/menu/new-common-navbar.jsp" %>
<div class="container my-4">
	<h2>DR Table Settings</h2>
	<a href="${contextPath}/datarouter">Datarouter Home</a>
	&nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp;
	<a href="${contextPath}/datarouter/clients?submitAction=inspectClient&clientName=${param.clientName}">
		client: ${param.clientName}
	</a>
	&nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp;
	table: ${param.tableName}

	<h3>Table Settings</h3>
	<b>clientType: </b>${clientType}<br/>
	<b>clientName: </b>${param.clientName}<br/>
	<b>tableName: </b>${param.tableName}<br/>
	<b>table level settings: </b><br/>
	<form method="post" action="?">
		<input type="hidden" name="submitAction" value="updateHBaseTableAttribute"/>
		<input type="hidden" name="clientName" value="${param.clientName}"/>
		<input type="hidden" name="tableName" value="${param.tableName}"/>

		MAX_FILESIZE (<b>enter in MEGAbytes!!</b>):
		<input name="maxFileSizeMb" value="" type="text"/>
		currently: ${tableParamByName['MAX_FILESIZE']} <b>mega</b>bytes<br/>

		MEMSTORE_FLUSHSIZE (<b>enter in MEGAbytes!!</b>):
		<input name="memstoreFlushSizeMb" value="" type="text"/>
		currently :${tableParamByName['MEMSTORE_FLUSHSIZE']} <b>mega</b>bytes<br/>

		<input type="submit" class="btn btn-success"/>
	</form>
	<br/>
	<table class="table table-striped table-bordered table-sm">
		<thead>
		<tr>
			<th>column family name</th>
			<th>settings</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach items="${columnSummaryByName}" var="columnSummary">
			<tr>
				<td>
					${columnSummary.key}
				</td>
				<td>
					<b>WARNING - DO NOT ENTER INVALID VALUES OR EXTRA WHITESPACE</b>
					<br/>
					<form method="post" action="?" class="form-horizontal">
						<input type="hidden" name="submitAction" value="updateHBaseColumnAttribute"/>
						<input type="hidden" name="clientName" value="${param.clientName}"/>
						<input type="hidden" name="tableName" value="${param.tableName}"/>
						<input type="hidden" name="columnName" value="${columnSummary.key}"/>
						<c:forEach items="${columnSummary.value}" var="attributeByName">
							<div class="form-group">
								<label class="mb-0" for="${attributeByName.key}">${attributeByName.key}</label>
								<input type="text" id="${attributeByName.key}" class="form-control" name="${attributeByName.key}" value="${attributeByName.value}"/>
							</div>
						</c:forEach>
						<div class="row-fluid">
							<input type="submit" class="btn btn-success"/>
						</div>
					</form>
				</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<br/>
	DATA_BLOCK_ENCODING in ${dataBlockEncodingOptions}<br/>
	COMPRESSION in ${compressionOptions}<br/>
	BLOOM in ${bloomOptions}<br/>
</div>
</body>
</html>