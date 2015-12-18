<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="org.eclipse.packagedrone.repo.signing.pgp.web.managed.ServiceController"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<%
pageContext.setAttribute ( "TAG", ServiceController.ACTION_TAG_PGP );
%>

<h:main title="PGP Signing" subtitle="Managed Keys">

<h:buttonbar menu="${menuManager.getActions(TAG) }" />

<h:pager value="${configurations }"/>

<div class="table-responsive">

<table class="table ">
    <thead>
        <tr>
            <th>ID</th>
            <th>Label</th>
            <th>State</th>
            <th></th>
        </tr>
    </thead>
    
    <tbody>
    <c:forEach var="entry" items="${configurations.data }"> 
        <tr class="${not empty entry.errorMessage ? 'danger' : '' }">
            <td>${fn:escapeXml(entry.id) }</td>
            <td>${fn:escapeXml(entry.label) }</td>
            <td>
            	${fn:escapeXml(entry.errorMessage) }
            	<ul>
            	<c:forEach var="key" items="${entry.keys }">
            		<li>${key.sub ? "sub" : "pub"} ${key.bits} bits / ${fn:escapeXml(key.shortId) }
            			<ul>
            				<c:forEach var="userId" items="${key.userIds }">
            					<li>${fn:escapeXml(userId) }</li>
            				</c:forEach>
            			</ul>
            		</li>
            	</c:forEach>
            	</ul>
            </td>
            <td>
                <a class="btn btn-danger" href="<c:url value="/pgp.sign.managed/${fn:escapeXml(entry.id) }/delete"/>"><span class="glyphicon glyphicon-trash"></span></a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

</div>

</h:main>