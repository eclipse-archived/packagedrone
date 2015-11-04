<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="Import all channels">

<div class="container-fluid">

	<div class="row">
	
		<div class="col-sm-6">
		
			<form:form action="" method="POST" cssClass="form-horizontal" enctype="multipart/form-data">
			    
			    <fieldset>
			    
			        <legend>Import source</legend>
			        
				    <h:formEntry label="Upload" command="command" path="file">
				        <form:input path="file" type="file"/>
				        <span class="help-block">
	                    Select the channel export file to import. This must be a channel file previously exported by Package Drone.
				        </span>
				    </h:formEntry>
				    
				    <div class="form-group text-center"><em>or</em></div>
				    
				    <h:formEntry label="Server file" command="command" path="location">
	                    <form:input path="location" cssClass="form-control" type="text"/>
	                    <span class="help-block">
	                    Server side file to import. Instead of selecting one for upload.
	                    </span>
	                </h:formEntry>
                
                </fieldset>
                
                <fieldset>
                
                    <legend>Import options</legend>
			    
	                <h:formCheckbox label="Use channel names" path="useNames" command="command">
	                    <span class="help-block">
	                    This will set the names (channel alias) of the exported channels. Since channel names are unique in the system
	                    this will only work, if neither of the imported channel names exists.
	                    </span>
	                </h:formCheckbox>
	                
	                <div class="text-danger">
	                <h:formCheckbox label="Wipe storage before import" path="wipe" command="command">
	                    <span class="help-block">
	                    This will <em>delete all channels</em> before importing. All channels <em>will be lost</em> unless you do have
	                    a separate backup! 
	                    </span>
	                </h:formCheckbox>
	                </div>
                
                </fieldset>
			    
				<h:formButtons>
			        <input type="submit" value="Import" class="btn btn-primary">
			    </h:formButtons>
			
			</form:form>
		</div>
	</div>

</div>

</h:main>