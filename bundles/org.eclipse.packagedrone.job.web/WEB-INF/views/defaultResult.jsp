<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="org.eclipse.packagedrone.job.JobHandle"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<% 
JobHandle job = (JobHandle)pageContext.getRequest ().getAttribute ( "job" );

if ( job != null && job.isComplete () && job.getError () == null ) {
    pageContext.setAttribute ( "type", "success");
} else if ( job != null &&  job.isComplete (  ) && job.getError (   ) != null  ) {
    pageContext.setAttribute ( "type", "danger");
    pageContext.setAttribute ( "failed", true );
} else {
    pageContext.setAttribute ( "type", "default");
}

%>

<h:main title="Job ${ failed ? 'failed' : 'complete' }" subtitle="${fn:escapeXml(job.label) }">

<div class="container">

	<div class="row">
	
		<div class="${ failed ? 'col-sm-12' : 'col-sm-offset-2 col-sm-8' }">
		
			<c:choose>
			    <c:when test="${empty job }">
			         <div class="alert alert-danger"><strong>Job not found!</strong> The job <q>${fn:escapeXml(jobId) }</q> could not be found.</div>
			    </c:when>
			    
			    <c:when test="${not failed }">
                    <div class="alert alert-success"><strong>Job complete!</strong> The job <q>${fn:escapeXml(job.label) }</q> has been completed successfully.</div>
			    </c:when>
			    
			    <c:otherwise>
			
					<div class="panel panel-${type }">
					    <div class="panel-heading">
					        <h4 class="panel-title">${fn:escapeXml(job.error.message) }</h4>
					    </div>
					    
					    <div class="panel-body">
                            <pre>${fn:escapeXml(job.error.formatted) }</pre>
					    </div>
					</div>
			
			    </c:otherwise>
			</c:choose>
		
		</div>
	</div>

</div>

</h:main>
