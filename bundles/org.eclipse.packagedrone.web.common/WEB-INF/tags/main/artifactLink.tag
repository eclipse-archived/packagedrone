<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="scriptless"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%@attribute name="artifactId" required="true" %>
<%@attribute name="url" required="true" %>

<c:if test="${not empty url }">

<c:choose>

<c:when test="${ fn:startsWith(url,'http://') or fn:startsWith(url,'https://') }">
<a href="${fn:escapeXml(url) }" target="_blank"><jsp:doBody/>${' ' }<span class="glyphicon glyphicon-link"></span></a>
</c:when>

<c:otherwise>
<a href="/unzip/artifact/${artifactId }/${fn:escapeXml(url) }" target="_blank"><jsp:doBody/></a>
</c:otherwise>

</c:choose>

</c:if>