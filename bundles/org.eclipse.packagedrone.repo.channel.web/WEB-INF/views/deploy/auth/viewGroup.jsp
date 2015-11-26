<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ page import="java.util.Collections"%>
<%@ page import="java.util.Arrays"%>
<%@ page import="org.eclipse.packagedrone.repo.channel.deploy.DeployGroup"%>
<%@ page import="org.eclipse.packagedrone.repo.channel.deploy.DeployKey"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%
DeployGroup dg = (DeployGroup)request.getAttribute ( "group" );

DeployKey[] keys = dg.getKeys ().toArray ( new DeployKey[0] );
Arrays.sort ( keys, DeployKey.NAME_COMPARATOR  );
request.setAttribute ( "keys", keys );
%>

<h:main title="Group" subtitle="${fn:escapeXml( (empty group.name) ? group.id : group.name ) }">

<h:buttonbar menu="${menuManager.getActions ( group ) }">
    <jsp:attribute name="before">
        <div class="btn-group" role="group">
            <a class="btn btn-default" href="<c:url value="/deploy/auth/group"/>">Groups</a>
        </div>
    </jsp:attribute>
</h:buttonbar>

<div class="container-fluid">
    <div class="row">
        <div class="col-md-12">

<section>
    <h2>Information</h2>
    
    <dl class="dl-horizontal details">
    
	    <dt>ID</dt>
	    <dd>${fn:escapeXml(group.id) }</dd>
	    
	    <dt>Name</dt>
	    <dd>${fn:escapeXml(group.name) }</dd>
	</dl>
    
</section>

        </div>
    </div>
    <div class="row">
        <div class="col-md-12">

<section>
    <h2>Deploy keys</h2>
    
    <div class="table-responsive">
    <table class="table">
    
        <thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Created</th>
                <th></th>
            </tr>
        </thead>
    
        <tbody>
            <c:forEach var="key" items="${keys}">
                <tr>
                    <td>${fn:escapeXml(key.id) }</td>
                    <td>${fn:escapeXml(key.name) }</td>
                    <td style="white-space: nowrap;">
                        <fmt:formatDate value="${ key.creationDate  }" type="both" />
                    </td>
                    <td rowspan="2">
                        <a class="btn btn-default" href="<c:url value="/deploy/auth/key/${key.id }/edit"/>">Edit</a>
                        <button type="button" data-toggle="modal" data-target="#dlg-delete" class="btn btn-danger" data-key-id="${fn:escapeXml(key.id) }" data-key-name="${fn:escapeXml(key.name) }"><span class="glyphicon glyphicon-trash"></span> Delete</button>
                    </td>
                </tr>
                <tr class="table-row-additional">
                    <td colspan="3" style="padding-left: 3em;">
                        <pre>
&lt;server&gt;
    &lt;id&gt;server.id&lt;/id&gt;&lt;!-- id of your repository element --&gt;
    &lt;username&gt;deploy&lt;/username&gt;
    &lt;password&gt;${fn:escapeXml(key.key) }&lt;/password&gt;
&lt;/server&gt;
</pre>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    
    </table>
    </div>
    
</section>

        </div>
    </div>
</div>


<div class="modal" id="dlg-delete" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">Delete deploy key</h4>
      </div>
      <div class="modal-body">
        <p>
            Are you sure you want to delete the deploy key:
            <span class="dlg-delete-key-id"></span>
            
            <span style="display:none;" class="dlg-delete-key-name-span">named <q><span class="dlg-delete-key-name"></span></q></span>
        </p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        <a type="button" class="btn btn-danger" href="#"><span class="glyphicon glyphicon-trash"></span> Delete</a>
      </div>
    </div><%-- /.modal-content --%>
  </div><%-- /.modal-dialog --%>
</div><%-- /.modal --%>

<script type="text/javascript">
$('#dlg-delete').on('show.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var id = button.data("key-id");
    var name = button.data("key-name");
    var modal = $(this);
    
    modal.find ( '.btn-danger').attr('href', '<c:url value="/deploy/auth/key/"/>' + id + '/delete');
    modal.find ( '.dlg-delete-key-id' ).text ( id );
    if ( name.length > 0 )
    	{
    	    modal.find ( '.dlg-delete-key-name' ).text ( name );
    	    modal.find ( '.dlg-delete-key-name-span' ).show ();
    	}
    else
    	{
    	   modal.find ( '.dlg-delete-key-name-span' ).hide ();
    	}
});
</script>

</h:main>