<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>

<h:main title="Create generated P2 category artifact">

<h:buttonbar>
    <jsp:attribute name="before">
        <div class="btn-group" role="group"><a class="btn btn-default" href="/channel/${channelId }/view">Cancel</a></div>
    </jsp:attribute>
</h:buttonbar>

<h:genBlock>


	<form:form action="" method="POST" cssClass="form-horizontal">
		<fieldset>
			<legend>Create channel P2 category</legend>

            <h:formEntry label="Category ID" path="id" command="command">
                <form:input path="id" cssClass="form-control"/>
            </h:formEntry>

            <h:formEntry label="Name" path="name" command="command">
                <form:input path="name" cssClass="form-control"/> 
            </h:formEntry>

            <h:formEntry label="Description" path="description" command="command">
                <form:textarea path="description" cssClass="form-control"/> 
            </h:formEntry> 
            
			<button type="submit" class="btn btn-primary">Create</button>
		</fieldset>
	</form:form>

</h:genBlock>

</h:main>