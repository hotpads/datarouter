<!DOCTYPE html>
<%@ include file="/jsp/generic/prelude.jspf"%>
<html>
<head>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
	<title>Exception records</title>
	<style>
	input{
		padding:0;
		margin:0;
	}
	.issue{
		width: 200px;
	}
	.issue input{
		width: 150px;
	}
	.align-center{
		text-align: center;
	}
	.align-right{
		text-align: right;
	}
	.numExceptions{	
		width: 20px;
	}
	</style>
	<script>
	require(['jquery', 'sorttable'], function($){
		const editIssue = function(){
			const issueCell = $(this).parent();
			const issue = issueCell.text();
			const type = issueCell.parent().data('type');
			const exceptionLocation = issueCell.parent().data('exceptionLocation');
			const input = $('<input>').addClass('form-control form-control-sm').val(issue.trim());
			const saveIssueCell = function(event){
				const exceptionMetadata = {
					type: type,
					exceptionLocation: exceptionLocation,
					issue: input.val() || null
				}
				$.post('saveIssue', $.param(exceptionMetadata))
					.done(function(){
						issueCell.empty()
							.append($('<a>')
								.text(input.val())
								.attr('href', '${issueLinkPrefix}' + input.val()))
							.append($('<button>').addClass('btn btn-link p-0 ml-2').attr('tabindex', '0')
								.append($('<span>').addClass('fas fa-pencil-alt')).click(editIssue))
					})
				event.stopPropagation();
			}
			input.keypress(function(event){
				if(event.which == 13){
					saveIssueCell(event)
				}
			})
			issueCell.empty()
				.append(input)
				.append($('<button>').addClass('save-issue-button btn btn-link py-0')
					.append($('<span>').addClass('fas fa-check'))
				.click(saveIssueCell))
		};
		$('.issue button').click(editIssue)
		$('.issue').click(function(event){
			event.stopPropagation()
		})
		$(document).click(function(){
			$('.save-issue-button').click()
		})
		$('.mute').click(function(event){
			const muteButton = $(this)
			const muteCell = muteButton.parent()
			const exceptionMetadata = {
				type: muteCell.parent().data('type'),
				exceptionLocation: muteCell.parent().data('exceptionLocation'),
				muted: Boolean(muteButton.find('.showing').data('mute'))
			}
			$.post('mute', $.param(exceptionMetadata))
				.done(function(){
					muteButton.find('.mute-text').toggleClass('d-none').toggleClass('showing');
				})
		})
	});
	</script>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
<nav class="navbar navbar-light bg-light px-0">
	<div class="container justify-content-start">
		<span class="navbar-brand">Exception Records</span>
		<span class="navbar-text" style="padding-bottom: 0.45rem">Report for hour starting on <strong>${lastPeriodStart}</strong></span>
		<form class="form-inline ml-0 ml-md-auto d-flex flex-nowrap col-12 col-md-auto px-0" action="${contextPath}${detailsPath}?exceptionRecord=${exceptionRecord}">
			<input class="form-control flex-grow-1 mr-2" type="search" placeholder="exception record ID" name="exceptionRecord" autofocus autocomplete>
			<input type="submit" class="btn btn-primary" value="Search">
		</form>
	</div>
</nav>
<div class="container-fluid">
	<table class="table table-sm table-striped my-4 border sortable">
		<thead>
			<tr>
				<th>Type</th>
				<th>Location</th>
				<th class="sorttable_nosort">Issue</th>
				<th>Count</th>
				<th class="sorttable_nosort">Details</th>
				<th class="sorttable_nosort" data-toggle="tooltip" title="Double click to toggle">Mute</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${exceptionRecordSummaries}" var="exceptionRecordSummary">
				<c:set var="metadata" value="${summaryMetadatas.get(exceptionRecordSummary.key.exceptionRecordSummaryMetadataKey)}" />
				<c:set var="issue" value="${metadata.issue}" />
				<tr data-type="${exceptionRecordSummary.key.type}"
					data-exception-location="${exceptionRecordSummary.key.exceptionLocation}">
					<td>${exceptionRecordSummary.key.type}</td>
					<td>${exceptionRecordSummary.key.exceptionLocation}</td>
					<td class="issue d-flex">
						<a href="${issueLinkPrefix}${issue}">${issue}</a>
						<button tabindex="0" class="btn btn-link p-0 ${not empty issue ? 'ml-2' : ''}"><i class="fas fa-pencil-alt"></i></button>
					</td>
					<td class="numExceptions align-right">${exceptionRecordSummary.numExceptions}</td>
					<td class="align-center"><a tabindex="0" href="${contextPath}${detailsPath}?exceptionRecord=${exceptionRecordSummary.sampleExceptionRecordId}" class="btn btn-link p-0 w-100"><i class="far fa-file-alt"></i></a></td>
					<td class="align-center">
						<a tabindex="0" class="mute btn btn-link p-0 w-100">
							<span class="mute-text ${metadata.muted ? 'showing' : 'd-none'}" data-mute="false">unmute</span>
							<span class="mute-text ${metadata.muted ? 'd-none' : 'showing'}" data-mute="true">mute</span>
						</a>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>
</body>
</html>
