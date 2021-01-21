<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<title>Browse Cluster Settings | Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<link rel="stylesheet" href="//code.jquery.com/ui/1.12.0/themes/base/jquery-ui.css">
	<script type="text/javascript" src="${contextPath}/js/setting/createSettings.js"></script>
	<script type="text/javascript">
		require(['settings-tools','jquery.validate', 'jquery-ui'], function(settingsTools){
			function updateDbOverrideCount(settingName, delta){
				const $el = $('#count_' + settingName)
				$el.text(parseInt($el.text(), 10) + delta)
			}

			function addRow(setting){
				const template = document.getElementById('settingRowTemplate')
				const $clone = $(template.content.cloneNode(true))
				$clone.find('.setting-name').attr('value', setting.name)
				$clone.find('.setting-scope').text(setting.scope)
				$clone.find('.setting-type').text(setting.serverType)
				$clone.find('.setting-serverName').text(setting.serverName)
				$clone.find('form.update-form input.setting-value').attr('value', setting.value)
				const escapedName = settingsTools.escape(setting.name)
				$('#rowc_' + escapedName).closest('.dbOverridesTable').show()
					.find('tbody').append($clone).find('tr:last-child').highlight()
				updateDbOverrideCount(escapedName, +1)
			}

			function removeRow(row, name){
				// check if this is the last row and hide the table if it is
				// +1 for a hidden row and +1 for the row being removed
				if(row.closest('tbody').find('tr').length === 2){
					row.closest('.dbOverridesTable').hide()
				}
				row.remove()
				updateDbOverrideCount(settingsTools.escape(name), -1)
			}

			$(document).ready(function(){
				const dialog = $("#dialog-form").dialog({
					autoOpen: false,
					height: 460,
					width: 350,
					modal: true,
					buttons: {
						"Create Setting": function(){
							$("#form").submit();
						},
						Cancel: function(){
							dialog.dialog("close");
						}
					},
					close: function() {
						//reset the form
						$('#form .value').val('')
						$('#form .scope').val('').trigger('change')
					}
				});

				function createSetting(form){
					settingsTools.createSetting(addRow, form);
					dialog.dialog("close");
				}

				//open modals
				$(".show-create-form-btn").click(function() {
					//set the form's name value to the appropriate setting name
					$('#form .name').val(this.id.substring(4));
					dialog.dialog("open");
				});

				//modal form submits, updates, and validation
				$('form').on("submit", function(event){
					event.preventDefault();
				});
				$('.scope').change(function(){
					settingsTools.updateFields($(this).closest('form'));
				});
				$("#form").validate(settingsTools.getValidatorOptionsWithHandler(createSetting, $('#form')));

				//table update/delete events
				//these are delegated so they don't need to be bound to each newly added row
				$('table').on('click', '.delete-setting-btn', function(){
					settingsTools.deleteSetting(this, removeRow);
				});
				$('table').on('submit', '.update-form', function(event){
					event.preventDefault();
					settingsTools.editSetting(this);
				});

				const settingHref = settingName => "${contextPath}/datarouter/settings/browseSettings?name=" + settingName

				$.ui.autocomplete.prototype._renderItem = function(ul, matchResult) {
					$(ul).addClass('shadow')
					return $('<a>')
						.addClass('list-group-item list-group-item-action border-top-0 border-left-0 border-right-0 border-bottom m-0')
						.attr('href', settingHref(matchResult.name))
						.append(matchResult.name)
						.appendTo(ul)
				}

				const autocompleteToggleClasses = 'position-absolute w-100 w-auto'
				$('#search-settings-autocomplete').autocomplete({
					source: '${contextPath}/datarouter/settings/searchSettingNames'
				}).focusin(function(){
					$(this).toggleClass(autocompleteToggleClasses).data('uiAutocomplete').search($(this).val())
				}).focusout(function(){
					$(this).toggleClass(autocompleteToggleClasses)
				}).on('autocompleteselect', (event, ui) => {
					window.location.href = settingHref(ui.item.name)
					return false
				})
			});
		});

	</script>
	<style>
		.ui-dialog { padding: 0; }
		.ui-dialog .ui-dialog-titlebar {
			border-radius: 0;
			border-top: none;
			border-left: none;
			border-right: none;
		}
		.ui-dialog label { margin: 6px 0 1px 0 }
		.ui-dialog label, .ui-dialog input { display:block; }
		td input { width:100%; }
	</style>
