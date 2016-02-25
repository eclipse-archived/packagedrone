<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="org.eclipse.packagedrone.repo.signing.pgp.web.managed.ServiceController"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<%
pageContext.setAttribute ( "TAG", ServiceController.ACTION_TAG_PGP );
%>

<h:main title="PGP Signing" subtitle="Managed Keys">

<h:buttonbar menu="${menuManager.getActions(TAG) }" />

<h:pager value="${configurations }"/>

<div class="table-responsive">

<table class="table ">
    <thead>
        <tr>
            <th>ID</th>
            <th>Label</th>
            <th>State</th>
            <th></th>
        </tr>
    </thead>
    
    <tbody>
    <c:forEach var="entry" items="${configurations.data }"> 
        <tr class="${not empty entry.errorMessage ? 'danger' : '' }">
            <td>${fn:escapeXml(entry.id) }</td>
            <td>${fn:escapeXml(entry.label) }</td>
            <td>
            	${fn:escapeXml(entry.errorMessage) }
            	<ul>
            	<c:forEach var="key" items="${entry.keys }">
            		<li>${key.sub ? "sub" : "pub"} ${key.bits} bits / ${fn:escapeXml(key.shortId) }
            			<ul>
            				<c:forEach var="userId" items="${key.userIds }">
            					<li>${fn:escapeXml(userId) }</li>
            				</c:forEach>
            			</ul>
            		</li>
            	</c:forEach>
            	</ul>
            </td>
            <td>
                <a class="btn btn-danger" data-key-id="${fn:escapeXml(entry.id)}" data-toggle="modal" data-target="#confirmationDialog">
                    <span class="glyphicon glyphicon-trash"></span>
                </a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<!-- Confirmation dialog -->
<div class="modal fade" id="confirmationDialog" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-sm" role="document">
        <div class="modal-content">
              <div class="modal-header">
                  <h4 class="modal-title">Are you sure?</h4>
              </div>
              <div class="modal-body">
                  <p>You are about to delete this PGP Key. This action <strong>cannot be undone</strong>.</p>
                  <p>Do you want to proceed?</p>
              </div>
              <div class="modal-footer">
                  <a type="button" class="btn btn-default" data-dismiss="modal">Cancel</a>
                  <a type="button" class="btn btn-danger" id="confirmDeleteBtn">Delete</a>
              </div>
        </div>
    </div>
</div>

<!-- Dynamically updates the delete button href to the correct Key -->
<script type="text/javascript">
$("#confirmationDialog").on("show.bs.modal", function(event) {
    var button = $(event.relatedTarget);
    var id = button.data("key-id");
    var modal = $(this);
    modal.find("#confirmDeleteBtn").attr('href', '/pgp.sign.managed/' + id + '/delete');
})
</script>

</div>

</h:main>