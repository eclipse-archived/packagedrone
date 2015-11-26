<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<h:main title="View User" subtitle="${empty user.details.name ? user.details.email : user.details.name }">

<h:buttonbar menu="${menuManager.getActions(user) }" />

<c:set var="gravatar" value="${web:gravatar(user.details.email) }"/>

<div class="container-fluid">

    <div class="row">

        <div class="col-md-12">
			<h2>
				<c:if test="${not empty gravatar }"><img class="gravatar" src="https://secure.gravatar.com/avatar/${gravatar }.jpg?s=48"/></c:if>
				${fn:escapeXml(user.details.name)}
				<small>
					<c:if test="${you }">(This is you!)</c:if>
					<c:if test="${user.details.deleted }"><span class="label label-default">Deleted</span></c:if>
					<c:if test="${user.details.locked }"><span class="label label-warning">Locked</span></c:if>
					<c:forEach var="role" items="${user.roles }">
					   <span class="label label-default">${role }</span>&nbsp;
					</c:forEach>
				</small>
			</h2>
			
			<dl class="dl-horizontal details">
			    
			    <dt>ID</dt>
			    <dd id="userId">${fn:escapeXml(user.id) }</dd>
			    
			    <dt>Name</dt>
			    <dd>${fn:escapeXml(user.details.name) }</dd>
			    
			    <dt>E-Mail</dt>
			    <dd>${fn:escapeXml(user.details.email) }
				    <small>
				    <c:if test="${user.details.emailVerified }">&nbsp;<span class="label label-success">Verified</span></c:if>
				    <c:if test="${not user.details.emailVerified }">&nbsp;<span class="label label-warning">Not Verified</span></c:if>
				    </small>
			    </dd>
			    
			    <dt><dt>
			    <dd>
			        <c:if test="${you or pageContext.request.isUserInRole('ADMIN') }">
                        <a href="<c:url value="/user/${user.id }/newPassword"/>">Change password</a>
                    </c:if>
			    </dd>
			</dl>
		</div>

    </div>
    
    <div class="row">
    
        <div class="col-md-6">

			<h3 class="details-heading">Registration</h3>
			
			<dl class="dl-horizontal details">
			    <dt>Registration Date</dt>
			    <dd><fmt:formatDate value="${user.details.registrationDate }" type="both" /></dd>
			    
			    <dt>E-Mail Token Date</dt>
			    <dd><fmt:formatDate value="${user.details.emailTokenDate }" type="both" /></dd>
			    
			</dl>
		</div>
    </div>

</div>

</h:main>