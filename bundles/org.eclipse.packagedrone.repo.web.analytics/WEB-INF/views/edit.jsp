<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<h:main title="Analytics" subtitle="Configuration">

<div class="container-fluid">

	<form:form action="" method="POST" cssClass="form-horizontal">
	    <h:formEntry label="Tracking ID" path="trackingId" optional="true" command="command">
	        <form:input path="trackingId" cssClass="form-control" placeholder="The tracking ID"/>
	    </h:formEntry>
	    <h:formCheckbox label="Anonymize IP" path="anonymizeIp" command="command">
	    </h:formCheckbox>
	    <h:formCheckbox label="Force SSL" path="forceSsl" command="command">
        </h:formCheckbox>
	    <h:formButtons>
	        <input type="submit" value="Update" class="btn btn-primary" />
	    </h:formButtons>
	</form:form>
    
</div>

</h:main>