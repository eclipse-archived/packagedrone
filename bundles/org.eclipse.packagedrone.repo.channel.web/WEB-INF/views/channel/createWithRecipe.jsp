<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<h:main title="Create Channel" subtitle="with recipe">

<div class="container-fluid">
    
	<div class="row">
	
	    <form:form action="" method="POST" cssClass="form-horizontal">
	
		<div class="col-xs-12 col-md-6">
		
		    <h:formEntry label="Name" command="command" path="name">
		        <form:input path="name" cssClass="form-control" placeholder="Optional channel alias"/>
		    </h:formEntry>
		    
               <h:formEntry label="Description" command="command" path="description">
                   <form:textarea path="description" cssClass="form-control"/>
               </h:formEntry>
			
			<h:formButtons>
			    <input type="submit" value="Create" class="btn btn-primary">
			</h:formButtons>
			
			<input type="hidden" id="recipe" name="recipe"/>
				
		</div>
		

        <div class="col-xs-12 col-md-6">
            <div class="list-group">
                <a href="#" class="list-group-item recipe active">
                    <h4 class="list-group-item-heading">Plain channel</h4>
                    <p class="list-group-item-text">A plain channel, without any extras.</p>
                </a>
                
	            <c:forEach var="recipe" items="${recipes }">
	                <a href="#" class="list-group-item recipe" id="recipe-${fn:escapeXml(recipe.id) }" data-recipe="${fn:escapeXml(recipe.id) }">
	                   <h4 class="list-group-item-heading">${fn:escapeXml(recipe.label) }</h4>
	                   <p class="list-group-item-text">${fn:escapeXml(recipe.description) }</p>
	                </a>
	            </c:forEach>
            </div>
        </div>
        
        </form:form>

	</div>

</div>

<script type="text/javascript">
$("a.recipe").click(function () {
    $("a.recipe").removeClass("active");
	$(this).addClass("active");
	
	var id = $(this).data("recipe");
	$("#recipe").val(id);
});
</script>

</h:main>