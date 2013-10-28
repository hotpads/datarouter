<%@ include file="/WEB-INF/prelude.jspf" %>
<html>
<head>
<title>DR ${param.tableName}</title>
<link rel="stylesheet" type="text/css" href="/css/admin.css" />
<script>
require(["util/ext/sorttable"], function() {});
</script>

<style>
#mainDecoratorDiv { width:100% }
</style>
</head>
<body>
<div class="page-container">
<h2 class="page-title">DR ${param.tableName}</h2>
<div class="clearfix"></div>
<div class="page-content-container page-content-thicktop page-single-column">
<a href="/admin/dataRouter/menu.htm">DataRouter Home</a>
&nbsp;&nbsp;>>&nbsp;&nbsp;
<a href="/admin/dataRouter/menu.htm?submitAction=inspectRouter&routerName=${param.routerName}">router: ${param.routerName}</a>
&nbsp;&nbsp;>>&nbsp;&nbsp;
<a href="/admin/dataRouter/menu.htm?submitAction=inspectClient&routerName=${param.routerName}
		&clientName=${param.clientName}">client: ${param.clientName}</a>
&nbsp;&nbsp;>>&nbsp;&nbsp;
table: ${param.tableName}
<br/>
<br/>
<h3 style="width:100%;border-bottom:1px solid gray;">Regions</h3>
<br/>
<form method="post" action="?" name="mainform">
<table>
<tr>
	<td>
		routerName:${param.routerName}<br/>
		clientName:${param.clientName}<br/>
		tableName:${param.tableName}<br/>
		<a href="/admin/dataRouter/menu.htm?submitAction=viewHBaseTableSettings&routerName=${param.routerName}
				&clientName=${param.clientName}&tableName=${param.tableName}">Table Settings</a>
	</td>
	<td>	
		move regions to correct server:
		<span class="button confirmation-button"><span class="button-left">
			<a href="javascript:document.mainform.submitAction.value='moveRegionsToCorrectServer';document.mainform.submit();"
			 	onclick="return window.confirm('Are you sure you want to moveRegionsToCorrectServer?');">&nbsp;Move all regions&nbsp;</a>
		</span></span><br/>
		move selected regions to:<br/>
			<select name="destinationServerName">
				<option>&nbsp;&nbsp;--&nbsp;&nbsp;select&nbsp;&nbsp;--&nbsp;&nbsp;</option>&nbsp;&nbsp;
				<c:forEach items="${serverNames}" var="serverName">
					<option value="${serverName}">${serverName}</option>
				</c:forEach>
			</select><br/>
		<span class="button confirmation-button"><span class="button-left">
			<a href="javascript:document.mainform.submitAction.value='moveHBaseTableRegions';document.mainform.submit();">&nbsp;Submit&nbsp;</a>
		</span></span><br/>
	</td>
	<td style="text-align:right;">
		compact:
		<span class="button confirmation-button"><span class="button-left">
			<a href="javascript:document.mainform.submitAction.value='compactHBaseTableRegions';document.mainform.submit();">&nbsp;Selected Regions&nbsp;</a>
		</span></span>
		<span class="button confirmation-button"><span class="button-left">
			<a href="javascript:document.mainform.submitAction.value='compactAllHBaseTableRegions';document.mainform.submit();"
			 	onclick="return window.confirm('Are you sure you want to compact all regions?');">&nbsp;All Regions&nbsp;</a>
		</span></span><br/>
		major compact:
		<span class="button confirmation-button"><span class="button-left">
			<a href="javascript:document.mainform.submitAction.value='majorCompactHBaseTableRegions';document.mainform.submit();">&nbsp;Selected Regions&nbsp;</a>
		</span></span>
		<span class="button confirmation-button"><span class="button-left">
			<a href="javascript:document.mainform.submitAction.value='majorCompactAllHBaseTableRegions';document.mainform.submit();"
			 	onclick="return window.confirm('Are you sure you want to majorCompact all regions?');">&nbsp;All Regions&nbsp;</a>
		</span></span><br/>
		flush:
		<span class="button confirmation-button"><span class="button-left">
			<a href="javascript:document.mainform.submitAction.value='flushHBaseTableRegions';document.mainform.submit();">&nbsp;Selected Regions&nbsp;</a>
		</span></span>
		<span class="button confirmation-button"><span class="button-left">
			<a href="javascript:document.mainform.submitAction.value='flushAllHBaseTableRegions';document.mainform.submit();"
			 	onclick="return window.confirm('Are you sure you want to flush all regions?');">&nbsp;All Regions&nbsp;</a>
		</span></span><br/>
		merge:
		<span class="button confirmation-button"><span class="button-left">
			<a href="javascript:document.mainform.submitAction.value='mergeFollowingHBaseTableRegions';document.mainform.submit();">&nbsp;Selected Regions&nbsp;</a>
		</span></span><br/>
	</td>
