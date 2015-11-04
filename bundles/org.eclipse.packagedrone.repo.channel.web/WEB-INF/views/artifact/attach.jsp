<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<h:main title="Attach Artifact" subtitle="To ${fn:escapeXml(artifact.name) }">

<h:buttonbar>
    <jsp:attribute name="before">
        <div class="btn-group" role="group"><a class="btn btn-default" href="view">Back</a></div>
    </jsp:attribute>
</h:buttonbar>

<div class="container-fluid">

	<form method="post" action="" enctype="multipart/form-data" class="form-horizontal">
	    <fieldset>
	        <legend>Attach artifact</legend>
	        
	        <div class="form-group optional">
	            <label for="name" class="col-sm-2 control-label">File Name</label>
	            <div class="col-sm-10">
	                <input type="text" id="name" name="name" class="form-control" placeholder="Optional artifact name"/>
	            </div>
	        </div>
	        
	        <div class="form-group">
		        <label for="file" class="col-sm-2 control-label">File</label>
		        <div class="col-sm-10">
		           <input type="file" id="file" name="file"/>
		        </div>
	        </div>
	        
	        <button type="submit" class="btn btn-primary">Upload</button>
	    </fieldset>
	</form>

</div>

</h:main>