<%@ include file="/jsp/generic/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>Webapp Instance Log</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
	<script>
		require(['sorttable'])
		function buildTooltipHtml(commitId, buildId, startupDate, buildDate){
			return '<div><div class="card-header">Build: <b>' + buildId + '</b> / Commit: <b>' + commitId + '</b></div>'
				+ '<div class="card-body" style="white-space: nowrap">'
				+ '<table>'
				+ '<tr><td>startup</td><td><b>' + startupDate.toLocaleString() + '</b></td></tr>'
				+ '<tr><td>built</td><td><b>' + buildDate.toLocaleString() + '</b></td></tr>'
				+ '</table></div></div>'
		}

		const ROWS = [
			<c:forEach items="${logs}" var="webapp">
			[
				'ID',
				'${webapp.buildId}' || '${webapp.commitId}',
				buildTooltipHtml('${webapp.commitId}', '${webapp.buildId}', new Date('${webapp.startupDate}'), new Date('${webapp.buildDate}')),
				new Date('${webapp.startupDate}'),
				new Date('${webapp.refreshedLast}')
			],
			</c:forEach>
		]

		function dataTableStartingAfter(startDate){
			const rows = ROWS.filter(row => row[3] > startDate)
			const data = new google.visualization.DataTable()
			data.addColumn('string', 'Role')
			data.addColumn('string', 'Name')
			data.addColumn({type: 'string', role: 'tooltip', p: {html: true}})
			data.addColumn('date', 'Start')
			data.addColumn('date', 'End')
			data.addRows(rows)
			return data
		}

		require(['jquery', 'goog!timeline'], function(){
			let showAll = false
			const chart = new google.visualization.Timeline(document.getElementById('timeline'))
			const drawGraph = () => {
				const filterStart = showAll ? new Date(0) : new Date(Date.now() - 7 * 1000 * 60 * 60 * 24)
				chart.draw(dataTableStartingAfter(filterStart), {
					timeline:{ showRowLabels: false, avoidOverlappingGridLines: false }
				})
			}
			drawGraph()
			$('#timeline-toggle').click(function(){
				$(this).blur()
				showAll = !showAll
				this.innerText = showAll ? 'show past 7 days' : 'show more'
				drawGraph()
			})
		})
	</script>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
<div class="container-fluid my-4">
	<h2 class="page-header"><small class="text-muted">webapp instance log - </small>${param.webappName}/${param.serverName}</h2>
	<div class="mt-2">
		<div class="text-right">
			<a id="timeline-toggle" class="btn-link" tabindex="0">show all</a>
		</div>
		<div id="timeline" style="height: 100px"></div>
	</div>
	<div class="page-content-container page-content-thicktop page-single-column">
		<table class="sortable table table-bordered table-sm table-striped" style="border-collapse:collapse;">
			<thead>
				<tr>
					<th>Build Id</th>
					<th>Commit Id</th>
					<th>Private IP</th>
					<th>Startup Date</th>
					<th>Build Date</th>
					<th>Java Version</th>
					<th>Servlet Container</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${logs}" var="webapp">
					<tr <c:if test="${webapp.latest}">class="info"</c:if>>
						<td>${webapp.buildId}</td>
						<td>${webapp.commitId}</td>
						<td>${webapp.serverPrivateIp}</td>
						<td sorttable_customkey="${webapp.startupDate.time}">${webapp.startupDate}</td>
						<td sorttable_customkey="${webapp.buildDate.time}">${webapp.buildDate}</td>
						<td>${webapp.javaVersion}</td>
						<td>${webapp.servletContainerVersion}</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		</br>
		<table class="table table-bordered table-condensed">
			<tr><th>Color Codes</th></tr>
			<tr class="info"><td>WebappInstance is within 1 day</td></tr>
		</table>
	</div>
</body>
</html>