<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<h:main title="Password reset">

<div class="container-fluid"><div class="row">

<div class="col-xs-offset-1 col-xs-10">

<c:choose>

	<c:when test="${not empty error }">
	
	   <div class="alert alert-danger">
            ${fn:escapeXml(error) }
        </div>
	
	</c:when>
	
	<c:otherwise>
	
		<div class="alert alert-success">
		    <strong>E-mail sent!</strong> The system sent you a password reset e-mail. Please <a class="alert-link" target="_blank" href="https://youtube.com">wait for it to
		    arrive</a> and also check you spam folder.
		</div>
		
		<div class="alert alert-info">
		    <strong>Please note</strong> that older password reset e-mails will not work anymore. Only the most reset link will work.
		</div>
	
	</c:otherwise>

</c:choose>

</div></div></div>

</h:main>