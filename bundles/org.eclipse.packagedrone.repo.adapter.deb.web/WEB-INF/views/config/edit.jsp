<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="APT Repository Configuration" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<div class="container-fluid form-padding">

	 <form:form action="" method="POST" cssClass="form-horizontal">
	
	    <div class="row">
	    
	        <div class="col-md-6">
	        
	            <fieldset>
	               <legend>Distribution</legend>
	               
	               <h:formEntry label="Distribution" path="distribution" command="command">
	                   <form:input path="distribution" cssClass="form-control"/>
	               </h:formEntry>
	               
	               <h:formEntry label="Origin" path="origin" command="command">
                       <form:input path="origin" cssClass="form-control"/>
                   </h:formEntry>
                   
                   <h:formEntry label="Label" path="label" command="command">
                       <form:input path="label" cssClass="form-control"/>
                   </h:formEntry>
                   
                   <h:formEntry label="Version" path="version" command="command">
                       <form:input path="version" cssClass="form-control"/>
                   </h:formEntry>
                   
                    <h:formEntry label="Suite" path="suite" command="command">
                       <form:input path="suite" cssClass="form-control"/>
                   </h:formEntry>
                   
                   <h:formEntry label="Codename" path="codename" command="command">
                       <form:input path="codename" cssClass="form-control"/>
                   </h:formEntry>
                   
                    
                   <h:formEntry label="Description" path="description" command="command">
                       <form:input path="description" cssClass="form-control"/>
                   </h:formEntry>
	            
	            </fieldset>
	            
	        </div>
	        
	        <div class="col-md-6">
	        
	           <fieldset>
	               <legend>Content</legend>
	               
	               <h:formEntry label="Component" path="defaultComponent" command="command">
	                   <form:input path="defaultComponent" cssClass="form-control"/>
	               </h:formEntry>
	               
	               <%--
	               
	               TODO: re-enable when we have component selection
	               
	               <h:formEntry label="Components" path="components">
	                   <form:select path="components" cssClass="form-control" multiple="true">
	                       <form:option value="main"/>
	                   </form:select>
	               </h:formEntry>
	               
	               --%>
	               
	               <h:formEntry label="Architectures" path="architectures">
	                   <div id="currentArchs">
		                   <c:forEach var="arch" items="${command.architectures }">
		                       <div class="checkbox">
		                            <label><input type="checkbox" checked="checked" name="architectures" value="${fn:escapeXml(arch) }"/> ${fn:escapeXml(arch)}</label>
	                           </div>
	                       </c:forEach>
                       </div>
                   </h:formEntry>
                   
                   <h:formEntry label="" path="">
                        <div class="input-group">
                            <input type="text" id="newArch" class="form-control" placeholder="New architecture"/>
                            <span class="input-group-btn">
                                <button class="btn btn-default" type="button" onclick="addArch();"><span class="glyphicon glyphicon-plus"></span></button>
                            </span>
                       </div>
                   </h:formEntry>
	               
	           </fieldset>
	           
	           <fieldset>
                   <legend>Signing</legend>
                   
                    <h:formEntry label="Service" path="signingService">
                       <form:select path="signingService" cssClass="form-control">
                           <form:option value="" label="Don't sign"/>
                           <form:optionList items="${signingServices }" itemValue="id"/>
                       </form:select>
                   </h:formEntry>
               </fieldset>
	        
	        </div>
	    
	    </div>
	    
	    <div class="row">
            <div class="col-md-6">
	            <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">Update</button>
                        <button type="reset" class="btn btn-default">Reset</button>
                    </div>
                </div>
            </div> 
	    </div>
	    
	</form:form>    

</div>

<script type="text/javascript">
function addArch() {
	var val = $('#newArch').val ();
	var ca = $('#currentArchs');
	
	var div = document.createElement ( "div" );
	div.setAttribute ( "class", "checkbox" );
	
	var label = document.createElement ( "label" );
	div.appendChild(label);
	
	var input = document.createElement ( "input");
	label.appendChild ( input );
	
    input.setAttribute ( "type", "checkbox" );
    input.setAttribute ( "checked", "checked" );
    input.setAttribute ( "name", "architectures" );
    input.setAttribute ( "value", val );
	
	label.appendChild ( document.createTextNode( " " + val ) );
	
	ca.append ( div );
}
</script>

</h:main>