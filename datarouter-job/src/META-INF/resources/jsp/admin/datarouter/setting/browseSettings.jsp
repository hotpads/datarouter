<%@ include file="/WEB-INF/prelude.jspf"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<title>Browse Cluster Settings | Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<link rel="stylesheet" href="//code.jquery.com/ui/1.12.0/themes/base/jquery-ui.css">
	<script type="text/javascript" src="${contextPath}/js/setting/createSettings.js"></script>
	<script type="text/javascript">
		require(['settings-tools','jquery.validate'], function(settingsTools){
			function addRow(setting){
				var name = setting["name"];
				var rowHTML = $("<tr><input type='hidden' name='name' class='setting-name' value='" + name +
					"'><td class='setting-scope'>" + setting["scope"] + "</td><td class='setting-type'>" +
					setting["serverType"] + "</td><td class='setting-serverName'>" + setting["serverName"] +
					"</td><td class='setting-application'>" + setting["application"] +
					"</td><td><form class='update-form'><input value='"	+ setting["value"] +
					"'class='input-mini setting-value'></form></td><td class='center'><form class='update-form'><button id='upda_"
					+ name + "'class='btn btn-mini btn-warning'>update</button><button id='dele_"
					+ name +
					"'class='btn btn-mini btn-danger delete-setting-btn' type='button'>delete</button></form></td>" +
					"<td></td></tr>");
				
				//determine proper position for new row
				var escapedName = settingsTools.escape(name);
				var rowsToSkip = $('#'+escapedName).attr('rowspan') - 1;
				var rowToAppendTo = $('#rowc_'+escapedName);
				while (rowsToSkip > 0) {
					rowsToSkip--;
					rowToAppendTo = rowToAppendTo.next();
				}
				rowToAppendTo.after(rowHTML);
				
				//increase the height of the parent row's name column
				$('#'+escapedName).attr("rowspan", function(i, rs){
						return parseInt(rs) + 1;
				});
			}
			
			function addGroupRow(groupName, scopeUnique, scopeDisplay){
				var groupBody = $('.group-name[value="' + groupName + '"]').closest('tbody');
				var existingInput = groupBody.find('tr input[value="' + scopeUnique + '"]')
				if(existingInput.length) {
					alert('The specified scope and group already have settings. Edit the row if you wish to overwrite them.');
					existingInput.closest('tr').effect("highlight", {color:"#ACE671"}, 2500).focus();
					return;
				}
				
				var beginTr = '<tr tabindex="-1"><input type="hidden" class="group-scope" value="' + scopeUnique + '"><td>' + scopeDisplay + ':</td>';
				
				var tds = [];
				var names = groupBody.find('tr:nth-child(2) td').map(function(){return $(this).text()}).get().slice(1, -1);
				var values = groupBody.find('tr:nth-child(3) td').map(function(){return $(this).text()}).get().slice(1, -1);
				for (var i = 0; i < names.length; i++){
					tds.push('<td class="setting"><form class="update-group"><input type="hidden" class="setting-name" value="'
							+ groupName + names[i] + '"><input class="setting-value" value="" placeholder="Current: '
							+ values[i] + '"></form></td>');
				}
				
				var endTr = '<td><button type="button" class="btn btn-mini btn-danger delete-group-btn">delete</button></td></tr>';
				
				wholeTr = beginTr + tds.join('') + endTr;
				
				groupBody.children('tr:last').after(wholeTr);
				groupBody.children('tr:last').effect("highlight", {color:"#ACE671"}, 2500).focus();
			}
			
			function removeRow(row, name, skipRowspan){
				row.remove();
				if (!skipRowspan){
					//decrease the height of the parent row's name column
					$('#'+settingsTools.escape(name)).attr("rowspan", function(i, rs){
						return parseInt(rs) - 1;
					});
				}
			}
			
			$(document).ready(function(){
				var dialog = $("#dialog-form").dialog({
					autoOpen: false,
					height: 400,
					width: 350,
					modal: true,
					buttons: {
						"Create Setting": function(){
							$("#form").submit();
						},
						Cancel: function(){
							dialog.dialog( "close" );
						}
					},
					close: function() {
						//reset the form
						var scope = $('#form .scope');
						scope.val('');
						scope.trigger('change');
						$('#form .value').val('');
					}
				});
				
				var groupDialog = $("#group-dialog-form").dialog({
					autoOpen: false,
					height: 350,
					width: 350,
					modal: true,
					buttons: {
						"Add Scope": function(){
							$("#group-form").submit();
						},
						Cancel: function(){
							groupDialog.dialog("close");
						}
					},
					close: function() {
						//reset the form
						var scope = $('#group-form .scope');
						scope.val('');
						scope.trigger('change');
					}
				});
				
				function createSetting(form){
		 			settingsTools.createSetting(addRow, form);
					dialog.dialog("close");
				}
				
				function addGroupScope(form){
		 			var scope = form.find('.scope').val();
		 			var groupName = form.find('.name').val();
		 			
		 			var value, scopeDisplay, scopeUnique;
		 			switch(scope){
		 			case 'defaultScope':
		 				value = '';
		 				scopeDisplay = 'Default Scope';
		 				break;
		 			case 'cluster':
		 				value = '';
		 				scopeDisplay = 'Cluster Scope';
		 				break;
		 			case 'serverType':
		 				value = form.find('.server-type option:selected').val();
		 				scopeDisplay = 'Server Type: ' + value;
		 				break;
		 			case 'serverName':
		 				value = form.find('.server-name').val();
		 				scopeDisplay = 'Server Name: ' + value;
		 				break;
		 			case 'application':
		 				value = form.find('.application').val();
		 				scopeDisplay = 'Web App Name: ' + value;
		 				break;
		 			}
		 			scopeUnique = scope + '_' + value;
		 			
		 			addGroupRow(groupName, scopeUnique, scopeDisplay);
					groupDialog.dialog("close");
				}
				
				//open modals
				$(".show-create-form-btn").click(function() {
					//set the form's name value to the appropriate setting name
					$('#form .name').val(this.id.substring(4));
				 	dialog.dialog("open");
				});
				$(".show-create-group-form-btn").click(function() {
					$('#group-form .name').val($(this).closest('tr').children('.group-name').val());
				 	groupDialog.dialog("open");
				});
				
				//modal form submits, updates, and validation
				$('form').on("submit", function(event){
				 	event.preventDefault();
				});
				$('.scope').change(function(){
					settingsTools.updateFields($(this).closest('form'));
				});
				$("#form").validate(settingsTools.getValidatorOptionsWithHandler(createSetting, $('#form')));
				$("#group-form").validate(settingsTools.getValidatorOptionsWithHandler(addGroupScope, $('#group-form')));
				
				//table update/delete events
				//these are delegated so they don't need to be bound to each newly added row
		 		$('table').on('click', '.delete-setting-btn', function(){
		 			settingsTools.deleteSetting(this, removeRow);
		 		});
		 		$('table').on('click', '.delete-group-btn', function(){
		 			settingsTools.deleteGroup(this, removeRow);
		 		});
		 		$('table').on('submit', '.update-form', function(event){
		 			event.preventDefault();
		 			settingsTools.editSetting(this);
		 		});
		 		$('table').on('submit', '.update-group', function(event){
		 			event.preventDefault();
		 			settingsTools.editSettingGroup(this);
		 		});
			});
		});
		
	</script>
	<style>
		.ui-dialog label, .ui-dialog input { display:block; }
		td input { width:100%; }
	</style>
