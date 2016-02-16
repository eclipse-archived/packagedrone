<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>
  
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ taglib prefix="form" uri="http://eclipse.org/packagedrone/web/form" %>

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>

<h:main title="HTTP endpoint" subtitle="Configure">

<script>
function generateId ( length ) {
	
	if ( typeof ( window.crypto ) != "undefined" && typeof(window.crypto.getRandomValues) == "function" ) {
		var rnd = new Uint8Array(Math.ceil(length/2));
		
		window.crypto.getRandomValues(rnd);
		
		var id = "";
		
		for ( var i = 0; i < rnd.length; i++ ) {
			var r = rnd[i];
			
			if ( r < 16 )
				id = id + "0";
			
			id = id + r.toString(16);
		}
		return id.substring(0,length); // cut off for odd numbers
	} else {
		var id = "";
		for ( var i = 0; i < length; i++ ) {
			var r = Math.floor ( Math.random() * 16 );
			id = id + r.toString(16);
		}
		return id;
	}
}

function randomName () {
	var id = generateId(16) + "-" + generateId(16) + "-"  + generateId(16) + "-" +  generateId(16);
	$('#endpoint').val(id);
}
</script>

<div class="container">
  <div class="row">
    <form:form method="POST" cssClass="form-horizontal">
    
      <h:formEntry label="Endpoint" path="endpoint" command="command">
        <div class="input-group">
          <span class="input-group-addon">${ fn:escapeXml(sitePrefix) }/trigger/http.endpoint/</span>
          <form:input path="endpoint" placeholder="myTrigger" cssClass="form-control" />
        </div>
        <span class="help-block">
            The suffix of the HTTP endpoint
        </span>
      </h:formEntry>
      
      <h:formButtons>
        <div class="alert alert-info">
        <strong>Remember! </strong> This URL will be publicly accessible.
        If you want to prevent anonymous access to this trigger
        <a href="#" class="alert-link" onclick="randomName(); return false;">choose an unguessable name</a>. 
        </div>
      </h:formButtons>
      
      <h:formButtons>
        <button type="submit" class="btn btn-primary">Apply</button>
        <button type="reset" class="btn btn-default">Reset</button>
        <a href="/trigger/channel/${fn:escapeXml(channelId) }/list" class="btn btn-default">Cancel</a>
      </h:formButtons>
    </form:form>
    
  </div>
</div>

</h:main>
