<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ include file="../../generic/prelude-datarouter.jspf"%>
<html>
<head>
<%@ include file="/jsp/generic/head.jsp" %>
<%@ include file="/jsp/css/css-import.jspf"%>

<link rel="stylesheet" href="${contextPath}/js/google-code-prettify/prettify.css" rel="stylesheet">
<link rel="stylesheet" href="${contextPath}/css/datatables/TableTools.css">
<link rel="stylesheet" href="${contextPath}/css/datatables/datatables-bootstrap2.css">
<link rel="stylesheet" href="${contextPath}/assets/css/analytics.css">
<link rel="stylesheet" href="${contextPath}/css/other/databeanGenerator.css">
<title>Databean class generator</title>
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
	var fileName = $("#databeanName").html();
	
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
	$("#createScript").val("");
	$.ajax({
		type: 'GET',
		url: '${contextPath}/datarouter/databeanGenerator?submitAction=getDemoScript', 
		success: function(data){
			addDemoScriptCode(data);
		}
	});
}

function generateDataBean(){
	var createScript = $("#createScript").val();
	$.ajax({
		type: 'POST',
		url: '${contextPath}/datarouter/databeanGenerator', 
		data: 'script='+ createScript + '&submitAction=generateJavaCode',
		success: function(data){
			addGeneratedCode(data);
		}
	});
}

function startsWith(str1, str2){
	return str1.substring( 0, str2.length ) === str2;
}

function endsWith(str1, str2){
	return str1.substring( str1.length - str2.length, str1.length ) === str2;
}

	function loadDataBean(){
		var createScript = $("#createScript").val();
		var lines = createScript.split(/\r\n|\r|\n/g);
		
		var isPKField = false;
		
		for(var i=0; i<lines.length;i++){
			var line = lines[i].trim();
			if(line === "") {
				continue;
			}
			var lCaseLine = line.toLowerCase(); 
			if(lCaseLine == "pk{" || lCaseLine == "pk {"){
				isPKField = true;
			} else if( isPKField && line == "}"){
				isPKField = false;
			} else if(endsWith(line, "{")){  
				//package and class name line
				var packageName = '';
				var className = '';
				if(line.indexOf(".") != -1){
					packageName = line.substring(0, line.lastIndexOf("."));
					className = line.substring(line.lastIndexOf(".") + 1, line.lastIndexOf("{"));
				} else {
					className = line.substring(0, line.lastIndexOf("{"));
				}
				$("input[name='databeanPackage']").val(packageName);
				$("input[name='databeanName']").val(className);
			} else if(isPKField){ 
				//pk field line
				var tableId='keyFieldsTable';
				line = line.replace(",", "");
				var fieldName = line.split(" ")[1];
				var fieldType = line.split(" ")[0];
				var fieldGenericType = '';
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
				
			} else if (startsWith(line, "index(") || startsWith(line, "index (")){
				//index line
			} else if(line == "}"){
				//last line of the script;
				break;
			} else { 
				//non pk field line
				var tableId ='fieldsTable';
				line = line.replace(",", "");
				var fieldName = line.split(" ")[1];;
				var fieldType = line.split(" ")[0];;
				var fieldGenericType = '';
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
	}
	
	function loadDataBeanFromJson(){
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
		 $("#createScript").val(code);
		 $("#createScript").focus();
		}

	
	function addGeneratedCode(code){
		if(code.indexOf("~~##~~")!=-1){
			$("#databeanName").html(code.split("~~##~~")[0]);
			 code = code.split("~~##~~")[1];		
		}
		 
	 if(code.indexOf("/****************************************************/")!=-1){
		 $("#generatedCode1").val(code.split("/****************************************************/")[0]);
		 $("#generatedCode2").val(code.split("/****************************************************/")[1]);	 
	 } else {
		 $("#generatedCode1").val(code);
		 $("#generatedCode2").val(code);
	 }
	 
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
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<h2>Databean class generator</h2>
		<div class="divButton" onclick="location.reload();">CLEAR</div>
		<div class="divButton" id="getDemoScript" onclick="getDemoScript();">Get DEMO script</div>
		<fieldset style="margin:0 0; padding:10px;">
					<legend style="color:#317eac; font-size:20px;">Databean Script</legend>
					<div>
						<textarea id="" class="fieldTypes" spellcheck=false readonly="readonly">${fieldTypesAsString}</textarea>
						<textarea id="createScript" class="createScriptCode" spellcheck=false>com.hotpads.demo.ClassName{
    PK{
        StringField stringFieldDemoKey
    }
        DateField dateFieldDemo,
        LongField longFieldDemo,
        index(dateFieldDemo), 
        index(stringFieldDemoKey, longFieldDemo) 
}
</textarea>
					</div>
					<div class = divButton id="loadDataBean" onclick="loadDataBean();">Load in Databean Builder</div>
					<div class = divButton id="generateDataBean" onclick="generateDataBean();">Generate</div>
		</fieldset>
		<form id="dataBeanForm" method="POST" action="${contextPath}/datarouter/databeanGenerator">
			<fieldset> 
				<legend style="color:#317eac; font-size:20px;">Databean Builder</legend>
				<label>Java package:<input type="text" name="databeanPackage" value="${param.feed}"  required></label>
				<label>Class name: <input type="text" name="databeanName" value="${param.feed}"  required></label>
				<table style="width:100%">
				<tr>
				<td style="width:50%; vertical-align:top">
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
				</td>
				<td style="width:50%; vertical-align:top">
				
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
	
				</td>
				</tr>
				</table>
				
	
				<div style="width:115px;margin: 0 auto; padding-top: 20px;padding-bottom:10px;">
					<input type="submit" value="Generate">
				</div>
			</fieldset>
			<input type="hidden" value="generateJavaCode"  name="submitAction" hidden="true">
		</form>
	</div>
	<div class="container generatedCode1">
		<h4>Java code for databean</h4>
		<div class="divButton" onclick="saveAs('#generatedCode1');">Save to File</div>
		<div style="display: none" id="databeanName"></div>
		<textarea id="generatedCode1" class="javaCode" spellcheck=false>
			
		</textarea>
	</div>
	
	<div class="container generatedCode2">
		<h4>Java code for databean key</h4>
		<div class="divButton" onclick="saveAs('#generatedCode2');">Save to File</div>
		<textarea id="generatedCode2" class="javaCode" spellcheck=false>
			
		</textarea>
	</div>
	
</body>
</html>