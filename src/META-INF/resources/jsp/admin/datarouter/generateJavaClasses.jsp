<%@ include file="/WEB-INF/prelude.jspf"%>
<html>
<head>
<%@ include file="/WEB-INF/jsp/generic/head.jsp"%>
<link rel="stylesheet" href="${contextPath}/assets/css/hackweek6.css" />
<title>Home</title>
<script type="text/javascript" data-main="${contextPath}/js/core-analytics" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
var numKeyFields = 0;
var numFields = 0;
var fieldTypesHtml = '${fieldTypes}';
$(document).ready(function(){
	
	$(document).on('click', '.removeRow', function(){
		$(this).parent().remove();
	});
	
	$(".addRow").click(function(){
		var id = $(this).parent().parent().attr("id");
		addRow(id);
		
		
	});
	
	$("#dataBeanForm").submit( function (ev){
		addGeneratedCode('success');
		$.ajax({
			type: $(this).attr('method'),
			url: $(this).attr('action'), 
			data: $(this).serialize(),
			success: function(data){
				addGeneratedCode(data);
			}
		});
		ev.preventDefault();
	});
	
});

function saveAs(id){
	var fileName = "abc";
	fileName = $("input[name='databeanName']").val();
	if(id==='#generatedCode2'){
		fileName += 'Key'
	}
	fileName += '.java';
	download1(fileName, $(id).val());
}

function download(fileName, content){
	alert(fileName);
	alert(content);
    var downloadble = document.createElement('a');
    	downloadble.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(content));
    	downloadble.setAttribute('download', fileName);
    	downloadble.click();
}

function download1(filename, content, contentType)
{
    if(!contentType) contentType = 'application/octet-stream';
        var a = document.createElement('a');
        var blob = new Blob([content], {'type':contentType});
        a.href = window.URL.createObjectURL(blob);
        a.download = filename;
        a.click();
}



function getDemoScript(){
	$.ajax({
		type: 'GET',
		url: '${contextPath}/datarouter/hackweek6?submitAction=getDemoScript', 
		success: function(data){
			addDemoScriptCode(data);
		}
	});
}

	function loadDataBean(){
		var jsonString = $("#createScript").val();
		var databean = $.parseJSON(jsonString);		
		$("input[name='databeanPackage']").val(databean.package);
		$("input[name='databeanName']").val(databean.class);
		
		var htmlRows = "";
		var pk = databean.pk;
		var tableId='keyFieldsTable';
		for(var i = 0; i< pk.length; i++){
			
			var fieldName = pk[i].name;
			var fieldType = pk[i].type;
			var fieldGenericType = pk[i].genericType;
			var inputNamePrefix = 'field_';
			var inputNamePostFix = '_' + numFields;
			if(tableId==='keyFieldsTable'){
				inputNamePrefix = "keyField_";
				inputNamePostFix = '_' + numKeyFields;
				numKeyFields ++;
			} else {
				numFields ++;
			}
			var rowId = inputNamePrefix + "_" + inputNamePostFix;
			var rowHtml = '<tr id="'+rowId+'"><td>';
			rowHtml += '<select name="'+inputNamePrefix+'type'+inputNamePostFix+'">'+fieldTypesHtml+'</select>';
			rowHtml += '</td>';
			rowHtml += '<td>';
			rowHtml += '<input type="text" name="'+inputNamePrefix+'name'+inputNamePostFix+'" value="'+fieldName+'" required/>';
			rowHtml += '</td>';
			rowHtml += '<td>';
			rowHtml += '<input type="text" name="'+inputNamePrefix+'enumType'+inputNamePostFix+'" value="'+fieldGenericType+'"/>';
			rowHtml += '</td>';
			rowHtml += '<td class="removeRow">';
			rowHtml += '<div style="width:30px; margin:0 auto; padding:5px"><div id="removeField" class="removeField"></div></div>';
			rowHtml += '</td>';
			rowHtml += '</tr>';
			
			$('#' + tableId+' tbody:last').append(rowHtml);
			$('#' + rowId + ' select').val(fieldType);
		}
		$('#keyFieldsTable tbody:last').append(htmlRows);
		tableId ='fieldsTable';
		var fields = databean.fields;
		htmlRows = "";
		for(var i = 0; i< fields.length; i++){
			
			var fieldName = fields[i].name;
			var fieldType = fields[i].type;
			var fieldGenericType = fields[i].genericType;
			var inputNamePrefix = 'field_';
			var inputNamePostFix = '_' + numFields;
			if(tableId==='keyFieldsTable'){
				inputNamePrefix = "keyField_";
				inputNamePostFix = '_' + numKeyFields;
				numKeyFields ++;
			} else {
				numFields ++;
			}
			var rowId = inputNamePrefix + "_" + inputNamePostFix;
			var rowHtml = '<tr id="'+rowId+'"><td>';
			rowHtml += '<select name="'+inputNamePrefix+'type'+inputNamePostFix+'">'+fieldTypesHtml+'</select>';
			rowHtml += '</td>';
			rowHtml += '<td>';
			rowHtml += '<input type="text" name="'+inputNamePrefix+'name'+inputNamePostFix+'" value="'+fieldName+'" required/>';
			rowHtml += '</td>';
			rowHtml += '<td>';
			rowHtml += '<input type="text" name="'+inputNamePrefix+'enumType'+inputNamePostFix+'" value="'+fieldGenericType+'"/>';
			rowHtml += '</td>';
			rowHtml += '<td class="removeRow">';
			rowHtml += '<div style="width:30px; margin:0 auto; padding:5px"><div id="removeField" class="removeField"></div></div>';
			rowHtml += '</td>';
			rowHtml += '</tr>';
			
			$('#' + tableId+' tbody:last').append(rowHtml);
			$('#' + rowId + ' select').val(fieldType);
		}
		
	}

	function addDemoScriptCode(code){
		 $("#createScript").html(code);
		 $("#createScript").focus();
		}

	
	function addGeneratedCode(code){
	 $("#generatedCode1").html(code.split("/****************************************************/")[0]);
	 $("#generatedCode2").html(code.split("/****************************************************/")[1]);
	 $("#generatedCode1").focus();
	}

	function addRow(tableId){
		
		var inputNamePrefix = 'field_';
		var inputNamePostFix = '_' + numFields;
		if(tableId==='keyFieldsTable'){
			inputNamePrefix = "keyField_";
			inputNamePostFix = '_' + numKeyFields;
			numKeyFields ++;
		} else {
			numFields ++;
		}
		var rowId = inputNamePrefix + "_" + inputNamePostFix;
		var rowHtml = '<tr id="'+rowId+'"><td>';
		rowHtml += '<select name="'+inputNamePrefix+'type'+inputNamePostFix+'">${fieldTypes}</select>';
		rowHtml += '</td>';
		rowHtml += '<td>';
		rowHtml += '<input type="text" name="'+inputNamePrefix+'name'+inputNamePostFix+'" value="" required/>';
		rowHtml += '</td>';
		rowHtml += '<td>';
		rowHtml += '<input type="text" name="'+inputNamePrefix+'enumType'+inputNamePostFix+'" value=""/>';
		rowHtml += '</td>';
		rowHtml += '<td class="removeRow">';
		rowHtml += '<div style="width:30px; margin:0 auto; padding:5px"><div id="removeField" class="removeField"></div></div>';
		rowHtml += '</td>';
		rowHtml += '</tr>'; 
		$('#' + tableId+' tbody:last').append(rowHtml);
		$('#' + rowId + ' select').focus();
	}