</head>
<body>

<!-- This gets turned into a modal. -->
<div id="dialog-form" title="Create Setting" style="display: none">
	<form id="form">
		<input type="hidden" class="name" name="name" value="">
		<input type="hidden" class="node" name="node" value="${nodeName}">
		<label for="scope">Setting Scope</label>
		<select	class="scope" name="scope" class="setting-type input-small">
			<option value="" disabled selected>Scope</option>
			<option value="defaultScope">Default</option>
			<option value="cluster">Cluster</option>
			<option value="serverType">Server Type</option>
			<option value="serverName">Server Name</option>
			<option value="application">Application</option>
		</select>
		<label for="serverType">Server Type</label>
		<select name="serverType" class="server-type setting-type input-small disableable" disabled>
			<option value="" disabled selected>Type</option>
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
		<label for="serverName">Server Name</label>
		<input type="text" name="serverName" class="server-name input-small disableable" placeholder="Server Name" value="" disabled>
		<label for="application">Application</label>
		<input type="text" name="application" class="application input-small disableable" placeholder="Application" value="" disabled>
		<label for="value">Value</label>
		<input type="text" name="value" class="value input-mini" placeholder="Value" value="">
		<!-- Allow form submission with keyboard without duplicating the dialog button -->
		<input type="submit" tabindex="-1" style="position:absolute; top:-1000px">
	</form>
