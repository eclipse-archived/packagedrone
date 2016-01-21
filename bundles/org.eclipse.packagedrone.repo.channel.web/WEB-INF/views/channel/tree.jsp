<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib tagdir="/WEB-INF/tags/storage" prefix="s" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="storage" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/common" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>

<%@ taglib prefix="table" uri="http://eclipse.org/packagedrone/web/common/table"  %>

<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<h:main title="Channel" subtitle="${storage:channel(channel) }"> 

<jsp:attribute name="subtitleHtml"><s:channelSubtitle channel="${channel }" /></jsp:attribute>

<jsp:attribute name="head">
	<style type="text/css">
	a.popup-ajax {
	    color: inherit;
	}
	</style>
	<s:dz_head/>
</jsp:attribute>

<jsp:attribute name="body">

<h:buttonbar menu="${menuManager.getActions(channel) }">
    <jsp:attribute name="after">
        <c:if test="${manager }"><s:dz_button /></c:if>
    </jsp:attribute>
</h:buttonbar>

<s:dz_container/>

<h:nav menu="${menuManager.getViews(channel) }" />

<div class="table-responsive">
	<table:extender id="artifacts.tree" tags="artifacts, default" channel="${channel }">
		<table id="artifacts" class="table table-condensed table-hover">
		
			<thead>
			    <tr>
			        <th>Name</th>
			        <th>Classifier</th>
			        <th>Size</th>
			        <th>Created</th>
			        <table:columns var="col" end="0"><th id="col-${col.id }" title="${fn:escapeXml(col.description) }">${fn:escapeXml(col.label) }</th></table:columns>
			        <th></th>
			        <th></th>
			        <th></th>
			        <th></th>
			        <table:columns var="col" start="0"><th id="col-${col.id }">${fn:escapeXml(col.label) }</th></table:columns>
			    </tr>
			</thead>
		
			<tbody>
				<s:tree_fragment map="${treeArtifacts }" artifacts="${treeArtifacts.get(null) }" level="${0 }"/>
			</tbody>
		
		</table>
	</table:extender>
</div>

<script type="text/javascript">
$(".expander").click(function (event) {
	event.preventDefault();
	var expanded = $(this).hasClass ( "opened" );
	var id = $(this).data("artifact");
	console.log ( "Expanded: " + expanded );
	console.log ( "parent: " + id );
	console.log ( $(this) );
	if ( expanded ) {
        $(this).children("i").removeClass ( "fa-minus-square-o");
        $(this).children("i").addClass ( "fa-plus-square-o");
	    $(this).removeClass("opened");
	    $('tr[data-parents~=' + id + ']').hide();
	    $('tr[data-parents~=' + id + '] a.expander' ).removeClass("opened");
	    $('tr[data-parents~=' + id + '] a.expander > i' ).removeClass("fa-minus-square-o");
	    $('tr[data-parents~=' + id + '] a.expander > i' ).addClass("fa-plus-square-o");
	}
	else {
		$(this).children("i").removeClass ( "fa-plus-square-o");
        $(this).children("i").addClass ( "fa-minus-square-o");
        $(this).addClass("opened");
        $('tr[data-parent=' + id + ']').show();
	}
});

$('tr[data-level=0]').show ();
</script>

<s:dz_init/>

</jsp:attribute>

</h:main>