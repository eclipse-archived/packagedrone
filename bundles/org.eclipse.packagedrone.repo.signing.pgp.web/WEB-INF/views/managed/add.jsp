<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="org.eclipse.packagedrone.repo.signing.pgp.web.ServiceManager"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<%
pageContext.setAttribute ( "TAG", ServiceManager.ACTION_TAG_PGP );
%>

<h:main title="Add key" subtitle="Import a new secret key">

<div class="container-fluid ">

	<div class="row">
	
	    <div class="col-md-10 col-lg-9">
	
	    <form:form action="" method="POST"  cssClass="form-horizontal">
	    
	        <h:formEntry label="Label"  command="command" path="label">
                <form:input path="label" cssClass="form-control" type="text" required="false"/>
            </h:formEntry>

            <h:formEntry label="Secret key"  command="command" path="secretKey">
                <form:textarea path="secretKey" cssClass="form-control" rows="25" />
                <span class="help-block">
               	This is the ASCII version of the secret key.
                </span>
            </h:formEntry>
            
	        <h:formEntry label="Passphrase"  command="command" path="passphrase">
                <form:input path="passphrase" cssClass="form-control" type="password"/>
            </h:formEntry>
            
            <h:formButtons>
        		<button type="submit" class="btn btn-primary">Add</button>
				<a href="<c:url value="/pgp.sign"/>" class="btn btn-default">Cancel</a>
            </h:formButtons>
	    
	    </form:form>
	    
	    </div>
	    
    </div>

</div> <%-- container --%>


</h:main>