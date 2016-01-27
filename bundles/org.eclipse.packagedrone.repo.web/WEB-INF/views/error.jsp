<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
pageContext.setAttribute ( "showStackTrace", Boolean.getBoolean ( "drone.showStackTrace" ) );
%>

<h:main title="Kaboom!" subtitle="Internal server error">

<c:choose>

    <c:when test="${showStackTrace and not empty result}">
    
	    <h:error title="${fn:escapeXml(result) }" icon="flash">
	       <p>${fn:escapeXml(message) }</p>
		      <pre>${fn:escapeXml(stacktrace) }</pre>
	    </h:error>
    
    </c:when>
    
    <c:when test="${showStackTrace and empty result }">
        <h:error title="${fn:escapeXml(message) }" icon="flash">
              <pre>${fn:escapeXml(stacktrace) }</pre>
        </h:error>
    </c:when>
    
    <c:otherwise>
        <div class="container">
            <div class="row">
                <div class="col-md-offset-2 col-md-8">
                    <div class="alert alert-danger"><c:if test="${not empty result }"><strong>${fn:escapeXml(result) }!</strong>${ ' ' }</c:if>${fn:escapeXml(message) }</div>
                </div>
            </div>
        </div>
    </c:otherwise>

</c:choose>

</h:main>