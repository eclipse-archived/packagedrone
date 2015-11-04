<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="Create" subtitle="Deploy group">

<h:breadcrumbs/>

<div class="container-fluid">

<div class="row">

    <div class="col-md-10 col-lg-9">

        <form:form action="" method="POST"  cssClass="form-horizontal">
        
            <h:formEntry label="Name"  command="command" path="name">
                <form:input path="name" cssClass="form-control"/>
            </h:formEntry>
            
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <button type="submit" class="btn btn-primary">Create</button>
                </div>
            </div>
            
        </form:form>
        
    </div>
    
</div></div>



</h:main>