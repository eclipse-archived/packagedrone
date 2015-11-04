<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/pm" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@attribute name="button" required="true" type="org.eclipse.packagedrone.web.common.Button"%>
<%@attribute name="type" required="false" type="java.lang.String"%>
<%@attribute name="href" required="false" type="java.lang.String"%>

<c:choose>
    <c:when test="${empty type }">
        <a role="button" href="${empty href ? '#' : href }" class="btn ${pm:modifier('btn-', button.modifier) }"><h:iconLabel label="${button.label }" icon="${button.icon }" /></a>
    </c:when>
    <c:otherwise>
        <button type="${fn:escapeXml( empty type ? 'button' : type) }" class="btn ${pm:modifier('btn-', button.modifier) }"><h:iconLabel label="${button.label }" icon="${button.icon }" /></button>
    </c:otherwise>
</c:choose>
