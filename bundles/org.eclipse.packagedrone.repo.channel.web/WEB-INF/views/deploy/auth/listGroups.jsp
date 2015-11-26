<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="org.eclipse.packagedrone.repo.channel.web.deploy.DeployAuthController"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
pageContext.setAttribute ( "TAG", DeployAuthController.GROUP_ACTION_TAG );
%>

<h:main title="Deploy Groups">

<h:buttonbar menu="${menuManager.getActions(TAG) }" />

<div class="table-responsive">

<table class="table table-condensed table-striped table-hover">

<thead>
    <tr>
        <th>Name</th>
        <th>ID</th>
        <th>Keys</th>
    </tr>
</thead>

<tbody>
    <c:forEach var="group" items="${groups.data }">
    <tr>
        <td>${fn:escapeXml( (group.name eq null ) ? group.id : group.name ) }</td>
        <td><a href="<c:url value="/deploy/auth/group/${group.id }/view"/>">${fn:escapeXml(group.id) }</a></td>
        <td>${group.keys.size() }</td>
    </tr>
    </c:forEach>    
</tbody>

</table>

<h:pager value="${groups }" />

</div>

</h:main>