</script>
<base href="${contextPath}" />
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<h2>Databean class generator</h2>
		<form id="dataBeanForm" method="POST" action="${contextPath}/datarouter/hackweek6">
			<fieldset> 
				<legend>Databean definition</legend>
				<div onclick="location.reload();">CLEAR</div>
				<fieldset style="margin:0 0; padding:10px;">
					<legend>Create script</legend>
					<div id="getDemoScript" onclick="getDemoScript();">Get DEMO script</div>
					<textarea id="createScript" class="jsonCode" spellcheck=false></textarea>
					<div id="loadDataBean" onclick="loadDataBean();">LOAD</div>
				</fieldset>
				<label>Java package:<input type="text" name="databeanPackage" value="${param.feed}"  required></label>
				<label>Class name: <input type="text" name="databeanName" value="${param.feed}"  required></label>
				<fieldset style="margin:0 0; padding:10px;">
						<legend><small>Key Fields</small></legend>
						
						<table class="table table-striped table-bordered table-hover" id="keyFieldsTable">
							<thead>
								<tr>
									<th>Field Type</th>
									<th>Field Name</th>
									<th>Generic type(for Enum fields)</th>
									<th></th>
								</tr>
							</thead>
							<tfoot>
								<tr class="addRow">	
									<td colspan="4">
										<div style="width:30px; margin:0 auto; padding:5px">
											<div id="addField" class="addField"></div>
										</div>
									</td>
								</tr>
							</tfoot>
							<tbody>
							</tbody>
						</table>
				</fieldset>
				<fieldset style="margin:0 0; padding:10px;">
						<legend><small>Fields</small></legend>
						<table class="table table-striped table-bordered table-hover" id="fieldsTable">
							<thead>
								<tr>
									<th>Field Type</th>
									<th>Field Name</th>
									<th>Generic type(for Enum fields)</th>
									<th></th>
								</tr>
							</thead>
							<tfoot>
								<tr class="addRow">	
									<td colspan="4">
										<div style="width:30px; margin:0 auto; padding:5px">
											<div id="addField" class="addField"></div>
										</div>
									</td>
								</tr>
							</tfoot>
							<tbody>
							</tbody>
						</table>
				</fieldset>
				<div style="width:115px;margin: 0 auto; padding-top: 20px;padding-bottom:10px;">
					<input type="submit" value="Generate">
				</div>
			</fieldset>
			<input type="hidden" value="generateJavaCode"  name="submitAction" hidden="true">
		</form>
	</div>
	<div class="container generatedCode1">
		<h4>Java code for databean</h4>
		<div onclick="saveAs('#generatedCode1');">Save to File</div>
		<textarea id="generatedCode1" class="javaCode" spellcheck=false>
			
		</textarea>
	</div>
	
	<div class="container generatedCode2">
		<h4>Java code for databean key</h4>
		<div onclick="saveAs('#generatedCode2');">Save to File</div>
		<textarea id="generatedCode2" class="javaCode" spellcheck=false>
			
		</textarea>
	</div>
	
</body>
</html>