<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Memory Statistic</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<style type="text/css">
		table.definition{
			white-space: nowrap;
			width: 100%;
			letter-spacing: .2px; /*increases readability*/
		}
		table.definition tr td:first-child{
			font-weight: bold;
		}
		table.definition.light tr.sub td:first-child,
		table.definition.light tr.sub-2 td:first-child{
			font-weight: normal;
		}
		table.definition tr td:nth-child(2){
			text-align: right;
		}
		table.definition tr.sub td:first-child:before{
			content: '\21B3';
			padding: 0 5px;
		}
		table.definition tr.sub-2 td:first-child:before{
			content: '\21B3';
			padding: 0 5px 0 calc(1rem + 5px);
		}
	</style>
	<script>
	require(['jquery'], function($){

		function createDefinitionTr(label, value, sub = false){
			return $('<tr>').addClass(sub ? 'sub' : '')
				.append($('<td>').append(label))
				.append($('<td>').append(value))
		}

		function createDefinitionTrLabel(label){
			return $('<tr>')
				.append($('<td>').attr('colspan', '2').append(label))
		}

		$(function(){
			$('#loading-example-btn').click(function(){
				if(!confirm('Do you really to run the garbage collector on ${serverName}')){
					return false;
				}
				$('#garbage-collector-error').remove()
				const btn = $(this).text('loading')
				btn.siblings('.garbage-collector-results').remove()
				const start = new Date().getTime()
				const interval = setInterval(function() {
					var diff = new Date().getTime() - start
					btn.text('In progress ' + Math.round(diff/100)/10 + 's')
				}, 100)
				$.get("${contextPath}/datarouter/memory/garbageCollector?serverName=${serverName}").done(function(response){
					window.clearInterval(interval)
					btn.text('Run garbage collector')
					if(response.success){
						btn.after($('<div>').addClass('garbage-collector-results card p-2 mb-2')
							.append($('<h5>').text('Previous manual run'))
							.append($('<table>').addClass('definition light').append(
								$('<tbody>')
									.append(createDefinitionTr('Time', response.duration/1000 + 's'))
									.append(response.effects.reduce((agg, effect) => [
										...agg, 
										createDefinitionTrLabel(effect.name),
										createDefinitionTr('Memory saved', effect.saved, true),
										createDefinitionTr('Percentage', Math.round(100 * effect.pct) + '%', true)
									], [])))))
					}else{
						$('#page-container').before(
							$('<div>')
								.attr('id', 'garbage-collector-error')
								.addClass('alert alert-danger rounded-0')
								.text('The request came from another server. Are you sure you are on an server specific url?'))
					}
				})
			})
		})
	})
	</script>
