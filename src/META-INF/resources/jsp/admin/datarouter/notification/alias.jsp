<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ include file="../../../generic/prelude-datarouter.jspf"%>
<!DOCTYPE html>
<html>
<head>
<title>Notification - Alias</title>
<meta charset="utf-8">
<%@ include file="/jsp/generic/head.jsp" %>
<%@ include file="/jsp/css/css-import.jspf"%>
<style>
body {
	padding-right: 0;
	padding-left: 0;
}
.moderator-btn{
	float: right;
}
#aliases{
	display: inline-block;
}
#details{
	padding: 0 6px;
	width: calc(100% - 12px);
	display: inline-block;
	vertical-align: top;
}
@media (min-width: 600px){
	#details{
		width: calc(100% - 218px)
	}
	#aliases{
		width: 200px;
		border-right: solid 1px grey;
	}
}
form:nth-child(odd) {
	background-color: #EEE;
	box-shadow: 0 0 1px 2px white inset;
}
form {
	margin-bottom: 4px;
	display: flex;
	padding: 2px;
}
form>span {
	margin: 4px 10px;
}
form>*:first-child {
	flex: 1;
}
</style>
<script data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script>
require(["bootstrap/bootstrap"]);
String.prototype.pluralize = function(count, plural){
	if (plural == null) {
		plural = this + 's';
	}
	return count < 2 ? this : plural;
}
String.prototype.by = function(count){
	return count + ' ' + this.pluralize(count);
}
function updateHistory(aliasName) {
	var	aliasServiceRoot = location.pathname.replace(/alias\/.*/, 'alias');
	history.pushState(null, aliasName, aliasServiceRoot + '/' + aliasName)
}
function getAliasPanel(aliasName){
	var details = $('#details');
	details.empty();
	$.ajax({
		url:'${contextPath}/datarouter/notification/alias/' + aliasName,
		dataType : 'json'
	}).done(function(data){
		updateHistory(data.alias.name);
		details.append($('<a>')
				.text('moderator'.by(data.moderators.length))
				.addClass('moderator-btn')
				.attr('data-toggle', 'modal')
				.attr('data-target', '#moderators'));
		details.append($('<h4>').text(data.alias.name));
		fillModeratorsWindow(data.moderators, data.haveAuthorityOnList);
		$.each(data.subscribers, function(){
			var form = $('<form>').append($('<span>')
					.text(this.key.email));
			if(data.haveAuthorityOnList){
				form.append($('<input>')
						.attr('type', 'hidden')
						.attr('name', 'unsubscribeEmail')
						.attr('value', this.key.email));
				form.append($('<input>')
						.attr('type', 'submit')
						.attr('value', 'Unsubscribe')
						.addClass('btn'));
			}
			details.append(form);
		});
		details.append($('<form>')
				.append($('<input>')
						.attr('name', 'subscribeEmail')
						.attr('type', 'email')
						.attr('value', userEmail)
						.click(function(){
							if (userEmail === this.value) {
								this.value = '';
							}
						})
						.blur(function(){
							if ('' === this.value) {
								this.value = userEmail;
							}
						}))
				.append($('<input>')
						.attr('type', 'submit')
						.attr('value', 'Subscribe')
						.addClass('btn')));
		details.append($('<a>')
				.text('This alias recieves ' + 'automated email'.by(data.automatedEmails.length))
				.attr('data-toggle', 'modal')
				.attr('data-target', '#automated-email'));
		fillAutomatedWindow(data.automatedEmails);
		details.append($('<span>').text(' ' + endash + ' '));
		details.append($('<a>')
				.text('See last ' + 'sent email'.by(data.notificationLogs.length))
				.attr('data-toggle', 'modal')
				.attr('data-target', '#emails'));
		fillSentEmailNotificationLog(data.notificationLogs, data.emails);
	});
}
function fillModeratorsWindow(moderators, haveAuthorityOnList){
	var modalBody = $('#moderators').find('.modal-body');
	modalBody.empty();
	$.each(moderators, function(){
		var form = $('<form>')
				.append($('<span>')
						.text(this.key.email));
		if(haveAuthorityOnList){
			form.append($('<input>')
					.attr('type', 'hidden')
					.attr('name', 'removeModerator')
					.attr('value', this.key.email));
			form.append($('<input>')
					.attr('type', 'submit')
					.attr('value', 'Remove')
					.addClass('btn'));
		}
		modalBody.append(form);
	});
	if(haveAuthorityOnList){
		modalBody.append($('<form>')
				.append($('<input>')
						.attr('name', 'addModerator')
						.attr('type', 'email'))
				.append($('<input>')
						.attr('type', 'submit')
						.addClass('btn')));
	}
}
function fillAutomatedWindow(automateds){
	var modalBody = $('#automated-email').find('.modal-body');
	modalBody.empty();
	var ul = $('<ul>');
	$.each(automateds, function(){
		ul.append($('<li>').text(this.name + " : " + this.description));
	});
	modalBody.append(ul);
}
function fillSentEmailNotificationLog(logs, emails){
	var modalBody = $('#emails').find('.modal-body');
	modalBody.empty();
	var idContainer = 'accordion2';
	var div = $('<div>')
			.addClass('accordion')
			.attr('id', idContainer);
	$.each(logs, function(index){
		var idInterne = 'collapse' + index;
		var subject = emails[index] ? emails[index].subject : '';
		var content = emails[index] ? emails[index].content : 'Can not show the email content because it is not a AutomatedEmail.';
		var accordionGroup = $('<div>')
				.addClass('accordion-group')
				.append($('<div>')
						.addClass('accordion-heading')
						.append($('<a>')
								.addClass('accordion-toggle')
								.attr('data-toggle', 'collapse')
								.attr('data-parent', '#' + idContainer)
								.attr('href', '#' + idInterne)
								.text(this.created + ' ' + endash + ' ' + subject)))
				.append($('<div>')
						.addClass('accordion-body')
						.addClass('collapse')
						.attr('id', idInterne)
						.append($('<div>')
								.addClass('accordion-inner')
								.html(content)));
		div.append(accordionGroup);
	});
	modalBody.append(div);
}
$(document).ready(function() {
	if("${preLoadedAlias}"){
		getAliasPanel("${preLoadedAlias}");
	}
	$('#aliases').find('li>a').each(function(){
		$(this).click(function(){
			getAliasPanel(this.dataset.aliasName);
		});
	});
});
var userEmail = "${userEmail}";
var endash = decodeURIComponent('%E2%80%93');
</script>
</head>
<body class="input-no-margin">
<%@ include file="/jsp/menu/common-navbar.jsp"%>
<%@ include file="/jsp/menu/dr-navbar.jsp"%>
<div id="aliases">
	<h3>Aliases</h3>
	<ul>
		<c:forEach items="${aliases}" var="alias">
			<li>
				<a href="#details" data-alias-name="${alias.name}">
					${alias.name}
				</a>
			</li>
		</c:forEach>
	</ul>
