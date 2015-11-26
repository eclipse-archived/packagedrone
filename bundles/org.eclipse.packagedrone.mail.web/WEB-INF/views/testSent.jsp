<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<h:main title="Test mail" subtitle="Result">

<div class="container-fluid"><div class="row">

<div class="col-xs-offset-1 col-xs-10">

	<div class="alert alert-success">
	    <strong>Mail sent!</strong> The test e-mail has been handed over to the first mail server.
	</div>
	
	<p class="text-muted">
        This does not guarantee a delivery, since other
        e-mails are passed from mail server to mail server until they reach
        their final destination. Any of those mail servers still can block
        this message.
    </p>

</div></div></div>

</h:main>