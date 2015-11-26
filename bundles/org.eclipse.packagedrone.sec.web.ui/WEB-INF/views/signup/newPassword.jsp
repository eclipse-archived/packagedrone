<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<h:main title="Set new password">

<div class="container-fluid">

<div class="row">

	<div class="col-md-10 col-lg-9">

        <c:url var="action" value="/signup/newPassword" />
	    <form:form action="${action }" method="POST"  cssClass="form-horizontal">
	    
	        <form:hidden path="token"/>
	    	   
		    <h:formEntry label="E-Mail"  command="command" path="email">
		        <form:input path="email" cssClass="form-control" type="email" readonly="true"/>
		    </h:formEntry>
		    
		    <h:formEntry label="Password"  command="command" path="password">
		        <form:input path="password" cssClass="form-control" type="password"/>
		    </h:formEntry>
		    
		    <h:formEntry label="Password (repeat)"  command="command" path="passwordRepeat">
		        <form:input path="passwordRepeat" cssClass="form-control" type="password"/>
		    </h:formEntry>
		    
			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-10">
					<button type="submit" class="btn btn-primary">Change</button>
				</div>
			</div>
			
	    </form:form>
	    
	</div>
	
</div></div>



</h:main>