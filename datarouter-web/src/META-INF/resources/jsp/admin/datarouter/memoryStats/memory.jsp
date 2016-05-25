<%@ include file="../../../generic/prelude-datarouter.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Memory Statistic</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<style type="text/css">
		body {
			padding-right: 0;
			padding-left: 0;
		}
		.property{
			display: inline-block;
			width: 130px;
			text-align: left;
			vertical-align: top;
		}
		.value{
			display: inline-block;
			width: 120px;
			overflow-wrap: break-word
		}
		.tree-level-1:before, .tree-level-2:before{
			content: '\21B3';
			padding: 0 5px;
		}
		.tree-level-1:before{
			padding-left: 5px;
		}
		.tree-level-2:before{
			padding-left: 20px;
		}
		.block {
			display: inline-block;
			margin: 0 20px 20px;
			text-align: right;
			vertical-align: top;
		}
		.block h6, .block h5, .block h4, .block h3, .block h2, .block h1{
			margin: 10px 0 0;
		}
		.auto-centered-container {
			text-align: center;
		}
		h6, h5, h4, h3, h2, h1{
			text-align: left;
		}
		.panel-heading {
			text-align: left;
		}
		.no-value-on-0 .tree-level-0{
			width: 255px;
		}
	</style>
	<script>
	require(['jquery'], function(){
		$(document).ready(function(){
			$('#loading-example-btn').click(function () {
				var answer = confirm('Do you really to run the garbage collector on ${serverName}');
				if(!answer){
					return false;
				}
				var btn = $(this);
				btn.button('loading');
				var start = new Date().getTime();
				setInterval(function() {
					var now = new Date().getTime();
					var diff = now - start;
					btn.text('In progress ' + Math.round(diff/100)/10 + 's');
				}, 100);
			});
		});
	});
	</script>
</head>
<body class="input-no-margin">
<%@ include file="/jsp/menu/common-navbar.jsp"%>
<%@ include file="/jsp/menu/dr-navbar.jsp"%>
<div class="auto-centered-container">
	<c:if test="${not empty param.sameServer}">
		<div class="alert alert-danger">The request come from another server. Are you sure to be on an server specific url?</div>
	</c:if>
	<div class="block">
		<h2>Server</h2>
		<span class="property tree-level-0">Start time</span>
		<span class="value">${startTime}</span>
		<br>
		<span class="property tree-level-0">Up time</span>
		<span class="value">${upTime}</span>
		<br>
		<span class="property tree-level-0">Name</span>
		<span class="value">${serverName}</span>
		<br>
		<span class="property tree-level-0" style="width:85px">Web server</span>
		<span class="value"  style="width:165px">${serverVersion}</span>
		<br>
		<span class="property tree-level-0">Java version</span>
		<span class="value" title="${jvmVersion}">${javaVersion}</span>
		<br>
		<span class="property tree-level-0">Web application</span>
		<span class="value">${appName}</span>
		<br>
		<span class="property tree-level-0" title="${gitDescribeShort}">Version</span>
		<span class="value"></span>
		<br>
		<span class="property tree-level-1">Branch</span>
		<span class="value">${gitBranch}</span>
		<br>
		<span class="property tree-level-1">Commit</span>
		<span class="value" title="${gitCommitTime} by ${gitCommitUserName}">${gitCommit}</span>
		<br>
		<span class="property tree-level-1">Build time</span>
		<span class="value">${buildTime}</span>
		<br>
		<span class="property tree-level-1">Tags</span>
		<span class="value">${gitTags}</span>
		<br>
		<c:set var="name" value="HotPads libraries"/>
		<c:set var="escapedName" value="HotPads-libraries"/>
		<c:set var="defaultVisible" value="in"/>
		<c:set var="libs" value="${hotPadsLibraries}"/>
		<c:set var="map" value="true"/>
		<%@ include file="libraries.jsp" %>

		<c:set var="name" value="Non-HotPads libraries"/>
		<c:set var="escapedName" value="Non-HotPads-libraries"/>
		<c:set var="defaultVisible" value=""/>
		<c:set var="libs" value="${nonHotPadsLibraries}"/>
		<c:set var="map" value="false"/>
		<%@ include file="libraries.jsp" %>
	</div>
	<div class="block">
		<h2>Threads</h2>
		<span class="property tree-level-0">processors</span>
		<span class="value">${procNumber}</span>
		<br>
		<span class="property tree-level-0">thread</span>
		<span class="value">${threadCount}</span>
		<br>
		<span class="property tree-level-1">daemon</span>
		<span class="value">${daemon}</span>
		<br>
		<span class="property tree-level-1">non daemon</span>
		<span class="value">${nonDaemon}</span>
		<br>
		<span class="property tree-level-0">peak</span>
		<span class="value">${peak}</span>
		<br>
		<span class="property tree-level-0">started</span>
		<span class="value">${started}</span>
		<br>
	</div>
	<div class="block">
		<h2>Memory</h2>
		<div class="panel-group" id="accordion">
			<c:set var="name" value="Heap"/>
			<c:set var="escapedName" value="Heap"/>
			<c:set var="defaultVisible" value="in"/>
			<c:set var="total" value="${heap}"/>
			<c:set var="pools" value="${heaps}"/>
			<%@ include file="memoryPool.jsp" %>

			<c:set var="name" value="Non-Heap"/>
			<c:set var="escapedName" value="Non-Heap"/>
			<c:set var="defaultVisible" value=""/>
			<c:set var="total" value="${nonHeap}"/>
			<c:set var="pools" value="${nonHeaps}"/>
			<%@ include file="memoryPool.jsp" %>
		</div>
	</div>
	<div class="block">
		<h2>Garbage collector</h2>
		<c:forEach items="${gcs}" var="gc">
			<span class="property tree-level-0">${gc.name}</span>
			<span class="value"></span>
			<br>
			<span class="property tree-level-1">Count</span>
			<span class="value">${gc.collectionCount}</span>
			<br>
			<span class="property tree-level-1">Time</span>
			<span class="value">${gc.collectionTime}</span>
			<br>
			<span class="property tree-level-1">Memory Pools</span>
			<span class="value"></span>
			<br>
			<c:forEach items="${gc.memoryPoolNames}" var="memoryPoolName">
				<span>${memoryPoolName}</span>
				<br>
			</c:forEach>
			<br>
		</c:forEach>
		<div style="text-align: left">
			<a
			class="btn btn-danger" id="loading-example-btn" data-loading-text="In progress..."
			href="${contextPath}/datarouter/memory/garbageCollector?serverName=${serverName}">
				Run garbage collector
			</a>
			<c:if test="${not empty duration}">
				<div style="text-align: right;">
					<h3>Previous manual run</h3>
					<span class="property tree-level-0">Time</span>
					<span class="value">${duration/1000}s</span>
					<br>
					<c:forEach items="${effects}" var="gcEffect">
						<span class="property tree-level-0">${gcEffect.name}</span>
						<span class="value"></span>
						<br>
						<span class="property tree-level-1">Memory saved</span>
						<span class="value">${gcEffect.saved}</span>
						<br>
						<span class="property tree-level-1">Percentage</span>
						<fmt:formatNumber var="pct" value="${100 * gcEffect.pct}" maxFractionDigits="0"/>
						<span class="value">${pct}%</span>
						<br>
					</c:forEach>
				</div>
			</c:if>
		</div>
	</div>
</div>
</body>
</html>
