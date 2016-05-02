<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>

<%@ page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>

<h:main title="Edit user" subtitle="${empty user.details.name ? user.details.email : user.details.name }">

<h:breadcrumbs/>

<style>
<!--
fieldset {
    padding: 1em;
}
-->
</style>

<div class="container-fluid">
	<form:form action="" method="POST"  cssClass="form-horizontal">
	   
	    <div class="col-md-6">
            <div class="row">
                <fieldset>
                    <legend>User details</legend>
       
				    <h:formEntry label="E-Mail"  command="command" path="email">
				        <form:input path="email" cssClass="form-control" type="email"/>
				    </h:formEntry>
				    
				    <h:formEntry label="Real Name"  command="command" path="name">
				        <form:input path="name" cssClass="form-control" placeholder="Optional real name"/>
				    </h:formEntry>
			    </fieldset>
		    </div>
        </div>
        
        <div class="col-md-6">
            <div class="row">
                <fieldset>
                    <legend>Security</legend>
                    <h:formEntry label="Roles" path="roles" command="command">
                        <div id="currentRoles">
		                    <c:forEach var="role" items="${allRoles }">
		                       <div class="checkbox">
		                           <label><input type="checkbox" name="roles" value="${fn:escapeXml(role) }" ${user.roles.contains(role) ? 'checked' : ''}/> ${fn:escapeXml(role) }</label>
		                       </div>
		                    </c:forEach>
	                    </div>
                    </h:formEntry>
                    
                    <h:formEntry>
                        <div class="input-group">
                            <input class="form-control" type="text" id="newRole" placeholder="Add new role" oninput="validateForm();"/>
                            <span class="input-group-btn">
                                <button class="btn btn-default" type="button" id="btnNewRole" onclick="addRole();" disabled="disabled"><span class="glyphicon glyphicon-plus"></span></button>
                            </span>
                        </div>
                    </h:formEntry>
                    
                </fieldset>
            </div>
        </div>
        
        <div class="row">
			<div class="col-md-12" >
				<input type="submit" value="Submit" class="btn btn-primary">
				<input type="reset" value="Reset" class="btn btn-default">
			</div>
	    </div>

		</form:form>
</div>

<script type="text/javascript">

function validateForm() {
	$('#btnNewRole').prop("disabled", !shouldEnable());
}

function shouldEnable () {
	var val = $('#newRole').val ();
	if ( val == "" ) {
		return false;	
	}
	
	return true;
}

validateForm ();

function addRole() {
    var val = $('#newRole').val ();
    
    var act = $("#currentRoles input[name='roles'][value='" + val + "']");
    
    if ( act.length > 0 ) {
    	console.log ( act[0] );
    	act[0].checked = true;
    } else {
        var ca = $('#currentRoles');
	    
	    var div = document.createElement ( "div" );
	    div.setAttribute ( "class", "checkbox" );
	    
	    var label = document.createElement ( "label" );
	    div.appendChild(label);
	    
	    var input = document.createElement ( "input");
	    label.appendChild ( input );
	    
	    input.setAttribute ( "type", "checkbox" );
	    input.setAttribute ( "checked", "checked" );
	    input.setAttribute ( "name", "roles" );
	    input.setAttribute ( "value", val );
	    
	    label.appendChild ( document.createTextNode( " " + val ) );
	    
	    ca.append ( div );
    }
    
    //clear entry box
    $('#newRole').val ( "" );
    validateForm ();
}
</script>

</h:main>