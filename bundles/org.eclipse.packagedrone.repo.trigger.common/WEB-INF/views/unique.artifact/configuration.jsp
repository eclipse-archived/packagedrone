<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>

<%@ page import="java.util.Arrays" %>
<%@ page import="org.eclipse.packagedrone.repo.channel.VetoPolicy" %>
  
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ taglib prefix="form" uri="http://eclipse.org/packagedrone/web/form" %>

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>

<%
pageContext.setAttribute ( "vetos", Arrays.asList ( VetoPolicy.values () ) );
%>

<h:main title="Unique artifact" subtitle="Configure">

<script>
function addTag ( value ) {
	if ( value == "" )
		return;
	
	var entry = $('<li class="drone-tag-list-item"><button type="button" class="close" data-dismiss="drone-tag" aria-label="Close"> <span aria-hidden="true">&times;</span></button></li>');
	
	var result = $('#keys').append (entry);
	
	entry.append ( document.createTextNode(value) );
	var input = $('<input type="hidden">');
	input.attr ("name", "keys");
	input.val ( value );
		
	entry.append ( input );
	
	$('[data-dismiss="drone-tag"]', entry).click(function(){
		droneTagDismissItem ( this );
	});
}
</script>

<div class="container">
  <div class="row">
    <form:form method="POST" cssClass="form-horizontal">
    
      <h:formEntry label="Artifact Key" path="keys" command="command">
      
        <ul id="keys" class="drone-tag-list">
          <form:inputList path="keys" var="key">
            <li class="drone-tag-list-item">
              <button type="button" class="close" data-dismiss="drone-tag" aria-label="Close"><span aria-hidden="true">&times;</span></button>
              ${key }<form:inputListValue />
            </li>
          </form:inputList>
        </ul>
       
        <div class="input-group">
          <input type="text" class="form-control" id="add-key">  
          <span class="input-group-btn">
            <button class="btn btn-default" onclick="var ele = $('#add-key'); addTag(ele.val()); ele.val('');" type="button"><span class="glyphicon glyphicon-plus"></span></button>
          </span>
        </div>
        
        
        <span class="help-block">
            The names of the attributes which will be used as key. One or more items in the form <code>namespace:key</code>.
        </span>
      </h:formEntry>
      
      <h:formEntry label="Unique attribute" path="uniqueAttribute" command="command">
        <form:input path="uniqueAttribute" cssClass="form-control"/>
        <span class="help-block">
            The name of the attribute which will be used as value. In the form <code>namespace:key</code>.
        </span>
      </h:formEntry>
      
      <h:formEntry label="Policy" path="vetoPolicy" command="command">
        <form:select path="vetoPolicy" multiple="false" cssClass="form-control">
          <form:optionList items="${vetos }"/>
        </form:select>
      </h:formEntry>
      
      <h:formButtons>
        <button type="submit" class="btn btn-primary">Apply</button>
        <button type="reset" class="btn btn-default">Reset</button>
        <a href="/trigger/channel/${fn:escapeXml(channelId) }/list" class="btn btn-default">Cancel</a>
      </h:formButtons>
    </form:form>
    
  </div>
</div>

</h:main>
