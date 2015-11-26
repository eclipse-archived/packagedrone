<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<h:main title="Reset password">

<form:form action="" method="POST"  cssClass="form-horizontal">

<div class="container-fluid">

<div class="row">

	<div class="col-md-10 col-lg-9">

        <legend>Enter the e-mail address for which you want to reset the password</legend>        
	   
	    <h:formEntry label="E-Mail"  command="command" path="email">
	        <form:input path="email" cssClass="form-control" type="email"/>
	    </h:formEntry>
	    
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <button type="submit" class="btn btn-primary">Reset password</button>
            </div>
        </div>
	
	</div>
	
</div></div>

</form:form>

</h:main>