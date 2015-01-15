<div class="panel panel-default">
<div class="panel-heading">
	<h4 class="panel-title">
		<a data-toggle="collapse" data-parent="#accordion" href="#collapse${escapedName}">
			${name}
		</a>
	</h4>
</div>
<div id="collapse${escapedName}" class="panel-collapse collapse ${defaultVisible} no-value-on-0">
	<div class="panel-body">
		<div class="panel-group" id="accordion">
			<c:forEach items="${libs}" var="lib">
				<c:choose>
					<c:when test="${map}">
						<span class="property tree-level-0">${lib.key}</span>
						<br>
						<span class="property tree-level-1">Branch</span>
						<span class="value">${lib.value.branch}</span>
						<br>
						<span class="property tree-level-1">Commit</span>
						<span class="value">${lib.value.idAbbrev}</span>
						<br>
						<span class="property tree-level-1">Build time</span>
						<span class="value">
							<fmt:formatDate value="${lib.value.buildTime}" pattern="d MMM H:mm"/>
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