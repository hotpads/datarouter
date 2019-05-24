<div class="panel panel-default">
<div class="panel-heading">
	<h4 class="panel-title">
		${name}
	</h4>
</div>
<div class="no-value-on-0">
	<div class="panel-body">
		<div class="panel-group" id="accordion">
			<c:forEach items="${libs}" var="lib">
				<c:choose>
					<c:when test="${map}">
						<span class="property tree-level-0" title="${lib.value.describeShort}">${lib.key}</span>
						<br>
						<span class="property tree-level-1">Branch</span>
						<span class="value">${lib.value.branch}</span>
						<br>
						<span class="property tree-level-1">Commit</span>
						<span class="value" title="${commitTime} by ${lib.value.commitUserName}">
							${lib.value.idAbbrev}
						</span>
						<br>
						<span class="property tree-level-1">Build time</span>
						<span class="value">
							${lib.value.buildTime}
						</span>
						<br>
						<span class="property tree-level-1">Build id</span>
						<span class="value">
							${buildDetailedLibraries[lib.key].buildId}
						</span>
						<br>
					</c:when>
					<c:otherwise>
						<span class="property tree-level-0">${lib}</span>
						<br>
					</c:otherwise>
				</c:choose>
			</c:forEach>
		</div>
	</div>
</div>
</div>
