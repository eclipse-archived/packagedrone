<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="org.eclipse.packagedrone.repo.signing.pgp.web.ServiceManager"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<%
pageContext.setAttribute ( "TAG", ServiceManager.ACTION_TAG_PGP );
%>

<h:main title="PGP Signing" subtitle="Add new signer">

<div class="container-fluid ">

	<div class="row">
	
	    <div class="col-md-10 col-lg-9">
	
	    <form:form action="" method="POST"  cssClass="form-horizontal">
	    
	        <h:formEntry label="Label"  command="command" path="label">
                <form:input path="label" cssClass="form-control" type="text"/>
            </h:formEntry>
            
            <h:formEntry label="Keyring"  command="command" path="keyring">
                <form:input path="keyring" cssClass="form-control" type="text"/>
                <span class="help-block">
                The path (on the server) to the keyring file. The server needs to have at least
                read access to this file.
                </span>
            </h:formEntry>
            
            <h:formEntry label="Key ID"  command="command" path="keyId">
                <form:input path="keyId" cssClass="form-control" type="text"/>
            </h:formEntry>
            
            <h:formEntry label="Key Passphrase"  command="command" path="keyPassphrase">
                <form:input path="keyPassphrase" cssClass="form-control" type="password"/>
                <span class="help-block">
	              <strong>Note:</strong> The passphrase will be stored on the server in order to unlock the key.
	            </span>
            </h:formEntry>
            
            <h:formEntry label="" command="command" path="">
            </h:formEntry>

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <button type="submit" class="btn btn-primary">Add</button>
                    <a href="<c:url value="/pgp.sign"/>" class="btn btn-default">Cancel</a>
                </div>
            </div>
	    
	    </form:form>
	    
	    </div>
	    
    </div>

</div> <%-- container --%>


</h:main>