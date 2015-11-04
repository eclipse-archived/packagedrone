<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>


<h:main title="Add new user">

<h:breadcrumbs/>

<div class="container">

<div class="row">

<form:form action="" method="POST"  cssClass="form-horizontal">
    
    <h:formEntry label="E-Mail"  command="command" path="email">
        <form:input path="email" cssClass="form-control" type="email"/>
    </h:formEntry>
    
    <h:formEntry label="Real Name"  command="command" path="name">
        <form:input path="name" cssClass="form-control" placeholder="Optional real name"/>
    </h:formEntry>
    
    <form:errors path="" var="error">
        <div class="alert alert-danger">${fn:escapeXml(error.message) }</div>
    </form:errors>
    
    <input type="submit" value="Submit" class="btn btn-primary">
    <input type="reset" value="Reset" class="btn btn-default">

</form:form>

</div></div>

</h:main>