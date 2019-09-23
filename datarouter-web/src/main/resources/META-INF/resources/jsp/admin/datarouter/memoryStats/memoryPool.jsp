<div class="card ${not empty additionalCardClasses ? additionalCardClasses : ''}">
	<div class="card-header py-1">
		<h4 class="mb-0">
			<a data-toggle="collapse" href="#collapse-${escapedName}">
				${name}
			</a>
		</h4>
	</div>
	<div id="collapse-${escapedName}" data-parent="#accordion" class="panel-collapse collapse ${openFirst ? 'show' : ''}">
		<div class="ml-3">
			<div id="accordion-${escapedName}">
				<div class="card border-bottom-0 border-right-0 border-top-0 rounded-0">
					<div class="card-header py-1">
						<h5 class="mb-0">
							<a data-toggle="collapse" data-parent="#accordion-${escapedName}" href="#collapse-${escapedName}-total">
								- Total
							</a>
						</h5>
					</div>
					<div id="collapse-${escapedName}-total" data-parent="#accordion-${escapedName}" class="panel-collapse collapse show">
						<div class="mx-1">
							<c:set var="wraper" value="${total}"/>
							<%@ include file="memoryUsage.jsp" %> 
						</div>
					</div>
				</div>
				<c:forEach items="${pools}" var="pool" varStatus="status">
					<div class="card border-bottom-0 border-right-0 border-top-0 rounded-0">
						<div class="card-header py-1 ${status.last ? 'border-bottom-0' : ''}">
							<h5 class="mb-0">
								<a data-toggle="collapse" data-parent="#accordion-${escapedName}" href="#collapse-${pool.escapedName}">
									- - ${pool.name}
								</a>
							</h5>
						</div>
						<div id="collapse-${pool.escapedName}" data-parent="#accordion-${escapedName}"class="panel-collapse collapse">
							<div class="mx-1">
								<c:set var="wraper" value="${pool.usage}"/>
								<%@ include file="memoryUsage.jsp" %>
							</div>
						</div>
					</div>
				</c:forEach>
			</div>
		</div>
	</div>
</div>