</div>

<div id="group-dialog-form" title="Add Group Scope" style="display: none">
	<form id="group-form">
		<input type="hidden" class="name" name="name" value="">
		<input type="hidden" class="node" name="node" value="${nodeName}">
		<label for="scope">Setting Scope</label>
		<select	class="scope" name="scope" class="setting-type input-small">
			<option value="" disabled selected>Scope</option>
			<option value="defaultScope">Default</option>
			<option value="cluster">Cluster</option>
			<option value="serverType">Server Type</option>
			<option value="serverName">Server Name</option>
			<option value="application">Application</option>
		</select>
		<label for="serverType">Server Type</label>
		<select name="serverType" class="server-type setting-type input-small disableable" disabled>
			<option value="" disabled selected>Type</option>
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
		<label for="serverName">Server Name</label>
		<input type="text" name="serverName" class="server-name input-small disableable" placeholder="Server Name" value="" disabled>
		<label for="application">Application</label>
		<input type="text" name="application" class="application input-small disableable" placeholder="Application" value="" disabled>
		<!-- Allow form submission with keyboard without duplicating the dialog button -->
		<input type="submit" tabindex="-1" style="position:absolute; top:-1000px">
	</form>
</div>

	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<a href="?" class="btn btn-primary">&rarr; All settings</a>
		<h2 class="page-header">Cluster Settings Browser</h2>
		<ol class="breadcrumb">
			<c:forEach items="${ancestors}" var="ancestor">	
				<c:choose>
					<c:when test="${node.getName().equals(ancestor.getName())}">
						<li class="active">${ancestor.getShortName()}</li>
					</c:when>
					<c:otherwise>
						<li>
							<a href="?submitAction=browseSettings&name=${ancestor.getName()}">${ancestor.getShortName()}
							</a>
						</li>
					</c:otherwise>
				</c:choose>
			</c:forEach>
		</ol>
		<c:if test="${!roots.isEmpty()}">
			<ul class="nav nav-pills">
				<c:forEach items="${roots}" var="root">
					<li ${root.getShortName() == currentRootName ? 'class="active"' : ''}>
						<a href="?submitAction=browseSettings&name=${root.getName()}"> ${root.getShortName()}</a>
					</li>
				</c:forEach>
			</ul>
		</c:if>
		<c:if test="${!children.isEmpty()}">
			<ul class="nav nav-pills">
				<c:forEach items="${children}" var="child">		
					<li><a href="?submitAction=browseSettings&name=${child.getName()}">${child.getShortName()}</a></li>
				</c:forEach>
			</ul>	
		</c:if>
		<c:if test="${!listSettings.isEmpty() || !settingGroups.isEmpty()}">
			<table class="table table-condensed settings-table">
				<tr>
					<th rowspan="2">Name</th>
					<th colspan="7">Custom values</th>
				</tr>
				<tr>
					<th>Scope</th>
					<th>ServerType</th>
					<th>Server Name</th>
					<th>Application</th>
					<th>Value</th>
					<th>Action</th>
					<th>Log</th>					
				</tr>
				<c:forEach items="${listSettings}" var="setting">
					<c:set var="settingName" value="${setting.name}"></c:set>
					<c:set var="customSettings" value="${mapListsCustomSettings.get(settingName)}"></c:set>
					<c:choose>
						<c:when test="${setting.hasRedundantCustomValue}">
							<tr id="rowc_${settingName}" style="background: #F5FAFA;">
						</c:when>
						<c:when test="${setting.hasCustomValue}">
							<tr id="rowc_${settingName}" style="background: #ACD1E9;">
						</c:when>
					</c:choose>
					
							<td rowspan="${customSettings.size() + 1}" id="${settingName}">
								${settingName}
							</td>
							<td colspan="5">
								<span style="margin: 0 20px">
									current : <strong>${setting.getValue()}</strong>
								</span>
								<wbr>
								<span style="margin: 0 20px">
									default : <strong>${setting.getDefaultValue()}</strong>
								</span>
							</td>
							<td class="center">
								<a id="add_${settingName}" class="show-create-form-btn">add</a>
							</td>
							<td class="center">
								<a href="?submitAction=viewLog&name=${settingName}">view log</a>
							</td>
						</tr>
					<c:forEach var="customSetting" items="${customSettings}">
						<tr>
							<input type="hidden" name="node-name" value="${nodeName}">
							<input type="hidden" name="name" class="setting-name" value="${customSetting.name}">
							<td class="setting-scope">${customSetting.scope}</td>
							<td class="setting-type">${customSetting.serverType}</td>
							<td class="setting-serverName">${customSetting.serverName}</td>
							<td class="setting-application">${customSetting.application}</td>
							<td>
								<form class="update-form">
									<input value="${customSetting.value}" class="input-mini setting-value" placeholder="Value" name="value" id="valu_${customSetting.hashCode()}">
								</form>
							</td>
							<td class="center">
								<form class="update-form">
									<button id="upda_${settingName}" class="btn btn-mini btn-warning">update</button>
									<button type="button" "dele_${settingName}" class="btn btn-mini btn-danger delete-setting-btn">delete</button>
								</form>
							</td>
							<td></td>
						</tr>
					</c:forEach>
				</c:forEach>
			</table>
			
			<c:forEach items="${settingGroupsByScope}" var="group">
				<c:set var="groupName" value ="${group.key}"></c:set>
				<c:set var="scopeGroups" value ="${group.value}"></c:set>
				<c:set var="settingsList" value ="${settingGroups.get(groupName)}"></c:set>
				
				<table class="table table-condensed settings-table">
					<tr>
						<input type="hidden" class="group-name" value="${groupName}">
						<td colspan="${settingsList.size()}"><strong>${groupName}</strong></td>
						<td><a href="?submitAction=viewLog&name=${groupName}">view logs</a></td>
						<td><a class="show-create-group-form-btn">add</a></td>
					</tr>
					
					<tr>
						<td/>
						<c:forEach items="${settingsList}" var="setting">
							<td><strong>${setting.getName().substring(setting.getName().lastIndexOf('.') + 1)}</strong></td>
						</c:forEach>
						<td/>
					</tr>
					
					<tr>
						<td>current:</td>
						<c:forEach items="${settingsList}" var="setting">
							<c:choose>
							<c:when test="${!setting.defaultValue.equals(setting.value)}">
								<td style="background: #ACD1E9;">${setting.value}</td>
							</c:when>
							<c:when test="${setting.defaultValue.equals(setting.value)}">
								<td>${setting.defaultValue}</td>
							</c:when>
							</c:choose>
						</c:forEach>
						<td/>
					</tr>
					
					<tr>
						<td>default:</td>
						<c:forEach items="${settingsList}" var="setting">
							<td>${setting.defaultValue}</td>
						</c:forEach>
						<td/>
					</tr>
					
					<c:forEach items="${scopeGroups.keySet()}" var="scope">
						<tr tabindex="-1">
							<input type="hidden" class="group-scope" value="${scope.persistentString}">
							<td>${scope.displayString}:</td>
							<c:forEach items="${settingsList}" var="setting">
								<td class="setting">
									<form class="update-group">
										<input type="hidden" class="setting-name" value="${setting.name}">
										<c:choose><c:when test="${scopeGroups.get(scope).containsKey(setting.name)}">
											<input class="setting-value" value="${scopeGroups.get(scope).get(setting.name)}" placeholder="Current: ${setting.value}">
										</c:when><c:when test="${!scopeGroups.get(scope).containsKey(setting.name)}">
											<input class="setting-value" value="" placeholder="Current: ${setting.value}">
										</c:when></c:choose>
									</form>
								</td>
							</c:forEach>
							<td><button type="button" class="btn btn-mini btn-danger delete-group-btn">delete</button></td>
						</tr>
					</c:forEach>
					
				</table>
			</c:forEach>
			
		</c:if>
	</div>
</body>
</html>
