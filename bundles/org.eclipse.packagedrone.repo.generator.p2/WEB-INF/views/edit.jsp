<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form"%>

<h:main title="Edit generated P2 feature artifact">

<h:buttonbar>
    <jsp:attribute name="before">
        <div class="btn-group" role="group"><a class="btn btn-default" href="/channel/${channelId }/view">Cancel</a></div>
    </jsp:attribute>
</h:buttonbar>

<div class="container form-padding">
<form:form action="" method="POST" cssClass="form-horizontal">
	<fieldset>
		<legend>Core information</legend>
    
        <h:formEntry label="Feature ID" path="id" command="command">
            <form:input path="id" cssClass="form-control"/>
        </h:formEntry>
		
        <h:formEntry label="Feature Version" path="version" command="command">
            <form:input path="version" cssClass="form-control"/>
            <span class="help-block">
                A valid version string. The qualifier <q>.qualifier</q> will be replaced with the current timesstamp.
            </span> 
        </h:formEntry>
        
        <h:formEntry label="Label" path="label" command="command">
            <form:input path="label" cssClass="form-control"/> 
        </h:formEntry>
        
        </fieldset>
        
        <fieldset>
          <legend>Filter</legend>
          <h:formEntry label="Bundle filter" path="symbolicNamePattern" command="command">
              <form:input path="symbolicNamePattern" cssClass="form-control"/>
              <span class="help-block">
                  A pattern for matching the <code>Bundle-SymbolicName</code>.
                  If set, then only bundles matching the pattern will be added. If left empty, all bundles will be added.
                  Use <code>%</code> for matching zero or more of any character, <code>_</code> to match any single character and <code>\</code> for escaping.</span>
              <span class="help-block"><strong>Example:</strong> <code>org.eclipse.%</code> will match all bundles whose symbolic name start with <code>org.eclipse.</code>, but not <code>org.eclipse</code> itself.</span>
          </h:formEntry>
        </fieldset>

        <fieldset>
          <legend>Additional metadata</legend>
                        
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
          
          <h:formButtons>
            <button type="submit" class="btn btn-primary">Update</button>
  		    <button type="reset" class="btn btn-default">Reset</button>
          </h:formButtons>
            
	</fieldset>
</form:form>
</div>

</h:main>