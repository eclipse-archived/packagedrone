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
<%@ taglib prefix="json" uri="http://eclipse.org/packagedrone/web/json" %>

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>

<%
pageContext.setAttribute ( "vetos", Arrays.asList ( VetoPolicy.values () ) );
%>

<h:main title="Unique artifact" subtitle="Configure">

<div class="container">
  <div class="row">
    <form:form method="POST" cssClass="form-horizontal">
    
      <h:formEntry label="Artifact Key" path="keys" command="command" id="keys-group">
      
        <div class="drone-tag-list drone-tag-list-form" id="keys">
          <ul class="drone-tag-list-content"></ul>
          
          <div class="input-group">
            <input type="text" class="form-control" data-add="drone-tag">  
            <span class="input-group-btn">
              <button class="btn btn-default" data-add-trigger="drone-tag" type="button"><span class="glyphicon glyphicon-plus"></span></button>
            </span>
          </div>
        </div>
        
        <span class="help-block" id="keys-validation"></span>
        
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
      
      <h:formCheckbox label="Ignore artifacts with missing values" path="skipMissingAttributes" command="command">
        <span class="help-block">
          If this box is checked, then all artifacts which are missing missing any of the <q>artifact keys</q> are permitted to be uploaded.
        </span>
      </h:formCheckbox>
      
      <h:formButtons>
        <button type="submit" class="btn btn-primary">Apply</button>
        <button type="reset" class="btn btn-default">Reset</button>
        <a href="/trigger/channel/${fn:escapeXml(channelId) }/list" class="btn btn-default">Cancel</a>
      </h:formButtons>
    </form:form>
    
  </div>
</div>

<script type="text/javascript">

var keys = JSON.parse('${json:array(command.keys)}');
var metaKeyRE = /[a-zA-Z0-9]+\:[a-zA-Z0-9]+/;

$('#keys').taglist ({
	labelProvider: function ( entry, element ) { element.text(entry); },
	entryProvider: function ( data ) {
		return data.trim();
	},
	validator: function (newTag) {
		var OK = metaKeyRE.exec ( newTag );
		if ( !OK ) throw "Meta key format is 'namespace:key'";
	},
	validatorMessagesContainer: '#keys-validation',
	validationStatusContainer: '#keys-group',
	hiddenInputs: "keys",
	data: keys
});
</script>

</h:main>
