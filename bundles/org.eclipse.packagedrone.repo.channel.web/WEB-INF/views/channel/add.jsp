<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>

<h:main title="Add artifact" subtitle="${pm:channel(channel) }">

<h:buttonbar>
    <jsp:attribute name="before">
        <div class="btn-group" role="group"><a class="btn btn-default" href="view">Cancel</a></div>
    </jsp:attribute>
</h:buttonbar>

<h:genBlock>

<form method="post" action="" enctype="multipart/form-data" class="">
    <fieldset>
        <legend>Upload artifact to channel</legend>
        
        <div class="form-group optional">
            <label class="control-label" for="name">File Name</label>
            <input type="text" id="name" name="name" class="form-control" placeholder="Optional alternate file name"/>
        </div>
        
        <div class="form-group">
            <label class="control-label" for="file">File</label>
            <input type="file" id="file" name="file"/>
        </div>
        
        <button type="submit" class="btn btn-primary">Upload</button>
    </fieldset>
</form>

</h:genBlock>

</h:main>