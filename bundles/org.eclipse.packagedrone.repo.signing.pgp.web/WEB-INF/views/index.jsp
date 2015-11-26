<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="org.eclipse.packagedrone.repo.signing.pgp.web.ServiceManager"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<%
pageContext.setAttribute ( "TAG", ServiceManager.ACTION_TAG_PGP );
%>

<h:main title="PGP Signing">

<h:buttonbar menu="${menuManager.getActions(TAG) }" />

<div class="container-fluid ">

<div class="row">

<div class="col-sm-8">

<table class="table tablue-responsive">
    <thead>
        <tr>
            <th>ID</th>
            <th>Label</th>
            <th>Keyring</th>
            <th>Key ID</th>
            <th>State</th>
            <th></th>
        </tr>
    </thead>
    
    <tbody>
    <c:forEach var="entry" items="${services }">
        <tr>
            <td>${fn:escapeXml(entry.id) }</td>
            <td>${fn:escapeXml(entry.label) }</td>
            <td>${fn:escapeXml(entry.keyring) }</td>
            <td>${fn:escapeXml(entry.keyId) }</td>
            <td>${fn:escapeXml(entry.servicePresent) }</td>
            <td>
                <a class="btn btn-danger" href="<c:url value="/pgp.sign/${fn:escapeXml(entry.id) }/delete"/>"><span class="glyphicon glyphicon-trash"></span></a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

</div> <%-- col --%>

</div> <%-- row --%>

</div> <%-- container --%>


</h:main>