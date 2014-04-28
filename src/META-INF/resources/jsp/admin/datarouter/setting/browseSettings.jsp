<%-- <%@page import="com.hotpads.config.server.enums.HotPadsServerType"%> --%>
<%@page import="com.hotpads.util.core.web.HTMLSelectOptionBean"%>
<%@page import="com.hotpads.setting.cluster.ClusterSettingScope"%>
<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ include file="../../../generic/prelude-datarouter.jspf"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Hotpads : Cluster settings</title>
<%@ include file="/jsp/css/css-import.jspf" %>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
<script type="text/javascript">
 		require(["bootstrap/bootstrap"], function($) {});
</script>
<script type="text/javascript">
function stayFocus(object){
	setTimeout(function(){object.form.instance.focus();},10);
}
</script>	
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<h2 class="page-header">Cluster Settings Browser</h2>
		<ul class="breadcrumb">
			<c:forEach items="${ancestors}" var="ancestor">	
				<c:choose>
					<c:when test="${node.getName().equals(ancestor.getName())}">
						<li class="active">${ancestor.getShortName()} <span class="divider">.</span></li>
					</c:when>
					<c:otherwise><li><a href="?submitAction=browseSettings&name=${ancestor.getName()}">
						${ancestor.getShortName()}</a> <span class="divider">.</span></li>
					</c:otherwise>
				</c:choose>
			</c:forEach>
		</ul>
		<c:if test="${!children.isEmpty()}">
			<div class="btn-toolbar">
				<div class="btn-group">
					<c:forEach items="${children}" var="child">		
						<a class="btn"
							href="?submitAction=browseSettings&name=${child.getName()}">${child.getShortName()}</a>
					</c:forEach>
				</div>	
			</div>
		</c:if>
		<c:if test="${!listSettings.isEmpty()}">
			<table class="table table-condensed">
				<tr>
					<th class="left_col">Default and current values</th>
					<th class="right_col">Custom values</th>
				</tr>
				<c:forEach items="${listSettings}" var="setting">
					<tr
						<c:choose>
							<c:when test="${setting.getHasRedundantCustomValue()}">style="background: #F5FAFA;"</c:when>
							<c:when test="${setting.getHasCustomValue()}">style="background: #ACD1E9;"</c:when>
						</c:choose>
						>
						<td class="left_col">
							<p>
								<c:choose>
									<c:when test="${setting.getHasCustomValue()}">
										<a href=?submitAction=detailSetting&name=${setting.getName()}>
											<strong>${setting.getName()}</strong>
										</a>
									</c:when>
									<c:otherwise>${setting.getName()}</c:otherwise>
								</c:choose>
								<br>
								<strong>Current value : </strong>${setting.getValue()}
								<br>
								<strong>Default value : </strong>${setting.getDefaultValue()}
							</p>
						</td>
						<td class="right_col">
							<c:choose>
								<c:when test="${setting.getHasCustomValue()}">
									<div>
										<c:if test="${! empty mapListsCustomSettings.get(setting.getName())}">
										<table
											class="table table-bordered table-condensed table-striped">
											<tr>
												<th>Scope</th>
												<th>ServerType</th>
												<th>Instance</th>
												<th>Application</th>
												<th>Value</th>
												<th>Delete</th>
											</tr>
											<c:forEach
												items="${mapListsCustomSettings.get(setting.getName())}"
												var="customSetting"
												>
<!-- 												<form method="post" action="?"> -->
												<tr>
													<td>${customSetting.scope}</td>
													<td>${customSetting.serverType}</td>
													<td>${customSetting.instance}</td>
													<td>${customSetting.application}</td>
													<td>${customSetting.value}</td>
													<td class="center"><a class="btn btn-mini btn-danger"
														href="?submitAction=delete
														&nodeName=${nodeName}
														&name=${customSetting.name}
														&scope=${customSetting.scope}
														&serverType=${customSetting.serverType}
														&instance=${customSetting.instance}
														&application=${customSetting.application}"
														onclick="return confirm('Are you sure?');">delete</a>
													</td>
												</tr>
<!-- 												</form> -->
											</c:forEach>
										</table>
										</c:if>
										<a class="btn btn-mini btn-primary" id="bt_${setting.hashCode()}" onclick="$('#bt_${setting.hashCode()}').hide();$('#form_${setting.hashCode()}').show();">Add a custom setting</a>
										<form class="well form-inline" method="post" action="?" id="form_${setting.hashCode()}" style="display: none; margin-top: 20px;">
											<label> Server Type :
												<select	name="serverType" class="input-small">
													<c:forEach items="${serverTypeOptions}" var="serverTypeOption">
														<option value="${serverTypeOption.value}">${serverTypeOption.name}</option>
													</c:forEach>
												</select>
											</label>
											<input type="hidden" name="nodeName" value="${nodeName}">
											<input type="hidden" name="name" value="${setting.getName()}">
											<input type="text" name="instance" class="input-medium" placeholder="Instance">
											<input type="text" name="value" class="input-medium" placeholder="Value">
											<input class="btn btn-mini btn-warning" type="submit" name="submitAction" value="Add">
										</form>
									</div>
								</c:when>
								<c:otherwise>${setting.getDefaultValue()}</c:otherwise>
							</c:choose>
						</td>
					</tr>
				</c:forEach>
			</table>
		</c:if>
	</div>
</body>
</html>