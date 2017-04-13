<%@ include file="/WEB-INF/prelude.jspf"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<title>Edit Cluster Settings | Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script type="text/javascript" src="${contextPath}/js/setting/createSettings.js"></script>
	<script type="text/javascript">
	require(['settings-tools','jquery.validate'], function(settingsTools) {
		var clusterSettingGlobal = {counter: 0};
		
		function addRow(setting) {
			var settingName = "new-setting-" + clusterSettingGlobal["counter"];
			var rowHTML = $("<tr class='" + settingName + "'><td class='setting-name'>" + setting["name"] +
				"</td><td class='setting-scope'>" + setting["scope"] + "</td><td class='setting-type'>"	+
				setting["serverType"] + "</td><td class='setting-serverName'>" + setting["serverName"] +
				"</td><td class='setting-application'>"	+ setting["application"]+ "</td><td>" +
				"<form class='update-form'><input name='value' class='input-medium setting-value' value='" +
				setting["value"] +
				"'></form></td><td class='center'><button class='btn btn-mini btn-danger delete-setting-btn'" +
				" type='button'>delete</button></td><td class='center'><form class='update-form'><button class=" +
				"'btn btn-mini btn-warning'>update</button></form></td></tr>");
			
			$('.settings-table tr:first').after(rowHTML);
			
			$('.'+settingName).effect("highlight", {color:"#ACE671"}, 2500);
			clusterSettingGlobal["counter"]++;
		}
		
		function removeRow(row, name){
			row.remove();
		}
		
		function createSetting(){
			 settingsTools.createSetting(addRow);
		}
	
		$(document).ready(function(){
			$('#form .scope').change(function() {
				settingsTools.updateFields();
			});
			$("#form").validate(settingsTools.getValidatorOptionsWithHandler(createSetting));
			$('#form').on('submit', function(event){
				event.preventDefault();
			});
			
			//these are delegated so they don't need to be bound to each newly added row
	 		$('table').on('click', '.delete-setting-btn', function(){
	 			settingsTools.deleteSetting(this, removeRow);
	 		});
	 		$('table').on('submit', '.update-form', function(event){
	 			event.preventDefault();
	 			settingsTools.editSetting(this);
	 		});
		});
	});
	</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<a href="?submitAction=browseSettings" class="btn btn-primary">&rarr; Browse settings</a>
		<br>
		<h2 class="page-header">Create Cluster Settings</h2>
		
		<form id="form" class="well">
			<div class="form-group row">
				<label for="name" class="col-form-label col-sm-2">Name</label>
				<input type="text" name="name" value="" class="name input-medium" placeholder="Name">
			</div>
			<div class="form-group row">
				<label for="scope" class="col-form-label col-sm-2">Setting Scope</label>
				<select	name="scope" class="scope setting-type input-small">
					<option value="" disabled selected>Scope</option>
					<option value="defaultScope">Default</option>
					<option value="cluster">Cluster</option>
					<option value="serverType">Server Type</option>
					<option value="serverName">Server Name</option>
					<option value="application">Application</option>
				</select>
			</div>
			<div class="form-group row">
				<label for="serverType" class="col-form-label col-sm-2">Server Type</label>
				<select	name="serverType" class="server-type setting-type input-small disableable" disabled>
					<option bootvalue="" disabled selected>Type</option>
					<c:forEach items="${serverTypeOptions}" var="serverTypeOption">
						<c:choose>
							<c:when test="${serverTypeOption.value.equals(\"all\") || serverTypeOption.value.equals(\"unknown\")}">
								<option value="${serverTypeOption.value}" disabled>${serverTypeOption.name}</option>
							</c:when>
							<c:otherwise>
								<option value="${serverTypeOption.value}">${serverTypeOption.name}</option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</select>
			</div>
			<div class="form-group row">
				<label for="serverName" class="col-form-label col-sm-2">Server Name</label>
				<input type="text" name="serverName" class="server-name input-small disableable" placeholder="Server Name" value="" disabled>
			</div>
			<div class="form-group row">
				<label for="application" class="col-form-label col-sm-2">Application</label>
				<input type="text" name="application" class="application input-small disableable" placeholder="Application" value="" disabled>
			</div>
			<div class="form-group row">
				<label for="value" class="col-form-label col-sm-2">Value</label>
				<input type="text" name="value" class="value input-mini" placeholder="Value" value="">
			</div>
			<div class="form-group row">
				<label for="" class="col-form-label col-sm-2"></label>
				<button id="create" class="btn btn-mini btn-success">create</button>
			</div>
		</form>
		
		<h2 class="page-header">Search Cluster Settings</h2>
		<form class="well form-search" method="get" action="?">
			<input type="text" class="input-medium search-query" name="prefix" placeholder="Name Prefix" autofocus>
			<button type="submit" class="btn btn-mini btn-primary">Search</button>
		</form>
		<table class="table table-bordered table-condensed settings-table">
			<tr style="background: black; color: white;">
				<th>Name</th>
				<th>Scope</th>
				<th>ServerType</th>
				<th>Server Name</th>
				<th>Application</th>
				<th>Value</th>
				<th>Delete</th>
				<th>Update</th>
			</tr>
			<c:forEach items="${settings}" var="setting">
					<tr>
						<td class="setting-name">${setting.name}</td>
						<td class="setting-scope">${setting.scope}</td>
						<td class="setting-type">${setting.serverType}</td>
						<td class="setting-serverName">${setting.serverName}</td>
						<td class="setting-application">${setting.application}</td>
						<td>
							<form class="update-form">
								<input name="value" class="setting-value input-medium" value="${setting.value}">
							</form>
						</td>
						<td class="center" >
							<button class="btn btn-mini btn-danger delete-setting-btn" type="button">
								delete
							</button>
						</td>
						<td class="center">
							<form class="update-form">
								<button class="btn btn-mini btn-warning">
									update
								</button>
							</form>
						</td>
					</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>