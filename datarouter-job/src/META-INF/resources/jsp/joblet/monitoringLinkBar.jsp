<%@ include file="/WEB-INF/prelude.jspf" %>

<a href="?submitAction=showJoblets" class="btn btn-mini">Joblets</a>
<a href="?submitAction=showQueues" class="btn btn-mini">JobletQueues</a>
<a href="?submitAction=showThreads" class="btn btn-mini">Joblet Threads</a>
<a href="?submitAction=listExceptions" class="btn btn-mini">Joblet Exceptions</a>
<br/>
<a href="/analytics/counters?submitAction=listCounters&webApps=All&servers=ALL&periods=300000&archive=databean+300000&nameLike=Joblet+queue+length" class="btn btn-mini">Queue Length Counts</a>
<a href="/analytics/counters?submitAction=listCounters&webApps=All&servers=ALL&periods=300000&archive=databean+300000&nameLike=Joblet+items+processed" class="btn btn-mini">Items Processed Counts</a>
<a href="/analytics/counters?submitAction=listCounters&webApps=All&servers=ALL&periods=300000&archive=databean+300000&nameLike=Joblet+tasks+processed" class="btn btn-mini">Tasks Processed Counts</a>
<a href="/analytics/counters?submitAction=listCounters&webApps=All&servers=ALL&periods=300000&archive=databean+300000&nameLike=Joblet+first+created" class="btn btn-mini">First Created Counts</a>
<br/>
<a href="/analytics/counters?counters=Joblet%20target%20servers&submitAction=viewCounters&webApps=All&servers=All&periods=300000&frequency=second&rollPeriod=1" class="btn btn-mini">Target Servers Counts</a>
<a href="/analytics/counters?counters=Joblet%20num%20servers&submitAction=viewCounters&webApps=All&servers=All&periods=300000&frequency=second&rollPeriod=1" class="btn btn-mini">Num Servers Counts</a>
