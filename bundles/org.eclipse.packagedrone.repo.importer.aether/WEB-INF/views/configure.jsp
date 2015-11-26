<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

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
		    
            <h:formEntry label="Dependencies" path="dependencies" command="command">
                <form:textarea path="dependencies" cssClass="form-control" rows="10" spellcheck="false" />
                <span class="help-block">
                
                <span class="pull-right"><a href="#" data-toggle="modal" data-target="#coordinatesHelp"><span class="glyphicon glyphicon-question-sign"></span></a></span>
                
                The list of Maven artifacts to import. This may be either in the for of coordinates  (<code>groupId:artifactId[:extension[:classifier]]:version</code>)
                or the <code>&lt;dependency&gt;</code> fragments.
                </span>
            </h:formEntry>
            
            <h:formCheckbox label="Resolve dependencies" path="resolveDependencies" command="command">
            </h:formCheckbox>
            
            <h:formCheckbox label="Include all optional" path="allOptional" command="command">
                <span class="help-block">Whether all optional dependencies should be considered.</span>
            </h:formCheckbox>
            
            <h:formCheckbox label="Include sources" path="includeSources" command="command" >
            </h:formCheckbox>

            <h:formCheckbox label="Include POM" path="includePoms" command="command" >
            </h:formCheckbox>

            <h:formCheckbox label="Include Javadoc" path="includeJavadoc" command="command" >
            </h:formCheckbox>
            
		    <div class="form-group">
		        <div class="col-sm-offset-2 col-sm-10">
		            <button type="submit" class="btn btn-${ok ?  'default' : 'primary' }">Validate</button>
		            <c:if test="${ok }">
                        <button type="button" id="test" class="btn btn-primary" onclick="doAction('<c:url value="test"/>');">Resolve</button>
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

<div class="modal" id="coordinatesHelp" tabindex="-1" role="dialog"
	aria-labelledby="coordinatesHelpLabel">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h3 class="modal-title" id="coordinatesHelpLabel">Maven coordinates</h3>
			</div>
			<div class="modal-body">
			<p>
			The field for the maven coordinates allows for different syntax variants of referencing maven artifacts. However is it not possible to mix
			the different variants in this field.
			</p>
			
			<h4>Plain coordinates</h4>
			<p>
			Maven artifacts are referenced in the form of <code>groupId:artifactId[:extension[:classifier]]:version</code>.
			While <code>extension</code> and <code>classifier</code> are optional, <code>version</code> is not optional and must
			be the exact version to import (<code>1.0.0.Final</code> is something different than <code>1.0.0</code>). 
			</p>
			<p>
			Multiple artifacts can be specified, one line each. Empty lines are ignored.
			</p>
			
			<h4>Dependency XML fragments</h4>
			<p>
			This format re-used the <code>&lt;dependency&gt;</code> tags from maven POM files.
			Multiple dependency tags can be used, the surrounding <code>&lt;dependencies&gt;</code>
			is optional.
			</p>
			<p>
			In addition to <code>&lt;groupId&gt;</code>, <code>&lt;artifactId&gt;</code> and <code>&lt;version&gt;</code>
			it is also possible to add <code>&lt;extension&gt;</code> and <code>&lt;classifier&gt;</code> as child element.
			</p>
			
			<p>
			See the following example:
			</p>
			
			<pre>&lt;dependency&gt;
    &lt;groupId&gt;foo&lt;/groupId&gt;
    &lt;artifactId&gt;bar&lt;/artifactId&gt;
    &lt;version&gt;1.2.0.Final&lt;/version&gt;
&lt;/dependency&gt;</pre>
			
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			</div>
		</div>
	</div>
</div>

</h:main>