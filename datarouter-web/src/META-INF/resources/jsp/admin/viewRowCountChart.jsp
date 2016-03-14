<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ include file="../generic/prelude-datarouter.jspf"%>
<html>
<head>
<title>Datarouter</title>
<%@ include file="/jsp/generic/head.jsp" %>
<%@ include file="/jsp/css/css-import.jspf" %>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
 
<script src="https://cdnjs.cloudflare.com/ajax/libs/dygraph/1.1.0/dygraph-combined-dev.js"></script>
   
<style>
</style>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<h2 class="container">Datarouter</h2>
	<div class="wide-container">
		<a href="${contextPath}/datarouter/routers">Datarouter Home</a> &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; <a href="${contextPath}/datarouter/routers?submitAction=inspectRouter&routerName=${param.routerName}">Router: ${param.routerName}</a> &nbsp;&nbsp;&#62;&#62; &nbsp;&nbsp;
		node: <b>${node.name}</b><br /> <br />
		<form method="get" action="?">					
		<br/>
		
		<div id="javaValues" style="display: none;">
    		<div id="jsonData">${jsonData}</div>
		</div>
		</form>		
				
	  	<div id="graphdiv" style="width:1250px; height:350px;"></div>		
		<script type="text/javascript">		 
				   g = new Dygraph(
				    document.getElementById("graphdiv"),
				    get_data,
					{   height : 500,
			            titleHeight : 25,
			            showRangeSelector : true,
			            showRoller : true,
			            connectSeparatedPoints : true,
			            drawPoints : true,
			            drawGapEdgePoints : true,
			            pointSize : 2,
			            fillGraph : true,
			            maxNumberWidth : 10,
			            axisLabelWidth : 70,
			            axisLabelFontSize : 11,
	                    title: 'Table Count chart'
	                }
			);
			
	    
	    function get_data() {	   	
	    	 
	    	 var dataArray = []; 	    
	    	  var jsonData = ${jsonData};
	    	  for(var i =0; i< jsonData.length; i++){	    		 
	    		  var rows = jsonData[i].rows;
	    		  var date = jsonData[i].date;
	    		  dataArray.push([new Date(date),rows]);
	    	  }

		      return dataArray;     
		 }	
	    
	    </script> 
	    
		</div>
		
		<br />
	

	<script type="text/javascript">
		require([ "bootstrap/bootstrap" ], function() {});
	</script>

   
</body>
</html>