<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>

<h:main title="Import" subtitle="From a Maven repository">

<script type="text/javascript">
function doAction(action) {
	var form = $('#command');
	form.attr("action", action);
	form.submit();
	return false;
} 
</script>

<div class="container-fluid form-padding">

<div class="row">
    <div class="col-md-8">
    
        <form:form action="start" method="POST" cssClass="form-horizontal">
		
		    <h:formEntry label="Repository URL" path="url" command="command">
		        <form:input path="url" cssClass="form-control" placeholder="Leave empty for Maven Central"/>
		    </h:formEntry>
		    
            <h:formEntry label="Coordinates" path="coordinates" command="command">
                <form:input path="coordinates" cssClass="form-control"/>
                <span class="help-block">
                The Maven coordinates of the artifact in the form: <code>groupId:artifactId[:classifier]:version</code>
                </span>
            </h:formEntry>
            
            <h:formCheckbox label="Include sources" path="includeSources" command="command">
                <span class="help-block">Whether the source attachment should be imported as well.</span>
            </h:formCheckbox>
		    
		    <div class="form-group">
		        <div class="col-sm-offset-2 col-sm-10">
		            <button type="submit" class="btn btn-${ok ?  'default' : 'primary' }">Validate</button>
		            <c:if test="${ok }">
                        <button type="button" id="test" class="btn btn-primary" onclick="doAction('<c:url value="test"/>');">Test</button>
		            </c:if>
		            <button type="reset" class="btn btn-default">Reset</button>
		        </div>
		    </div>
		</form:form>
	
    </div>
    
    <div class="col-md-4">
        <div class="panel panel-info">
            <div class="panel-heading">
                <h4 class="panel-title"><span class="glyphicon glyphicon-info-sign"></span> Import from Maven repository</h4>
            </div>
            <div class="panel-body">
            This importer downloads the artifact content directly from an existing Maven 2 compatible repository.
            </div>
            <div class="panel-body">
            For this to work, the Package Drone server instance must have network access
            to this target resource.
            </div>
        </div>
    </div>
    
</div>

</div>

</h:main>