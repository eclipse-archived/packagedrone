<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="org.eclipse.packagedrone.job.JobHandle"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/job" prefix="job" %>

<% 
JobHandle job = (JobHandle)pageContext.getRequest ().getAttribute ( "job" );

if ( job != null && job.isComplete () && job.getError () == null )
    pageContext.setAttribute ( "type", "success");
else if ( job != null &&  job.isComplete (  ) && job.getError (   ) != null  )
    pageContext.setAttribute ( "type", "danger");
else
    pageContext.setAttribute ( "type", "default");
%>

<h:main title="View Job" subtitle="${fn:escapeXml(job.label) }">

<div class="container">

	<div class="row">
		<div class="col-md-12">
		
			<c:choose>
			    <c:when test="${empty job }">Job not found!</c:when>
			    <c:otherwise>
			
					<job:monitor job="${job }"></job:monitor>
			
			    </c:otherwise>
			</c:choose>
		
		</div>
	</div>

</div>

</h:main>
