<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<h:main title="Re-Request E-Mail">

<div class="container-fluid">

<div class="row">

	<div class="col-md-10 col-lg-9">

        <form:form action="" method="POST"  cssClass="form-horizontal">

	        <legend>Enter the e-mail address for which you want to re-request the verification e-mail</legend>        
		   
		    <h:formEntry label="E-Mail"  command="command" path="email">
                <div class="input-group">
                    <span class="input-group-addon">@</span>
                    <form:input path="email" cssClass="form-control" type="email"/>
                </div>
		    </h:formEntry>
		    
	        <div class="form-group">
	            <div class="col-sm-offset-2 col-sm-10">
	                <button type="submit" class="btn btn-primary">Request E-Mail</button>
	            </div>
	        </div>
        
        </form:form>
	
	</div>
	
</div></div>



</h:main>