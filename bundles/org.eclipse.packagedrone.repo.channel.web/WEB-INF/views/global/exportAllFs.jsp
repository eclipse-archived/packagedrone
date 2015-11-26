<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<h:main title="Export all" subtitle="to local filesystem">

<div class="container-fluid">
    <div class="row">
        <div class="col-sm-6">
        
            <form:form action="" method="POST" cssClass="form-horizontal">
                
                <h:formEntry label="Location" command="command" path="location">
                    <form:input path="location" type="text" cssClass="form-control" placeholder="Location on server"/>
                    <span class="help-block">
                    The full location in server file system. The export process will create a file with exactly the same name.
                    If the file already exists, it will <strong>not</strong> be overwritten. The directory in which the file
                    will be created already has to exist, and the Package Drone server requires write access in this directory.
                    </span>
                </h:formEntry>
                
                <h:globalErrors/>
                
                <h:formButtons>
                    <input type="submit" value="Export all" class="btn btn-primary">
                </h:formButtons>
            
            </form:form>
        </div>
    </div>
</div>

</h:main>