</tr>
</table>
groupBy:
<c:set var="groupByUrlBase" value="/admin/dataRouter/menu.htm?submitAction=viewHBaseTableRegions&routerName=${param.routerName}
		&clientName=${param.clientName}&tableName=${param.tableName}"/>
<a style="<c:if test="${empty param.groupBy}">font-weight:bold;</c:if>" href="${groupByUrlBase}">all</a>&nbsp;-&nbsp;
<a style="<c:if test="${param.groupBy=='serverName'}">font-weight:bold;</c:if>" href="${groupByUrlBase}&groupBy=serverName">serverName</a>
<br/>
 	<input type="hidden" name="submitAction" />
	<c:forEach items="${regionsByGroup}" var="group" varStatus="groupStatus">
	<br/>
	<b>${group.key}</b>: ${fn:length(group.value)} regions<br/>
	<br/>"
	<table class="data sortable">
		<tr>
			<th>#</th>
			<th></th>
			<th>host</th>
			<th>tgt</th>
			<th>mem</th>
			<th>disk</th>
			<th>index</th>
			<th>stores</th>
			<th>storefiles</th>
			<th>kvs</th>
			<th>reads</th>
			<th>writes</th>
			<th>next major compaction</th>
			<th>startKey</th>
			<th>endKey</th>
			<th>id</th>
			<th>encodedName</th>
			<th>hbase-style name</th>
		</tr>
		<c:forEach items="${group.value}" var="region" varStatus="regionStatus">
			<tr<c:if test="${regionStatus.index%5==0}"> class="highlighted"</c:if>>
				<td>${region.regionNum}</td>
				<td style="text-align:center;">
					<input type="checkbox" name="encodedRegionName_${region.region.encodedName}"/>
				</td>
				<td>${region.displayServerName}</td>
				<c:set var="wrongServer" value="${region.displayServerName != region.consistentHashDisplayServerName}"/>
				<td style="<c:if test="${wrongServer}">color:#FF515D;</c:if>">
					<c:if test="${wrongServer}">${region.consistentHashDisplayServerName}</c:if>
				</td>
				<td class="right">${region.load.memStoreSizeMB}</td>
				<td class="right">${region.load.storefileSizeMB}</td>
				<td class="right">${region.load.storefileIndexSizeMB}</td>
				<td class="right">${region.load.stores}</td>
				<td class="right" style="<c:if test="${region.load.storefiles > 1}">font-weight:bold;</c:if>">${region.load.storefiles}</td>
				<td class="right">${region.numKeyValuesWithCompactionPercent}</td>
				<td class="right">${region.load.readRequestsCount}</td>
				<td class="right">${region.load.writeRequestsCount}</td>
				<td class="">${region.compactionScheduler.nextCompactTimeFormatted}</td>
				<td><c:if test="${not empty region.startKey}">${region.startKey.persistentString}</c:if></td>
				<td><c:if test="${not empty region.endKey}">${region.endKey.persistentString}</c:if></td>
				<td class="right">${region.region.regionId}</td>
				<td>${region.region.encodedName}</td>
				<td>${region.region.regionNameAsString}</td>
			</tr>
		</c:forEach>
	</table>
	</c:forEach>
	<br/>
	<input type="hidden" name="routerName" value="${param.routerName}"/>
	<input type="hidden" name="clientName" value="${param.clientName}"/>
	<input type="hidden" name="tableName" value="${param.tableName}"/>

</form>

</div></div>
</body>
</html>