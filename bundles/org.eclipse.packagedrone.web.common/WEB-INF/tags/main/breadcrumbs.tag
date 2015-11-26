<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="empty"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel/web" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ attribute name="cssClass" type="java.lang.String"%>

<c:if test="${not empty breadcrumbs }">

<ol id="breadcrumbs" class="breadcrumb ${cssClass }">
    <c:forEach var="entry" items="${breadcrumbs.entries }" >
        <c:set var="active" value="${web:active(pageContext.request, entry.target) }"/>
        <c:choose>
            <c:when test="${entry.link and empty active }">
                <li><a href="${entry.target }">${fn:escapeXml(entry.label) }</a></li>
            </c:when>
            <c:otherwise>
                <li class="${active }">${fn:escapeXml(entry.label) }</li>
            </c:otherwise>
        </c:choose>
    </c:forEach>
</ol>

</c:if>