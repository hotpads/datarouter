<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>HBase Regions - ${param.tableName}</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
	<script>require(["sorttable"])</script>
	<style>
		td{
			white-space: nowrap;
		}
		.right{
			text-align: right;
		}
	</style>
</head>
<body>
	<%@ include file="/jsp/menu/new-common-navbar.jsp" %>
	<div class="container-fluid my-4">
		<form method="post" action="?" name="mainform">
			<input type="hidden" name="submitAction" />
			<input type="hidden" name="clientName" value="${param.clientName}" />
			<input type="hidden" name="tableName" value="${param.tableName}" />
			<div class="container-fluid">
				<h2>${param.tableName}</h2>
				<ol class="breadcrumb">
					<li class="breadcrumb-item"><a href="${contextPath}/datarouter">Datarouter Home</a></li>
					<li class="breadcrumb-item">
						<a href="${contextPath}/datarouter/clients?submitAction=inspectClient&clientName=${param.clientName}">
							client: ${param.clientName}
						</a>
					</li>
					<li class="breadcrumb-item active">
						<a href="?submitAction=viewHBaseTableSettings&clientName=${param.clientName}&tableName=${param.tableName}">
							table: ${param.tableName}
						</a>
					</li>
				</ol>
				<h3 class="page-header">Regions</h3>
				<div class="row">
					<div class="col-12 col-md-6">
						<ul>
							<li>move regions to correct server:
								<span class="button confirmation-button">
									<span class="button-left">
										<a href="javascript:
												document.mainform.submitAction.value='moveRegionsToCorrectServer';
												document.mainform.submit();"
											onclick="return window.confirm('Are you sure you want to moveRegionsToCorrectServer?');">
											&nbsp;Move all regions&nbsp;
										</a>
									</span>
								</span>
							</li>
							<li>move selected regions to:
								<select name="destinationServerName">
									<option>&nbsp;&nbsp;--&nbsp;&nbsp;select&nbsp;&nbsp;--&nbsp;&nbsp;</option>
									<c:forEach items="${serverNames}" var="serverName">
										<option value="${serverName}">${serverName}</option>
									</c:forEach>
								</select>
								<span class="button confirmation-button">
									<span class="button-left">
										<a href="javascript:
												document.mainform.submitAction.value='moveHBaseTableRegions';
												document.mainform.submit();">
											&nbsp;Submit&nbsp;
										</a>
									</span>
								</span>
							</li>
						</ul>
					</div>
					<div class="col-12 col-md-6">
						<ul>
							<li>compact:
								<span class="button confirmation-button">
									<span class="button-left">
										<a href="javascript:
												document.mainform.submitAction.value='compactHBaseTableRegions';
												document.mainform.submit();">
											&nbsp;Selected Regions&nbsp;
										</a>
									</span>
								</span>
								<span class="button confirmation-button">
									<span class="button-left">
										<a href="javascript:
												document.mainform.submitAction.value='compactAllHBaseTableRegions';
												document.mainform.submit();"
											onclick="return window.confirm('Are you sure you want to compact all regions?');">
											&nbsp;All Regions&nbsp;
										</a>
									</span>
								</span>
							</li>
							<li>major compact:
								<span class="button confirmation-button">
									<span class="button-left">
										<a href="javascript:
												document.mainform.submitAction.value='majorCompactHBaseTableRegions';
												document.mainform.submit();">
											&nbsp;Selected Regions&nbsp;
										</a>
									</span>
								</span>
								<span class="button confirmation-button">
									<span class="button-left">
										<a href="javascript:
												document.mainform.submitAction.value='majorCompactAllHBaseTableRegions';
												document.mainform.submit();"
											onclick="return window.confirm('Are you sure you want to majorCompact all regions?');">
											&nbsp;All Regions&nbsp;
										</a>
									</span>
								</span>
							</li>
							<li>flush:
								<span class="button confirmation-button">
									<span class="button-left">
										<a href="javascript:
												document.mainform.submitAction.value='flushHBaseTableRegions';
												document.mainform.submit();">
											&nbsp;Selected Regions&nbsp;
										</a>
									</span>
								</span>
								<span class="button confirmation-button">
									<span class="button-left">
										<a href="javascript:
												document.mainform.submitAction.value='flushAllHBaseTableRegions';
												document.mainform.submit();"
											onclick="return window.confirm('Are you sure you want to flush all regions?');">
											&nbsp;All Regions&nbsp;
										</a>
									</span>
								</span>
							</li>
							<li>merge:
								<span class="button confirmation-button">
									<span class="button-left">
										<a href="javascript:
												document.mainform.submitAction.value='mergeFollowingHBaseTableRegions';
												document.mainform.submit();">
											&nbsp;Selected Regions&nbsp;
										</a>
									</span>
								</span>
							</li>
							<li>split:
								<span class="button confirmation-button">
									<span class="button-left"> 
										<a href="javascript:
												document.mainform.submitAction.value='splitPartitions';
												document.mainform.submit();">
											&nbsp;By Regions/Partitions&nbsp;
										</a>
									</span>
								</span>
							</li>
						</ul>
					</div>
				</div>
				groupBy:
				<c:set var="groupByUrlBase"
					value="?submitAction=viewHBaseTableRegions&clientName=${param.clientName}&tableName=${param.tableName}" />
				<a style="<c:if test="${empty param.groupBy}">font-weight:bold;</c:if>"
					href="${groupByUrlBase}">
					all
				</a>
				&nbsp;-&nbsp;
				<a style="<c:if test="${param.groupBy=='serverName'}">font-weight:bold;</c:if>"
					href="${groupByUrlBase}&groupBy=serverName">
					serverName
				</a>
				<br/>
				<c:forEach items="${regionsByGroup}" var="group" varStatus="groupStatus">
					<br/>
					<b>${group.key}</b>: ${fn:length(group.value)} regions
					<br/>
					<br/>
					<table class="table table-striped table-bordered table-collapse sortable">
						<thead>
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
								<th>partition</th>
								<th>startKey</th>
								<th>endKey</th>
								<th>id</th>
								<th>encodedName</th>
								<th>hbase-style name</th>
							</tr>
						</thead>
						<c:forEach items="${group.value}" var="region" varStatus="regionStatus">
							<tr <c:if test="${regionStatus.index%5==0}"> class="highlighted"</c:if>>
								<td>${region.regionNum}</td>
								<td style="text-align: center;">
									<input type="checkbox" name="encodedRegionName_${region.region.encodedName}" />
								</td>
								<td>${region.displayServerName}</td>
								<c:set var="wrongServer"
									value="${region.displayServerName != region.consistentHashDisplayServerName}" />
								<td style="<c:if test="${wrongServer}">color:#FF515D;</c:if>">
									<c:if test="${wrongServer}">${region.consistentHashDisplayServerName}</c:if>
								</td>
								<td class="right">${region.load.memStoreSizeMB}</td>
								<td class="right">${region.load.storefileSizeMB}</td>
								<td class="right">${region.load.storefileIndexSizeMB}</td>
								<td class="right">${region.load.stores}</td>
								<td class="right"
									style="<c:if test="${region.load.storefiles > 1}">font-weight:bold;</c:if>">
									${region.load.storefiles}
								</td>
								<fmt:formatNumber type="number" value="${balance}" />
								<td class="right">${region.numKeyValuesWithCompactionPercent}</td>
								<td class="right"><fmt:formatNumber type="number" value="${region.load.readRequestsCount}" /></td>
								<td class="right"><fmt:formatNumber type="number" value="${region.load.writeRequestsCount}" /></td>
								<td>${region.compactionScheduler.nextCompactTimeFormatted}</td>
								<td>${region.partition}</td>
								<td><c:if test="${not empty region.startKey}">${region.startKey}</c:if></td>
								<td><c:if test="${not empty region.endKey}">${region.endKey}</c:if></td>
								<td class="right">${region.region.regionId}</td>
								<td>${region.region.encodedName}</td>
								<td>${region.region.regionNameAsString}</td>
							</tr>
						</c:forEach>
					</table>
				</c:forEach>
				<br/>
			</div>
		</form>
	</div>
</body>
</html>