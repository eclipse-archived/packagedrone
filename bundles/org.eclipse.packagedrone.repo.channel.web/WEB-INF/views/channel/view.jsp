<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib tagdir="/WEB-INF/tags/storage" prefix="s" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="storage" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>

<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<h:main title="Channel" subtitle="${storage:channel(channel) }">

<jsp:attribute name="head">
<s:dz_head/>
</jsp:attribute>

<jsp:attribute name="body">

<h:buttonbar menu="${menuManager.getActions(channel) }">
    <jsp:attribute name="after">
        <c:if test="${manager }"><s:dz_button /></c:if>
    </jsp:attribute>
</h:buttonbar>

<s:dz_container/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<table id="artifacts" class="table table-striped table-condensed table-hover">

<thead>
    <tr>
        <th>Name</th>
        <th>Size</th>
        <th>Created</th>
        <th></th>
        <th></th>
        <th></th>
        <th></th>
    </tr>
</thead>

<tbody>
<c:forEach items="${ sortedArtifacts }" var="artifact">
    <tr id="row-${artifact.id }" class="${storage:severityWithDefault(artifact.getOverallValidationState(), '') }">
        <td>${ fn:escapeXml(artifact.name) }</td>
        <td class="text-right"><web:bytes amount="${ artifact.size}"/></td>
        <td style="white-space: nowrap;"><fmt:formatDate value="${artifact.creationTimestamp }" type="both" /> </td>
        <td><a href="<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/${ fn:escapeXml(artifact.id) }/get"/>">Download</a></td>
        <td>
          <c:if test='${artifact.is("stored") and manager}'><a href="<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/${ fn:escapeXml(artifact.id) }/delete"/>">Delete</a></c:if>
        </td>
        <td><a href="<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/${ fn:escapeXml(artifact.id) }/view"/>">Details</a></td>
        <td><a href="<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/${ fn:escapeXml(artifact.id) }/dump"/>">View</a></td>
    </tr>
</c:forEach>
</tbody>

</table>

<s:dz_init/>

</jsp:attribute>

</h:main>