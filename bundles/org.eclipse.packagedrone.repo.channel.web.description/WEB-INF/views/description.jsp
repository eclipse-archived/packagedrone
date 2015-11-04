<%@ page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    trimDirectiveWhitespaces="true"
    %>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/packagedrone/security" prefix="sec" %>

<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<h:main title="Channel" subtitle="${pm:channel(channel) }">

<jsp:attribute name="head">
    <script src="/ckeditor/ckeditor.js"></script>
    <style>
    div[contenteditable='true'] {
        border: 1pt solid #CCC;
        padding: 1em;
        margin: 1em;
    }
    </style>
</jsp:attribute>

<jsp:attribute name="body">
    <h:buttonbar menu="${menuManager.getActions(channel) }"/>
    <h:nav menu="${menuManager.getViews(channel) }"/>
    
    <script>
    CKEDITOR.disableAutoInline = true;
    
    function startEditor ()  {
    	  $('#description').attr("contenteditable","true");
    	  CKEDITOR.inline( 'description', {
    		  'startupFocus': true,
    		  'uiColor': '#EEEEEE'
    		  } );
    	  $('#noDescription').hide();
    	  $('#editDescription').hide();
    	  $('#saveDescription').show ();
    }
    
    function saveEditor () {
    	$('#saveDescription').attr("disabled","disabled");
    	$('#description').attr("contenteditable",null);
    	var data = CKEDITOR.instances.description.getData();
    	$('#data').val ( data );
    	$('#form').submit ();
    }
    </script>
    
    <div class="container-fluid form-padding">
        <div class="row">
            <div class="col-xs-12">
                <div id="description">
	               ${channel.getMetaData("sys", "description")}
                </div>
            </div>
            <c:if test="${manager }">
                <div class="col-xs-12">
                    <c:choose>
                        <c:when test="${ empty channel.getMetaData('sys', 'description') }">
                            <div id="noDescription" class="well well-lg">
                            No channel description found. <a href="#" onclick="startEditor(); return false;">Create one</a>!
                            </div>
                        </c:when>
                        <c:otherwise>
                            <button id="editDescription" class="btn btn-default" onclick="startEditor();">Edit</button>
                        </c:otherwise>
                    </c:choose>
                    <button id="saveDescription" class="btn btn-primary" style="display: none;" onclick="saveEditor();">Save</button>
                </div>
                <form id="form" action="" method="POST">
                    <input type="hidden" name="data" id="data"/>
                </form>
            </c:if>
	    </div>
    </div>
    
</jsp:attribute>

</h:main>