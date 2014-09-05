<div class="panel panel-default">
<div class="panel-heading">
	<h4 class="panel-title">
		<a data-toggle="collapse" data-parent="#accordion" href="#collapse${escapedName}">
			${name}
		</a>
	</h4>
</div>
<div id="collapse${escapedName}" class="panel-collapse collapse ${defaultVisible}">
	<div class="panel-body">
		<div class="panel-group" id="accordion">
			<div class="panel panel-default">
				<div class="panel-heading">
					<h5 class="panel-title">
						<a data-toggle="collapse" data-parent="#accordion" href="#collapse${escapedName}Total">
							- Total
						</a>
					</h5>
				</div>
				<div id="collapse${escapedName}Total" class="panel-collapse collapse in">
					<div class="panel-body">
						<c:set var="wraper" value="${total}"/>
						<%@ include file="memoryUsage.jsp" %> 
					</div>
				</div>
			</div>
			<c:forEach items="${pools}" var="pool">
				<div class="panel panel-default">
					<div class="panel-heading">
						<h5 class="panel-title">
							<a data-toggle="collapse" data-parent="#accordion" href="#collapse${pool.escapedName}">
								- - ${pool.name}
							</a>
						</h5>
					</div>
					<div id="collapse${pool.escapedName}" class="panel-collapse collapse">
						<div class="panel-body">
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