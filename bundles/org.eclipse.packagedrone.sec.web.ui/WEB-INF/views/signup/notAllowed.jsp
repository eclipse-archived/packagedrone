<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<h:main title="Signup">

<div class="container-fluid"><div class="row">

<div class="col-xs-offset-1 col-xs-10">

	<div class="alert alert-warning">
	<strong>No self registration!</strong>
	
		Self registration of accounts is disabled on this system.
		Please contact a system administrator or the appropriate person
		and ask for an account.
	</div>

</div>

</div></div>

</h:main>