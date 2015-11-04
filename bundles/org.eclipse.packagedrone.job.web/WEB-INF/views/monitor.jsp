<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="org.eclipse.packagedrone.job.JobHandle"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<% 
JobHandle job = (JobHandle)pageContext.getRequest ().getAttribute ( "job" );

if ( job != null && job.isComplete () && job.getError () == null )
    pageContext.setAttribute ( "type", "success");
else if ( job != null &&  job.isComplete (  ) && job.getError (   ) != null  )
    pageContext.setAttribute ( "type", "danger");
else
    pageContext.setAttribute ( "type", "default");
%>

<c:choose>
    <c:when test="${empty job }">Job not found!</c:when>
    <c:otherwise>

		<div class="panel panel-${type }">
		    <div class="panel-heading">
		        <%-- when we upgrade to font awesome 4.3, we can use fp-pulse again --%>
		        <h4 class="panel-title"><c:if test="${not job.complete }"><i class="fa fa-spinner fa-spin"></i>${ ' ' }</c:if>${fn:escapeXml(job.label) }</h4>
		    </div>
		    
		    <div class="panel-body">
		    
		        <c:if test="${not empty job.currentWorkLabel }">
		          <p>${fn:escapeXml(job.currentWorkLabel) }</p>
		          <div class="progress">
		              <fmt:formatNumber var="percent" type="number" maxFractionDigits="1" minFractionDigits="1" value="${job.percentComplete*100.0}" />
		              <div class="progress-bar progress-bar-striped" role="progressbar" aria-valuenow="${percent }" aria-valuemin="0" aria-valuemax="100" style="width: ${percent}%;">
                          <span>${percent }%</span>
                      </div>
		          </div>
		        </c:if>
		    
		        <c:if test="${not empty errorFormatted }">
		            <pre>${fn:escapeXml(errorFormatted) }</pre>
		        </c:if>
		    </div>
		</div>
		
		<c:if test="${job.complete }">
			<script type="text/javascript">
			$('#job-${job.id}').trigger ('job.complete', {
				error:${job.error ne null},
			} );
			</script>
		</c:if>
		<c:if test="${not job.complete }">
			<script type="text/javascript">
			triggerReload ();
			</script>
		</c:if>

    </c:otherwise>
</c:choose>
