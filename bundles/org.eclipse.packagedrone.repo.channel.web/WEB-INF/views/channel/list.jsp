<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    trimDirectiveWhitespaces="true"
    %>

<%@ page import="org.eclipse.packagedrone.repo.channel.web.Tags"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="storage" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>

<%
pageContext.setAttribute ( "TAG", Tags.ACTION_TAG_CHANNELS );
%>

<h:main title="Channels">

<h:buttonbar menu="${menuManager.getActions(TAG) }" />

<div class="table-responsive">

<table class="table table-striped table-hover" style="width: 100%" id="channels">

<thead>
	<tr>
    	<th>Name</th>
    	<th>Description</th>
    	<th>#</th>
    	<th>Size</th>
    	<th>ID</th>
	</tr>
</thead>

<tbody>
	<c:forEach items="${channels}" var="channel">
        <%-- the next call to "get" is required since jasper seems to have issues with Java 8 default methods --%>
		<tr class="${storage:severityWithDefault(channel.state.getOverallValidationState(), '') }">
		    <td class="channel-name"><a href="<c:url value="/channel/${channel.id }/view"/>">${channel.name }</a></td>
		    <td class="channel-description">${fn:escapeXml(channel.state.description) }</td>
		    <td class="channel-count">${channel.state.numberOfArtifacts }</td>
		    <td class="channel-size"><web:bytes amount="${channel.state.numberOfBytes }"/></td>
		    <td class="channel-id"><a href="<c:url value="/channel/${channel.id }/view"/>">${channel.id }</a></td>
		</tr>
	</c:forEach>
</tbody>

</table>

</div>

</h:main>