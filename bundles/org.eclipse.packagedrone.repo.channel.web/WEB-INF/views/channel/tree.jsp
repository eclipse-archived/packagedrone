<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="storage" uri="http://eclipse.org/packagedrone/repo/channel" %>
<%@ taglib prefix="pm" uri="http://eclipse.org/packagedrone/web/common" %>
<%@ taglib prefix="web" uri="http://eclipse.org/packagedrone/web" %>

<%@ taglib prefix="table" uri="http://eclipse.org/packagedrone/web/common/table"  %>

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/storage" %>

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
				<s:tree_fragment map="${treeArtifacts }" manager="${manager }" artifacts="${treeArtifacts.get(null) }" level="${0 }"/>
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

<div class="modal" tabindex="-1" role="dialog" id="deleteArtifactModal">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">Delete artifact?</h4>
      </div>
      <div class="modal-body">
        <p>
        Are you sure that you want to delete the artifact <code><span class="artifact-name"></span></code> (<code><span class="artifact-id"></span></code>)?
        </p>
        <p>
        This operation cannot be undone.
        </p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
        <button type="button" class="btn btn-danger" onclick="performDelete();" id="deleteArtifactModalButton"><span class="glyphicon glyphicon-trash"></span> Delete</button>
      </div>
    </div><%-- /.modal-content --%>
  </div><%-- /.modal-dialog --%>
</div><%-- /.modal --%>

<script type="text/javascript">
$('#deleteArtifactModal').on('show.bs.modal', function (event) {
	var source = $(event.relatedTarget);
	var artifactId = source.data('artifact-id');
	var artifactName = source.data('artifact-name');
	
	var modal = $(this);
	modal.find('.artifact-id').text(artifactId);
	modal.find('.artifact-name').text(artifactName);
});

function performDelete () {
	$('#deleteArtifactModal button').prop('disabled', true);
	
	var modal = $('#deleteArtifactModal');
	var artifactId = modal.find('.artifact-id').text();
	var artifactName = modal.find('.artifact-name').text();
	
	window.location = '<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/"/>' + encodeURIComponent ( artifactId ) + '/delete';
}
</script>

</jsp:attribute>

</h:main>