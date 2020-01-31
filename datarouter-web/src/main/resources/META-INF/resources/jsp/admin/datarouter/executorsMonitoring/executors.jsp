<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Executors</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
	<style>
		.executor-details{
			display: none;
			height: 230px;
		}
		.table-sm td{
			padding: 0.2rem !important;
		}
	</style>
	<script>
		require(['jquery', 'goog!visualization,1,packages:[corechart]'], function(){
			const chartNames = {
				completedTaskCount: {
					title: "Completed Tasks",
					graphs: [
						"completedTaskCount"
					]
				},
				activeCount: {
					title: "Threads",
					graphs: [
						"activeCount",
						"poolSize"
					]
				}, 
				queueSize: {
					title: "Queue Size",
					graphs: [
						"queueSize"
					]
				}
			}
			const chart = {}
			const chartData = {}
			const drawChart = function(key){
				var chartDetails = chartNames[key]
				var data = new google.visualization.DataTable()
				data.addColumn('number', 'X')
				for(const graphName of chartDetails.graphs){
					data.addColumn('number', graphName)
				}
				data.addRows(chartData[key])
				const options = {
					title: chartDetails.title,
					legend: 'none'
				}
				chart[key].draw(data, options)
			}
			var refresh = function(){
				if(!$('#auto-refresh').is(':checked')){
					return;
				}
				$.get('${contextPath}/datarouter/executors/getExecutors', function(executors){
					$(executors).each(function(){
						var row = $('#executor-' + this.name).children();
						if(row.length == 0){
							console.log("cannot find " + this.name);
							return;
						}
						row[1].textContent = this.activeCount;
						row[2].textContent = this.poolSize;
						row[3].textContent = this.maxPoolSize;
						row[4].textContent = this.queueSize;
						row[5].textContent = this.remainingQueueCapacity;
						row[6].textContent = this.completedTaskCount;
						this.queueSize = this.queueSize === "MAX" ? 2147483647 : parseInt(this.queueSize);

						if($('#executor-' + this.name).next().find('div').length > 0){
							for(const key in chartNames){
								const row = []
								row.push(chartData[key].length + 1)
								for(var graph in chartNames[key].graphs){
									row.push(this[chartNames[key].graphs[graph]])
								}
								chartData[key].push(row)
								drawChart(key)
							}
						}
					})
				})
			}

			$(document).ready(function(){
				setInterval(refresh, 1000)
				$('.executor-row').click(function(){
					$('.executor-details').hide()
					$('.executor-details').children().empty()
					let details = $(this).next()
					details.show()
					details = details.children().eq(0)
					details.empty()
					for(const key in chartNames){
						const chartBox = $('<div>').addClass('d-inline-block').css({width: '33%'})
						details.append(chartBox)
						chart[key] = new google.visualization.LineChart(chartBox[0])
						chartData[key] = []
					}
				})
			})
		})
	</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container my-4">
		<h1>Executors</h1>
		<label><input type="checkbox" id="auto-refresh" checked> Auto refresh</label>
		<table class="table table-sm table-bordered">
			<thead>
				<tr>
					<th>Name</th>
					<th>Active threads</th>
					<th>Pool size</th>
					<th>Maximum pool size</th>
					<th>Queue size</th>
					<th>Remaining queue size</th>
					<th>Completed tasks</th>
				</tr>
			</thead>
			<c:forEach items="${executors}" var="executor">
				<tr id="executor-${executor.name}" class="executor-row">
					<td>${executor.name}</td>
					<td>${executor.activeCount}</td>
					<td>${executor.poolSize}</td>
					<td>${executor.maxPoolSize}</td>
					<td>${executor.queueSize}</td>
					<td>${executor.remainingQueueCapacity}</td>
					<td>${executor.completedTaskCount}</td>
				</tr>
				<tr data-name="${executor.name}" class="executor-details"><td colspan="7"></td></tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>