</head>
<body class="input-no-margin">
<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
<div class="container-fluid my-2 my-md-5" style="max-width: 1400px" id="page-container">
	<div class="row">
		<div class="col-12 col-md-6 col-xl-3 my-3 my-xs-0">
			<h2>Server</h2>
			<table class="definition">
			<tbody>
				<tr><td>Start time</td><td>${startTime}</td></tr>
				<tr><td>Up time</td><td>${upTime}</td></tr>
				<tr><td>Name</td><td>${serverName}</td></tr>
				<tr><td>Web server</td><td>${serverVersion}</td></tr>
				<tr title="${jvmVersion}"><td>Java version</td><td>${javaVersion}</td></tr>
				<tr><td>Web application</td><td>${appName}</td></tr>
				<tr title="${manifest}"><td colspan="2">Version</td></tr>
				<tr class="sub" title="${gitDescribeShort}"><td>Branch</td><td>${gitBranch}</td></tr>
				<tr class="sub" title="${gitCommitTime} by ${gitCommitUserName}"><td>Commit</td><td class="code">${gitCommit}</td></tr>
				<tr class="sub"><td>Build time</td><td>${buildTime}</td></tr>
				<tr class="sub"><td>Build id</td><td>${buildId}</td></tr>
				<tr class="sub"><td>${buildJdk.left}</td><td>${buildJdk.right}</td></tr>
			</tbody>
			</table>
		</div>
		<div class="col-12 col-md-6 col-xl-3 my-3 my-xs-0">
			<h2>Threads</h2>
			<table class="definition">
			<tbody>
				<tr><td>Processors</td><td>${procNumber}</td></tr>
				<tr><td>Threads</td><td>${threadCount}</td></tr>
				<tr class="sub"><td>Daemon</td><td>${daemon}</td></tr>
				<tr class="sub"><td>Non Daemon</td><td>${nonDaemon}</td></tr>
				<tr><td>Peak</td><td>${peak}</td></tr>
				<tr><td>Started</td><td>${peak}</td></tr>
				<tr><td>Tomcat</td></tr>
				<c:forEach items="${tomcatThreadMetrics}" var="elem">
					<tr class="sub"><td>${elem.poolName}</td></tr> 
					<tr class="sub-2"><td>Total</td><td>${elem.currentThreadCount}</td></tr>
					<tr class="sub-2"><td>Busy</td><td>${elem.currentThreadsBusy}</td></tr>
				</c:forEach>
			</tbody>
			</table>
		</div>
		<div class="col-12 col-md-6 col-xl-3 my-3 my-xs-0">
			<h2>Memory</h2>
			<div class="panel-group" id="accordion">
				<c:set var="name" value="Heap"/>
				<c:set var="escapedName" value="Heap"/>
				<c:set var="defaultVisible" value="show"/>
				<c:set var="total" value="${heap}"/>
				<c:set var="pools" value="${heaps}"/>
				<c:set var="openFirst" value="${true}"/>
				<c:set var="additionalCardClasses" value="rounded-0 border-bottom-0"/>
				<%@ include file="memoryPool.jsp" %>

				<c:set var="name" value="Non-Heap"/>
				<c:set var="escapedName" value="Non-Heap"/>
				<c:set var="defaultVisible" value=""/>
				<c:set var="total" value="${nonHeap}"/>
				<c:set var="pools" value="${nonHeaps}"/>
				<c:set var="openFirst" value="${false}"/>
				<c:set var="additionalCardClasses" value="rounded-0"/>
				<%@ include file="memoryPool.jsp" %>
			</div>
		</div>
		<div class="col-12 col-md-6 col-xl-3 my-3 my-xs-0">
			<h2>Garbage collector</h2>
			<a class="btn btn-block btn-outline-danger btn-sm mb-2" id="loading-example-btn" tabindex="-1">
				Run garbage collector
			</a>
			<c:forEach items="${gcs}" var="gc" varStatus="status">
				<table class="definition ${status.first ? '' : 'mt-3'}">
				<tbody>
					<tr class="h5 bg-light"><td colspan="2">${gc.name}</td></tr>
					<tr><td>Count</td><td>${gc.collectionCount}</td></tr>
					<tr><td>Time</td><td>${gc.collectionTime}</td></tr>
					<tr><td>Memory Pools</td></tr>
					<c:forEach items="${gc.memoryPoolNames}" var="memoryPoolName">
						<tr class="sub"><td colspan="2">${memoryPoolName}</td></tr>
					</c:forEach>
				</tbody>
				</table>
			</c:forEach>
		</div>
	</div>
	<div class="row mt-4">
		<div class="col-12 col-md-6">
			<c:set var="name" value="Detailed libraries"/>
			<c:set var="escapedName" value="Detailed-libraries"/>
			<c:set var="defaultVisible" value="in"/>
			<c:set var="libs" value="${detailedLibraries}"/>
			<c:set var="map" value="${true}"/>
			<c:set var="lined" value="${true}"/>
			<%@ include file="libraries.jsp" %>
		</div>
		<div class="col-12 col-md-6">
			<c:set var="name" value="Other libraries"/>
			<c:set var="escapedName" value="Other-libraries"/>
			<c:set var="defaultVisible" value="in"/>
			<c:set var="libs" value="${otherLibraries}"/>
			<c:set var="map" value="${false}"/>
			<c:set var="lined" value="${false}"/>
			<%@ include file="libraries.jsp" %>
		</div>
	</div>
</div>
</body>
</html>