</div>
<div id="details"></div>

<div class="modal fade" id="moderators" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">
          <span aria-hidden="true">&times;</span>
          <span class="sr-only">Close</span>
        </button>
        <h4 class="modal-title" id="myModalLabel">Moderators</h4>
      </div>
      <div class="modal-body"></div>
      <div class="modal-footer"></div>
    </div>
  </div>
</div>
<div class="modal fade" id="automated-email" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">
          <span aria-hidden="true">&times;</span>
          <span class="sr-only">Close</span>
        </button>
        <h4 class="modal-title" id="myModalLabel">Automated emails</h4>
      </div>
      <div class="modal-body"></div>
      <div class="modal-footer"></div>
    </div>
  </div>
</div>
<div class="modal fade" id="emails" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">
          <span aria-hidden="true">&times;</span>
          <span class="sr-only">Close</span>
        </button>
        <h4 class="modal-title" id="myModalLabel">Emails sent</h4>
      </div>
      <div class="modal-body"></div>
      <div class="modal-footer"></div>
    </div>
  </div>
</div>
<div class="modal fade" id="emails" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">
          <span aria-hidden="true">&times;</span>
          <span class="sr-only">Close</span>
        </button>
        <h4 class="modal-title" id="myModalLabel">Emails sent</h4>
      </div>
      <div class="modal-body"></div>
      <div class="modal-footer"></div>
    </div>
  </div>
</div>
</body>
</html>