<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>

<h:main title="Create generated P2 feature artifact">

<h:buttonbar>
    <jsp:attribute name="before">
        <div class="btn-group" role="group"><a class="btn btn-default" href="/channel/${channelId }/view">Cancel</a></div>
    </jsp:attribute>
</h:buttonbar>

<h:genBlock>

	<form:form action="" method="POST" cssClass="form-horizontal">
		<fieldset>
			<legend>Create channel P2 feature</legend>

            <h:formEntry label="Feature ID" path="id" command="command">
                <form:input path="id" cssClass="form-control"/>
            </h:formEntry>

            <h:formEntry label="Feature Version" path="version" command="command">
                <form:input path="version" cssClass="form-control"/>
                <span class="help-block">
                    A valid version string. The qualifier <code>.qualifier</code> will be replaced with the current timesstamp.
                </span>
            </h:formEntry>
            
            <h:formEntry label="Label" path="label" command="command">
                <form:input path="label" cssClass="form-control"/> 
            </h:formEntry>

            <h:formEntry label="Provider" path="provider" command="command">
                <form:input path="provider" cssClass="form-control"/> 
            </h:formEntry>            

	        <h:formEntry label="Description URL" path="descriptionUrl" command="command">
	            <form:input type="text" path="descriptionUrl" cssClass="form-control"/> 
	        </h:formEntry> 
	        
	        <h:formEntry label="Description" path="description" command="command">
	            <form:textarea path="description" cssClass="form-control"/> 
	        </h:formEntry>
	        
	        <h:formEntry label="Coypright URL" path="copyrightUrl" command="command">
	            <form:input type="text" path="copyrightUrl" cssClass="form-control"/> 
	        </h:formEntry> 
	        
	        <h:formEntry label="Copyright" path="copyright" command="command">
	            <form:textarea path="copyright" cssClass="form-control"/> 
	        </h:formEntry>
	        
	        <h:formEntry label="License URL" path="licenseUrl" command="command">
	            <form:input type="text" path="licenseUrl" cssClass="form-control"/> 
	        </h:formEntry> 
	        
	        <h:formEntry label="License" path="license" command="command">
	            <form:textarea path="license" cssClass="form-control"/> 
	        </h:formEntry>
            
			<button type="submit" class="btn btn-primary">Create</button>
		</fieldset>
	</form:form>

</h:genBlock>

</h:main>