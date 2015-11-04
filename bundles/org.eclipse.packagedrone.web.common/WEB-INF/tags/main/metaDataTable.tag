<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="empty"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@attribute name="metaData" required="true" type="java.util.Map"%>

<% jspContext.setAttribute("nl", "\n"); %>

<table class="table table-condensed">

<tr>
    <th>Namespace</th>
    <th>Key</th>
    <th>Value</th>
</tr>

<c:forEach items="${metaData }" var="entry">
    <tr>
        <td>${fn:escapeXml(entry.key.namespace) }</td>
        <td style="white-space: nowrap;">${fn:escapeXml(entry.key.key) }</td>
        <td>
            <c:choose>
                <c:when test="${fn:contains(entry.value, nl) }">
                    <pre>${fn:escapeXml(entry.value) }</pre>
                </c:when>
                <c:otherwise>
                    <c:choose>
                        <c:when test="${fn:length(entry.value) > 120 }">
                            <code>${fn:escapeXml(fn:substring(entry.value, 0, 120)) }</code>…
                        </c:when>
                        <c:otherwise>
                            <code>${fn:escapeXml(entry.value) }</code>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
            
        </td>
    </tr>
</c:forEach>

</table>