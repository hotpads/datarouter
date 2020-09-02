<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<title>Edit Cluster Settings | Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script type="text/javascript" src="${contextPath}/js/setting/createSettings.js"></script>
	<script type="text/javascript">
	require(['settings-tools','jquery.validate'], function(settingsTools) {
		function addRow(setting) {
			const template = document.getElementById('settingRowTemplate')
			const $clone = $(template.content.cloneNode(true))
			$clone.find('.setting-name').text(setting.name)
			$clone.find('.setting-scope').text(setting.scope)
			$clone.find('.setting-type').text(setting.serverType)
			$clone.find('.setting-serverName').text(setting.serverName)
			$clone.find('form.update-form input[name="value"]').attr('value', setting.value)
			$('.settings-table tbody').prepend($clone)
				.first().highlight() // highlight newly appended row
		}

		$(document).ready(function(){
			$('#form .scope').change(function(){
				settingsTools.updateFields()
			})
			$('#form').submit(e => e.preventDefault())
			$('#form').validate(settingsTools.getValidatorOptionsWithHandler(function(){
				settingsTools.createSetting(function(setting){
					addRow(setting)
				});
			}))
			//these are delegated so they don't need to be bound to each newly added row
			$('table').on('click', '.delete-setting-btn', function(){
				settingsTools.deleteSetting(this, function(row){
					row.remove()
				});
			});
			$('table').on('submit', '.update-form', function(event){
				event.preventDefault();
				settingsTools.editSetting(this);
			});
		});
	});
	</script>
</head>

<template id="settingRowTemplate">
	<tr>
		<td class="setting-name"></td>
		<td class="setting-scope"></td>
		<td class="setting-type"></td>
		<td class="setting-serverName"></td>
		<td>
			<form class="update-form">
				<input name="value" class="input-medium setting-value">
			</form>
		</td>
		<td class="center">
			<button class="btn btn-danger delete-setting-btn" type="button">delete</button>
		</td>
		<td class="center">
			<form class="update-form">
				<button class="btn btn-warning">update</button>
			</form>
		</td>
	</tr>
</template>

<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
	<div class="container mobile-friendly mt-4">
		<a href="?submitAction=browseSettings" class="btn btn-primary"><i class="fas fa-xs fa-search"></i> Browse settings</a>
		<div class="card mt-4">
			<nav class="navbar navbar-light bg-light">
				<span class="navbar-brand py-0">Create</span>
			</nav>
			<div class="card-body">
				<form id="form">
					<div class="form-group row">
						<label for="name" class="col-form-label col-sm-2 d-none d-md-block">Name</label>
						<div class="col-12 col-md-auto">
							<input type="text" name="name" value="" class="name form-control" placeholder="Name">
						</div>
					</div>
					<div class="form-group row">
						<label for="scope" class="col-form-label col-sm-2 d-none d-md-block">Setting Scope</label>
						<div class="col-12 col-md-auto">
							<select name="scope" class="scope setting-type form-control">
								<option value="" disabled selected>Scope</option>
								<option value="defaultScope">Default</option>
								<option value="serverType">Server Type</option>
								<option value="serverName">Server Name</option>
							</select>
						</div>
					</div>
					<div class="form-group row">
						<label for="serverType" class="col-form-label col-sm-2 d-none d-md-block">Server Type</label>
						<div class="col-12 col-md-auto">
							<select name="serverType" class="server-type setting-type form-control disableable" disabled>
								<option disabled selected>Type</option>
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
					</div>
					<div class="form-group row">
						<label for="serverName" class="col-form-label col-sm-2 d-none d-md-block">Server Name</label>
						<div class="col-12 col-md-auto">
							<input type="text" name="serverName" class="server-name form-control disableable" placeholder="Server Name" value="" disabled>
						</div>
					</div>
					<div class="form-group row">
						<label for="value" class="col-form-label col-sm-2 d-none d-md-block">Value</label>
						<div class="col-12 col-md-auto">
							<input type="text" name="value" class="value form-control" placeholder="Value" value="">
						</div>
					</div>
					<div class="row">
						<div class="offset-md-2 col-auto"><button id="create" class="btn btn-success">create</button></div>
					</div>
				</form>
			</div>
		</div>
		<div class="card my-4">
			<nav class="navbar navbar-light bg-light">
				<span class="navbar-brand py-0">Cluster Settings</span>
			</nav>
			<div class="card-body">
			${validities}
			</div>
		</div>
		<div class="card my-4">
			<nav class="navbar navbar-light bg-light">
				<span class="navbar-brand py-0">Cluster Settings</span>
				<c:if test="${not empty param.prefix}">
					<span class="navbar-text mr-auto">
						prefixed by <code>${param.prefix}</code>
						&nbsp;<a href="settings" class="close">&times;</a>
					</span>
				</c:if>
				<ul class="navbar-nav">
					<form class="form-search input-group navbar-item" method="get" action="?">
						<input type="text" class="form-control search-query" name="prefix" placeholder="Name Prefix" autofocus>
						<div class="input-group-append"><input type="submit" class="btn btn-primary" value="Search"></div>
					</form>
				</ul>
			</nav>
			<div class="table-responsive-xl">
				<table class="table table-sm settings-table mb-0">
					<thead class="thead-dark">
						<tr>
							<th>Name</th>
							<th>Scope</th>
							<th>ServerType</th>
							<th>ServerName</th>
							<th>Value</th>
							<th>Delete</th>
							<th>Update</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${rows}" var="row">
							<c:set var="setting" value="${row.setting}"/>
							<c:set var="validity" value="${row.validity}"/>
							<c:set var="cardId" value="card${serviceProvider.id}${webApp}"/>
							<tr class="${validity.color}">
								<td class="setting-name">
									<a href="?submitAction=browseSettings&name=${setting.name}">
										${setting.name}
									</a>
								</td>
								<td class="setting-scope">${setting.scope.persistentString}</td>
								<td class="setting-type">${setting.serverType}</td>
								<td class="setting-serverName">${setting.serverName}</td>
								<td>
									<form class="update-form">
										<input name="value" class="setting-value form-control" style="min-width: 100px" value="${setting.value}">
									</form>
								</td>
								<td class="center" >
									<button class="btn btn-danger delete-setting-btn" type="button">
										delete
									</button>
								</td>
								<td class="center">
									<form class="update-form">
										<button class="btn btn-warning">
											update
										</button>
									</form>
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</body>
</html>
