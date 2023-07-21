require(['jquery', 'goog!corechart'], function(){
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
	const refresh = function(){
		if(!$('#auto-refresh').is(':checked')){
			return;
		}
		$.get(`${window.contextPath}/datarouter/executors/getExecutors`, function(executors){
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
			});

			setTimeout(refresh, 1000);
		});
	}

	let filterName = (function(){
		const urlParams = new URLSearchParams(window.location.search);
		const value = urlParams.get('filterName') || "";
		$("#executor-filter").val(value);
		return value;
	})();

	function filterRowsByName(){
		if (filterName === "") {
			$('.executor-row').show();
			return;
		}
		$('.executor-row').each(function() {
			const $this = $(this);
			const executorName = $this.find('.executor-name').text();
			executorName.toLowerCase().includes(filterName.toLowerCase()) ? $this.show() : $this.hide();
		});
	}

	function emptyDetails(){
		$('.executor-details').hide()
		$('.executor-details').children().empty()
	}

	$(document).ready(function(){
		setTimeout(refresh, 1000)
		$('.executor-row').click(function(){
			let details = $(this).next();
			if (details.is(':visible')){
				emptyDetails();
				return;
			}
			emptyDetails();
			details.show()
			details = details.children().eq(0)
			details.empty()
			for(const key in chartNames){
				const chartBox = $('<div>').addClass('d-inline-block').css({width: '33%'})
				details.append(chartBox)
				chart[key] = new google.visualization.LineChart(chartBox[0])
				chartData[key] = []
			}
		});

		$('#executor-filter').keyup(function(e) {
			filterName = e.target.value;
			filterRowsByName();

			history.replaceState(null, null, "?filterName=" + encodeURIComponent(filterName));
		});

		filterRowsByName();

		$("#executor-table").fadeIn();
	})
})
