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
			<div class="label-above row-fluid">
				<ul class="span6">
					<li><b>RouterName:</b></li>
					<li><input name="routerName" value="${param.routerName}" type="text" /></li>
				</ul>
				<ul class="span5">
					<li><b>NodeName:</b></li>
					<li><input name="nodeName" value="${node.name}" type="text" /></li>
				</ul>
			</div>
			<div class="label-above row-fluid">
 				<ul class="span6">
					<li><b>DatabeanType:</b></li>
					<li>${node.fieldInfo.sampleDatabean['class']}</li>
				</ul>
				<ul class="span5">
					<li><b>NodeType:</b></li>
					<li>${node['class'].simpleName}</li>
				</ul>
			</div>
			
			
			<br/>
				<div class="label-above row-fluid">				
				<li><input name="nodeName" value="dataCsv()" type="text" /></li>
			<button onclick="dataCsv()">Try it</button> 								
			</div>
			
		<br />
		
		 <div>
		   <table>
		     <c:forEach items="${data}" var="item">
		      <tr>
		       <td><input name="nodeName" value="${item}" type="text" /></td>
		         <td><input name="nodeName" value="${item.getNumRows()}" type="text" /></td>		    
		      </tr>
		     </c:forEach>
	    	</table>
    	 </div>
		
			
		<div id="graphdiv" style="width:500px; height:300px;"></div>
		<script type="text/javascript">
				   g = new Dygraph(
				    document.getElementById("graphdiv"),
				    data_showzerovalues,
				{}
			);
				   
				   


				   
	    function data_showzerovalues() {
		return "" +
		"20070101,0,39\n" +
		"20070102,62,0\n" +
		"20070103,0,42\n" +
		"20070104,57,0\n" +
		"20070105,65,44\n" +
		"20070106,55,44\n" +
		"20070107,0,45\n" +
		"20070108,66,0\n" +
		"20070109,0,39\n";
		}
	    
	    
	    
		   
	    function dataCsv() {
		return "" +
		"DateCreated","numRecords"+
		"20070101,0,39\n" +
		"20070102,62,0\n" +
		"20070103,0,42\n" +
		"20070104,57,0\n" +
		"20070105,65,44\n" +
		"20070106,55,44\n" +
		"20070107,0,45\n" +
		"20070108,66,0\n" +
		"20070109,0,39\n";
		}
	    
		</script>
		</div>
		
		</form>		
		
		
		<br />

		
		<br />
	

	<script type="text/javascript">
		require([ "bootstrap/bootstrap" ], function() {});
	</script>

   
</body>
</html>