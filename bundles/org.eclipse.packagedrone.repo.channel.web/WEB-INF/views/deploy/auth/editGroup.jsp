<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="Edit group" subtitle="${fn:escapeXml( (empty command.name) ? command.id : command.name ) }">

<h:breadcrumbs/>

<div class="container-fluid">

<div class="row">

    <div class="col-md-10 col-lg-9">

        <form:form action="" method="POST"  cssClass="form-horizontal">
        
            <h:formEntry label="ID"  command="command" path="id">
                <form:input path="id" cssClass="form-control" disabled="true"/>
            </h:formEntry>
        
            <h:formEntry label="Name"  command="command" path="name">
                <form:input path="name" cssClass="form-control"/>
            </h:formEntry>
            
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <button type="submit" class="btn btn-primary">Update</button>
                    <button type="reset" class="btn btn-default">Reset</button>
                </div>
            </div>
            
        </form:form>
        
    </div>
    
</div></div>



</h:main>