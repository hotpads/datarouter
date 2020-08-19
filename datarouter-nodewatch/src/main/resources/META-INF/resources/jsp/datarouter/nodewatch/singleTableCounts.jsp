<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>TableSizeLister</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<style>
		tbody tr td:nth-child(2),
		tbody tr td:nth-child(3){
			text-align: right;
		}
	</style>
	<script>
		const DATE_ROWS_DATA = ${jsonData}.map(({date, rows}) => [new Date(date), rows])
		let DYGRAPH = null

		function addDataAndUpdateGraph(date, rows){
			DATE_ROWS_DATA.push([date, rows])
			DYGRAPH.updateOptions({file: DATE_ROWS_DATA})
		}

		function createTd(innerText, attributes = {}){
			const td = document.createElement('td')
			td.innerText = innerText
			Object.keys(attributes).forEach(key => td.setAttribute(key, attributes[key]))
			return td
		}

		function recount(){
			fetch('?submitAction=recount&clientName=${clientName}&tableName=${tableName}')
				.then(response => response.json())
				.then(json => {
					console.log(json)
					document.querySelectorAll('tr').forEach(tr => {
						tr.classList.remove('table-success')
					})
					const tr = document.createElement('tr')
					tr.classList.add('table-success')
					tr.appendChild(createTd(json.numSpans))
					tr.appendChild(createTd(Intl.NumberFormat().format(json.numRows)))
					tr.appendChild(createTd(json.countTime, {sorttable_customkey: json.countTimeMs}))
					tr.appendChild(createTd(json.dateCreated))
					const tbody = document.querySelector('table tbody')
					tbody.insertBefore(tr, tbody.firstChild)
					addDataAndUpdateGraph(new Date(json.dateCreatedTime), json.numRows)
				})
				.catch(err => alert("Error recounting: " + err))
		}
	</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
	<div class="container my-4">
		<h2>${clientName}.${tableName}</h2>
	</div>
	<div class="container-fluid d-flex justify-content-center">

		<div id="graphdiv" style="max-width: 1250px; width: 100%; height: 350px;"></div>

		<script type="text/javascript">
			require(['dygraph'], function(){
				DYGRAPH = new Dygraph(document.getElementById("graphdiv"), DATE_ROWS_DATA, {
					drawPoints: true,
					drawGapEdgePoints: true,
					pointSize: 2,
					fillGraph: true,
					maxNumberWidth: 10,
					axisLabelWidth: 70,
					axisLabelFontSize: 11,
					axes:{
						y: {
							axisLabelWidth: 40,
							axisLabelFormatter(count){
								this.labelY1Memo = this.labelY1Memo || {}
								if(this.labelY1Memo[count]){
									return this.labelY1Memo[count]
								}
								// 1..10..100..1k..10k..100k..1m..10m..100m..1b..10b..100b..1t..10t..100t
								if(count < 1000){
									return count;
								}
								const suffixes = ['', 'k', 'm', 'b', 't']
								const strCount = String(count)
								const power = strCount.replace(/\..+/, '').length - 1
								const powerFloor3 = Math.floor(power / 3)
								const beforeZeros = count.toFixed(1) / (10 ** (3 * powerFloor3))
								const label = beforeZeros + suffixes[powerFloor3]
								this.labelY1Memo[count] = label
								return label
							}
						}
					}
				})
			})
		</script>
	</div>
	<div class="container">
		<table class="sortable table table-striped table-bordered table-hover table-sm">
			<caption style="caption-side: top">
				Table order by decreasing date
				<div class="float-right">
					<a class="btn btn-secondary btn-sm" tabindex="0" onclick="return recount()">Count now (Not Persisted)</a>
					<a class="btn btn-warning btn-sm ml-1" href="?submitAction=resample&clientName=${clientName}&tableName=${tableName}" onclick="return confirm('Resample now?')">Resample</a>
				</div>
			</caption>
			<thead>
				<tr class="table-primary">
					<th>sample count</th>
					<th>row count</th>
					<th>count time</th>
					<th>date</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${results}" var="result">
					<tr>
						<td>${result.numSpans}</td>
						<td><fmt:formatNumber pattern="#,##0" value="${result.numRows}"/></td>
						<td sorttable_customkey="${result.countTimeMs}">${result.countTime}</td>
						<td>${result.dateCreated}</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</body>
</html>
