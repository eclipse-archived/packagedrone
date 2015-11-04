<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="Change password">

<div class="container-fluid">

<div class="row">

	<div class="col-md-10 col-lg-9">

	    <form:form action="" method="POST"  cssClass="form-horizontal">
	    	   
		    <h:formEntry label="E-Mail"  command="command" path="email">
		        <form:input path="email" cssClass="form-control" type="email" readonly="true"/>
		    </h:formEntry>
		    
		    <c:if test="${you}">
		    <h:formEntry label="Current password"  command="command" path="currentPassword">
                <form:input path="currentPassword" cssClass="form-control" type="password"/>
            </h:formEntry>
            </c:if>
		    
		    <h:formEntry label="New password"  command="command" path="password">
		        <form:input path="password" cssClass="form-control" type="password"/>
		    </h:formEntry>
		    
		    <h:formEntry label="New password (repeat)"  command="command" path="passwordRepeat">
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