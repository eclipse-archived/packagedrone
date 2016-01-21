<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="storage" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/storage" %>

<%
pageContext.setAttribute ( "admin", request.isUserInRole ( "ADMIN" ) );
%>

<h:main title="Channel" subtitle="${storage:channel(channel) }">

<jsp:attribute name="subtitleHtml"><s:channelSubtitle channel="${channel }" /></jsp:attribute>

<jsp:body>

  <h:buttonbar menu="${menuManager.getActions(channel) }" />
  <h:nav menu="${menuManager.getViews(channel) }"/>
  
  <div class="container-fluid">
  
  	<div class="well well-lg" style="margin-top: 1em;">
  	    <h2>Too many artifacts…</h2>
  	    <p>
  	    This view contains too many artifacts (<fmt:formatNumber value="${numberOfArtifacts }" />) to view.
  	    </p>
  	    <c:if test="${admin }">
  	    <p>
  	    The limit is set to <fmt:formatNumber value="${maxNumberOfArtifacts }" />. It can be changed by setting the system property <code>${fn:escapeXml(propertyName) }</code> to a different value.
  	    </p>
  	    </c:if>
  	</div>
  
  </div>
</jsp:body>
</h:main>