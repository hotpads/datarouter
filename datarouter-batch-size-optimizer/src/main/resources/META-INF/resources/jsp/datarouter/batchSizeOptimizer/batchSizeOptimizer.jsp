<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Batch size optimizer</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
	<script>
		var generateId = function(opName){
			return 'batchSize-' + btoa(opName).replace(/\W+/g, "");
		}

		var refresh = function(){
			$.get('${contextPath}/datarouter/batchSizeOptimizer/getOptimalBatchSizes', function(batchSizes){
				$(batchSizes).each(function(){
					if(!document.getElementById(generateId(this.opName))){
						var row = $('<tr>');
						row.attr('id', generateId(this.opName));
						row.append($('<td>'))
						row.append($('<td>'))
						row.append($('<td>'))
						$('#batchSizesTable').append(row)
					}
					var row = $('#' + generateId(this.opName)).children();
					row[0].textContent = this.opName;
					row[1].textContent = this.batchSize;
					row[2].textContent = this.curiosity.toFixed(2);
				});
			});
		};

		require(['jquery'], function($){
			$(document).ready(function(){
				refresh();
				setInterval(refresh, 10000);
			});
		});
	</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container my-4">
		<h1>Optimal batch sizes</h1>
		<table class="table table-sm table-bordered" id="batchSizesTable">
			<tr>
				<th>Operation name</th>
				<th>Optimal Batch Size</th>
				<th>Curiosity</th>
			</tr>
		</table>
	</div>
</body>
</html>