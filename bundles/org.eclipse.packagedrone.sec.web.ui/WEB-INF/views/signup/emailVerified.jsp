<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<h:main title="E-Mail verified">

<div class="container-fluid"><div class="row">

<div class="col-xs-offset-1 col-xs-10">

	<div class="alert alert-success">
	<strong>E-Mail verified!</strong> Your e-mail address has been verified. Go ahead and
	<a href="<c:url value="/login"/>" class="alert-link">log in</a>.
	</div>

</div>

</div></div>

</h:main>