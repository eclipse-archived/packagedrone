<%@ tag
  language="java"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  body-content="empty"
  %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="storage" %>

<%@ attribute name="messages" required="true" type="java.util.Collection" %>
<%@ attribute name="channelId" required="true" type="java.lang.String" %>

<table id="messages" class="table table-striped table-hover">
    
    <thead>
        <tr>
            <th>Message</th>
            <th>Source</th>
            <th>Artifacts</th>
        </tr>
    </thead>
    
    <tbody>
    <c:forEach items="${messages }" var="msg">
        <tr class="${storage:severity(msg.severity) }">
            <td>${fn:escapeXml(msg.message) }</td>
            <td nowrap="nowrap">${fn:escapeXml( aspects[msg.aspectId].label ) }</td>
            <td nowrap="nowrap">
            <ul>
                <c:forEach items="${msg.artifactIds }" var="id"><li><a href="<c:url value="/channel/${fn:escapeXml(channelId)}/artifacts/${id}/view"/>">${id }</a></li></c:forEach>
            </ul>
            </td>
        </tr>
    </c:forEach>
    </tbody>
    
</table>