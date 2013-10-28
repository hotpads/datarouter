<%@ include file="/WEB-INF/prelude.jspf" %>
<html>
<head>
<title>DR Table Settings</title>
<script>
require(["util/ext/sorttable"], function() {});
</script>
<%@ include file="/jsp/generic/css-import.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
<div class="container">
<h2 >DR Table Settings</h2>
<a href="/admin/dataRouter/menu.htm">DataRouter Home</a>
&nbsp;&nbsp;>>&nbsp;&nbsp;
<a href="/admin/dataRouter/menu.htm?submitAction=inspectRouter&routerName=${param.routerName}">router: ${param.routerName}</a>
&nbsp;&nbsp;>>&nbsp;&nbsp;
<a href="/admin/dataRouter/menu.htm?submitAction=inspectClient&routerName=${param.routerName}
		&clientName=${param.clientName}">client: ${param.clientName}</a>
&nbsp;&nbsp;>>&nbsp;&nbsp;
table: ${param.tableName}

<h3>Table Settings</h3>
routerName:${param.routerName}<br/>
clientName:${param.clientName}<br/>
tableName:${param.tableName}<br/>
table level settings:<br/>
<form method="post" action="?">
	<input type="hidden" name="submitAction" value="updateHBaseTableAttribute"/>
	<input type="hidden" name="routerName" value="${param.routerName}"/>
	<input type="hidden" name="clientName" value="${param.clientName}"/>
	<input type="hidden" name="tableName" value="${param.tableName}"/>
	
	MAX_FILESIZE (<b>enter in MEGAbytes!!</b>):
	<input name="maxFileSizeMb" value="" type="text" />
	currently: ${tableParamByName['MAX_FILESIZE']} <b>mega</b>bytes<br/>
	
	MEMSTORE_FLUSHSIZE (<b>enter in MEGAbytes!!</b>):
	<input name="memstoreFlushSizeMb" value="" type="text"/>
	currently :${tableParamByName['MEMSTORE_FLUSHSIZE']} <b>mega</b>bytes<br/>
	
	<input type="submit" class="btn btn-success"/>
</form>
<br/>
	<table class="table table-striped table-bordered  table-condensed">
	<tr>
		<th>column family name</th>
		<th>settings</th>
	</tr>
<c:forEach items="${columnSummaryByName}" var="columnSummary">
	<tr>
		<td>
			<!--<a href="?submitAction=summary&routerName=${param.routerName}
				&nodeName=${param.tableName}&columnName=${param.columnName}"></a>-->
				${columnSummary.key}
		</td>
		<td>
			<b>WARNING - DO NOT ENTER INVALID VALUES OR EXTRA WHITESPACE</b>
			<br/>
			<form method="post" action="?" class="form-horizontal">
				<input type="hidden" name="submitAction" value="updateHBaseColumnAttribute"/>
				<input type="hidden" name="routerName" value="${param.routerName}"/>
				<input type="hidden" name="clientName" value="${param.clientName}"/>
				<input type="hidden" name="tableName" value="${param.tableName}"/>
				<input type="hidden" name="columnName" value="${columnSummary.key}"/>
				<c:forEach items="${columnSummary.value}" var="attributeByName">
				<div class="span11 row-fluid">
				<label class="span3" for="${attributeByName.key}" >${attributeByName.key}:</label>
				<div class="span3">
					<input type="text"  id="${attributeByName.key}" name="${attributeByName.key}" value="${attributeByName.value}" />
				</div>
				</div>
				</c:forEach>
				<div class="row-fluid">
				<input type="submit" class="btn btn-success"/>
				</div>
			</form>
		</td>
	</tr>
</c:forEach>
</table>
<br/>
DATA_BLOCK_ENCODING in ('NONE', 'PREFIX', 'DIFF', 'FAST_DIFF')<br/>
COMPRESSION in ('LZO', 'GZ', 'NONE')<br/>
BLOOM in ('NONE', 'ROW', 'ROWCOL')<br/>
<br/>
</div>
</div>
</body>
</html>