</head>
<body>
	<template id="settingRowTemplate">
		<tr>
			<input type="hidden" name="name" class="setting-name">
			<td class="setting-scope"></td>
			<td class="setting-type"></td>
			<td class="setting-serverName"></td>
			<td>
				<form class='update-form'>
					<input class='input-mini setting-value'>
				</form>
			</td>
			<td class='center'>
				<form class='update-form'>
					<button id='upda_${name}' class='btn btn-mini btn-warning'>update</button>
					<button id='dele_${name}' class='btn btn-mini btn-danger delete-setting-btn' type='button'>delete</button>
				</form>
			</td>
			<td></td>
		</tr>
	</template>
	<!-- Create settings modal -->
	<div id="dialog-form" title="Create Setting" style="display: none">
		<form id="form">
			<input type="hidden" class="name" name="name" value="">
			<input type="hidden" class="node" name="node" value="${nodeName}">
			<label for="scope">Setting Scope</label>
			<select	class="scope" name="scope" class="setting-type input-small">
				<option value="" disabled selected>Scope</option>
				<option value="defaultScope">Default</option>
				<option value="serverType">Server Type</option>
				<option value="serverName">Server Name</option>
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
			<label for="value">Value</label>
			<input type="text" name="value" class="value input-mini" placeholder="Value" value="">
			<hr>
			<textarea name="comment" class="value input-mini" placeholder="Comment on change"></textarea>
			<!-- Allow form submission with keyboard without duplicating the dialog button -->
			<input type="submit" tabindex="-1" style="position:absolute; top:-1000px">
		</form>
	</div>
	<!-- modal end -->
	<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
	<div class="container mt-4 mobile-friendly">
		<a href="?" class="btn btn-primary"><i class="fas fa-angle-left"></i>&nbsp;All settings</a>
		<h2 class="mt-5 pb-2 mb-3 d-flex justify-content-between position-relative">
			Cluster Settings Browser
			<input id="search-settings-autocomplete" class="d-none d-sm-block form-control w-auto" type="text" placeholder="Search...">
		</h2>
		<c:if test="${!categoryMap.isEmpty()}">
			<ul class="nav nav-tabs">
				<c:forEach items="${categoryMap}" var="category">
					<li class="nav-item font-italic">
						<a class="nav-link ${category.key.display == currentCategory ? 'active' : ''}" href="#${category.key.href}" data-toggle="tab">
							${category.key.display}
						</a>
					</li>
				</c:forEach>
			</ul>
			<div class="tab-content p-1 border border-top-0">
				<c:forEach items="${categoryMap}" var="category">
					<div id="${category.key.href}" class="tab-pane ${category.key.display == currentCategory ? 'show active' : ''}" role="tabpanel">
						<ul class="nav nav-pills">
							<c:forEach items="${category.value}" var="root">
								<li class="nav-item">
									<a class="nav-link ${root.getShortName() == currentRootName ? 'active' : ''}"
											href="?submitAction=browseSettings&name=${root.getName()}"> ${root.getShortName()}</a>
								</li>
							</c:forEach>
						</ul>
					</div>
				</c:forEach>
			</div>
		</c:if>
		<c:if test="${not empty ancestors}">
			<nav class="my-2">
				<ol class="breadcrumb m-0">
					<c:forEach items="${ancestors}" var="ancestor">
						<c:choose>
							<c:when test="${nodeName.equals(ancestor.getName())}">
								<li class="breadcrumb-item active">${ancestor.getShortName()}</li>
							</c:when>
							<c:otherwise>
								<li class="breadcrumb-item">
									<a href="?submitAction=browseSettings&name=${ancestor.getName()}">${ancestor.getShortName()}</a>
								</li>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</ol>
			</nav>
		</c:if>
		<c:if test="${!children.isEmpty()}">
			<ul class="nav nav-pills mb-4">
				<c:forEach items="${children}" var="child">
					<li class="nav-item"><a class="nav-link" href="?submitAction=browseSettings&name=${child.getName()}">${child.getShortName()}</a></li>
				</c:forEach>
			</ul>
		</c:if>
		<c:choose>
			<c:when test="${listSettings != null && !listSettings.isEmpty()}">
				<c:forEach items="${listSettings}" var="setting">
					<c:set var="settingName" value="${setting.name}"></c:set>
					<c:set var="customSettings" value="${mapListsCustomSettings.get(settingName)}"></c:set>
					<div class="w-100 mb-4 border">
						<div class="w-100 py-2 px-1 px-sm-4">
							<a class="text-break font-weight-bold" href="?submitAction=browseSettings&name=${settingName}">
								${settingName}
							</a>
						</div>
						<div class="w-100 py-1 px-1 px-sm-4 text-break">
							current: <strong>${setting.value}</strong>
						</div>
						<div class="w-100 py-1 px-1 px-sm-4 text-break">
							global default: <strong>${setting.defaultValue}</strong>
						</div>
						<c:if test="${mightBeDevelopment}">
							<div class="w-100 py-1 px-1 px-sm-4">
								<span class="font-weight-bold">${fn:length(setting.settingTags)}</span> tag overrides
								<c:if test="${not empty setting.settingTags}">
									<div class="w-100 py-2 px-0 px-sm-4 table-responsive">
										<table class="table table-sm settings-table">
											<tr>
												<th>Setting Tag</th>
												<th>Value</th>
												<th>Active</th>
											</tr>
											<c:forEach var="def" items="${setting.settingTags}">
												<c:if test="${def.active}">
													<c:set var="rowStyle" value="style=\"background: #E1C2C2\""/>
													<c:set var="rowContent" value="&#x2714;&#xfe0f;"/>
												</c:if>
												<tr ${rowStyle}>
	    											<td>${def.settingTag}</td>
													<td>${def.value}</td>
													<td>${rowContent}</td>
												</tr>
												<c:remove var = "rowStyle"/>
												<c:remove var = "rowContent"/>
											</c:forEach>
										</table>
									</div>
								</c:if>
							</div>
						</c:if>
						<div class="w-100 py-1 px-1 px-sm-4">
							<span class="font-weight-bold">${fn:length(setting.codeOverrides)}</span> code overrides
							<c:if test="${not empty setting.codeOverrides}">
								<div class="w-100 py-2 px-0 px-sm-4 table-responsive">
									<table class="table table-sm settings-table">
										<tr>
											<th>Environment Type</th>
											<th>Environment Name</th>
											<th>Server Type</th>
											<th>Server Name</th>
											<th>Value</th>
											<th>Active</th>
										</tr>
										<c:forEach var="def" items="${setting.codeOverrides}">
											<c:if test="${def.active}">
												<c:set var="rowStyle" value="style=\"background: #E1C2C2\""/>
												<c:set var="rowContent" value="&#x2714;&#xfe0f;"/>
											</c:if>
											<tr ${rowStyle}>
												<td class="setting-default">${def.globalOrEnvironmentType}</td>
												<td class="setting-environment">${def.environmentName}</td>
												<td class="setting-type">${def.serverType}</td>
												<td class="setting-serverName">${def.serverName}</td>
												<td class="setting-value">${def.value}</td>
												<td class="setting-active">${rowContent}</td>
											</tr>
											<c:remove var = "rowStyle"/>
											<c:remove var = "rowContent"/>
										</c:forEach>
									</table>
								</div>
							</c:if>
						</div>
						<div class="w-100 py-1 px-1 px-sm-4">
							<span id="count_${settingName}" style="font-weight:bold">${fn:length(customSettings)}</span> database overrides
							&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;
							<a tabindex="0" id="add_${settingName}" class="show-create-form-btn">add</a>
							&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;
							<a href="?submitAction=logsForName&name=${settingName}">view log</a>
							<div class="dbOverridesTable px-1 px-sm-4 table-responsive" style="${empty customSettings ? 'display: none' : ''}">
								<table class="table table-sm settings-table">
									<thead>
										<tr>
											<th>Scope</th>
											<th>Server Type</th>
											<th>Server Name</th>
											<th>Value</th>
											<th>Action</th>
										</tr>
									</thead>
									<tbody>
									<c:choose>
										<c:when test="${setting.hasRedundantCustomValue}">
											<tr id="rowc_${settingName}" style="background: #F5FAFA;">
										</c:when>
										<c:when test="${setting.hasCustomValue}">
											<tr id="rowc_${settingName}" style="background: #ACD1E9;">
										</c:when>
									</c:choose>
									</tr>
									<c:forEach var="customSetting" items="${customSettings}">
										<tr>
											<input type="hidden" name="node-name" value="${nodeName}">
											<input type="hidden" name="name" class="setting-name" value="${customSetting.name}">
											<td class="setting-scope">${customSetting.scope.persistentString}</td>
											<td class="setting-type">${customSetting.serverType}</td>
											<td class="setting-serverName">${customSetting.serverName}</td>
											<td>
												<form class="update-form">
													<input value="${customSetting.value}" class="form-control setting-value"
														placeholder="Value" name="value" id="valu_${customSetting.hashCode()}">
												</form>
											</td>
											<td class="center">
												<form class="update-form">
													<button id="upda_${settingName}" class="btn btn-mini btn-warning">update</button>
													<button id="dele_${settingName}" class="btn btn-mini btn-danger delete-setting-btn" type="button">delete</button>
												</form>
											</td>
										</tr>
									</c:forEach>
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</c:forEach>
			</c:when>
			<c:otherwise>
				<c:if test="${!nodeName.isEmpty()}">
					<div>
						<h3>No settings found for "${nodeName}"</h3>
						<a href="?submitAction=logsForName&name=${nodeName}">try logs</a>
					</div>
				</c:if>
			</c:otherwise>
		</c:choose>
	</div>
	<br/>
</body>
</html>
