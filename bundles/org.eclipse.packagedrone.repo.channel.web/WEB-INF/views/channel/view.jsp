<%@ page language="java"
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

<%@ taglib prefix="table" uri="http://eclipse.org/packagedrone/web/common/table" %>

<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<h:main title="Channel" subtitle="${storage:channel(channel) }">

<jsp:attribute name="subtitleHtml">
	<span class="label label-primary">${fn:escapeXml(channel.id) }</span>
	<c:forEach var="name" items="${web:sort(channel.names)}">
	<span class="label label-default">${fn:escapeXml(name) }</span>
	</c:forEach>
	<c:if test="${not empty channel.description }">
	${ ' ' }${fn:escapeXml(channel.description)}
	</c:if>
</jsp:attribute>

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

<table:extender id="artifacts.list" tags="artifacts">
<table id="artifacts" class="table table-striped table-condensed table-hover">

<thead>
    <tr>
        <th>Name</th>
        <th>Size</th>
        <th>Created</th>
        <table:columns end="0" var="col"><th id="col-${col.id }" title="${fn:escapeXml(col.description) }">${fn:escapeXml(col.label) }</th></table:columns>
        <th></th>
        <th></th>
        <th></th>
        <th></th>
        <table:columns start="0" var="col"><th id="col-${col.id }" title="${fn:escapeXml(col.description) }">${fn:escapeXml(col.label) }</th></table:columns>
    </tr>
</thead>

<tbody>
<c:forEach items="${ sortedArtifacts }" var="artifact">
    <tr id="row-${artifact.id }" class="${storage:severityWithDefault(artifact.getOverallValidationState(), '') }">
        <td>${ fn:escapeXml(artifact.name) }</td>
        <td class="text-right"><web:bytes amount="${ artifact.size}"/></td>
        <td style="white-space: nowrap;"><fmt:formatDate value="${artifact.creationTimestamp }" type="both" /> </td>
        
        <%-- extensions - before 0 --%>
        
		<table:row end="0" item="${artifact }">
        	<td><table:extension/></td>
        </table:row>
        
        <%-- commands --%>
        
        <td><a href="<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/${ fn:escapeXml(artifact.id) }/get"/>">Download</a></td>
        <td>
          <c:if test='${artifact.is("stored") and manager}'><a href="<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/${ fn:escapeXml(artifact.id) }/delete"/>">Delete</a></c:if>
        </td>
        <td><a href="<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/${ fn:escapeXml(artifact.id) }/view"/>">Details</a></td>
        <td><a href="<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/${ fn:escapeXml(artifact.id) }/dump"/>">View</a></td>
        
        <%-- extensions - after 0 --%>
        
		<table:row start="0" item="${artifact }">
        	<td><table:extension/></td>
        </table:row>
    </tr>
</c:forEach>
</tbody>

</table>
</table:extender>

<s:dz_init/>

</jsp:attribute>

</h:main>