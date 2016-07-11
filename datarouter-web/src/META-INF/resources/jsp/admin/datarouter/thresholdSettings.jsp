<%@ include file="../../generic/prelude-datarouter.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Threshold</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<h2>Set Threshold</h2>		

		
		<form id="validationform" name="validationform" method="GET" action="?">
		<table id="myTable2" class="order-list" class="table table-striped table-bordered table-hover table-condensed">
				<thead>
					<tr>
						<td>Router name</td>
						<td>Node name</td>
						<td>Threshold Value</td>
					</tr>
				</thead>
				<tbody>
				<c:forEach items="${thresholdSettings}" var="setting">
					<tr>
						<td><input type="text" name="clientName" value='${setting.key.getClientName()}' readonly></td>
						<td><input type="text" name="tableName" value='${setting.key.getTableName()}' readonly></td>
						<td><input type="text" name="threshold" value='${setting.getMaxRows()}' readonly></td>					
					</tr>
				</c:forEach>
				</tbody>	
			</table>
			<input type="hidden" value="saveThresholds" name="submitAction" hidden="true">
		</form>
				
		<br>
		<button onclick="addThreshold()" >Add Threshold </button>
		
		
		<form id="validationform2" name="validationform2" method="GET" action="?">
		<table  id="myTable" class="order-list" class="table table-striped table-bordered table-hover table-condensed" style="visibility:hidden">
				<thead>
					<tr>
						<td>Router name</td>
						<td>Node name</td>
						<td>Threshold Value</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td><input type="text" name="clientName"></td>
						<td><input type="text" name="tableName" ></td>
						<td><input type="text" name="threshold"></td>					
					</tr>
				</tbody>
				
				<tfoot>
				<tr>
					<td><input type="button" value="Add More(+)" name="addrow"
						id="addrow"></td>					
				</tr>
				<tr>
					<td><input type="submit" value="save" />					
				</tr>
			</tfoot>
			</table>
			<input value="saveThresholds" name="submitAction" hidden="true">
		</form>	
	</div>
	
	<script>
		require(['jquery'], function(){
	 		$("#addrow").click(function(){
		      var newRow = $("<tr>");
              var cols = "";
              cols += '<td><input type="text" name="clientName" required/></td>';
              cols += '<td><input type="text" name="tableName" required/></td>';
              cols += '<td><input type="text" name="threshold" required /></td>';
              cols += '<td><a class="deleteRow"> x </a></td>';	           
              newRow.append($(cols));
		      $("#myTable").append(newRow);
		    });
		});

		function addThreshold() {
		    document.getElementById("myTable").style="visibility:visible"
		}
	</script>
	
</body>
</html>