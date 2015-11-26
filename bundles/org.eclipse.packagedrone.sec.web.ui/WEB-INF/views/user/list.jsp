<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
pageContext.setAttribute ( "TAG", UserStorage.ACTION_TAG_USERS );
%>

<h:main title="Users">

<h:buttonbar menu="${menuManager.getActions(TAG) }" />

<div class="table-responsive">
	
	<table class="table table-condensed table-striped">
	
	<thead>
	    <tr>
	        <th>ID</th>
	        <th>Name</th>
	        <th>E-Mail <span class="glyphicon glyphicon-sort-by-attributes"></span></th>
	        <th></th>
	    </tr>
	</thead>
	
	<tbody>
	    <c:forEach var="user" items="${users.data }">
	    <tr
	    <c:if test="${ user.id eq pageContext.request.remoteUser }">class="info"</c:if>
	    >
	        <td><a href="<c:url value="/user/${user.id }/view"/>">${fn:escapeXml(user.id) }</a></td>
	        <td>${fn:escapeXml(user.details.name) } <c:if test="${ user.id eq pageContext.request.remoteUser }">&nbsp;<small>(you)</small></c:if></td>
	        <td>
	        ${fn:escapeXml(user.details.email) }</td>
	        <td>
	        <c:if test="${not empty user.details.email }">
	            <c:if test="${user.details.emailVerified }">&nbsp;<span class="label label-success">Verified</span></c:if>
	            <c:if test="${not user.details.emailVerified }">&nbsp;<span class="label label-warning">Not Verified</span></c:if>
	        </c:if>
	        <c:if test="${user.details.locked }">
	            &nbsp;<span class="label label-warning">Locked</span>
	        </c:if>
	        <c:if test="${user.details.deleted }">
	            &nbsp;<span class="label label-danger">Deleted</span>
	        </c:if>
	        </td>
	    </tr>
	    </c:forEach>    
	</tbody>
	
	</table>
</div>

<h:pager value="${users }" />

</h:main>