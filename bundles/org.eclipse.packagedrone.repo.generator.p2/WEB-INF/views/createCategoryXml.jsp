<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form"%>

<h:main title="Create generated P2 category artifact">

<h:buttonbar>
    <jsp:attribute name="before">
        <div class="btn-group" role="group"><a class="btn btn-default" href="/channel/${channelId }/view">Cancel</a></div>
    </jsp:attribute>
</h:buttonbar>

<h:genBlock>

	<form:form action="" method="POST" cssClass="form-horizontal" enctype="multipart/form-data">
		<fieldset>
			<legend>Create P2 categories based on an uploaded category.xml</legend>

            <h:formEntry label="Category XML file" path="file">
                <input type="file" name="file" id="file">
                <span class="help-block">
                Select and upload a <q>Category Definition</q> file (<code>category.xml</code>) which defines
                categories and assigned features.
                </span>
                <span class="help-block">
                Also see <a href="http://help.eclipse.org/luna/index.jsp?topic=%2Forg.eclipse.pde.doc.user%2Fguide%2Ftools%2Ffile_wizards%2Fnew_category.htm" target="_blank" >New Category Definition File</a>
                in the Eclipse Luna help.
                </span>
            </h:formEntry>
            
			<button type="submit" class="btn btn-primary">Create</button>
		</fieldset>
	</form:form>

</h:genBlock>

</h:main>