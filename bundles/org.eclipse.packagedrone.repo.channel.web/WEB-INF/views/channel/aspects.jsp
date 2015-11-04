<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>

<web:define name="requires">
	<c:if test="${not empty aspect.requires }">
        <div class="panel-body-section">
			<strong>Requires:</strong>
			<ul class="dependency-list">
				<c:forEach var="req" items="${requires }">
				    <c:choose>
				        <c:when test="${assignedAspects.contains(req) }">
				            <li><a href="#${fn:escapeXml(req.factoryId) }" class="text-success">${fn:escapeXml(req.name) }</a></li>
				        </c:when>
				        <c:otherwise>
				            <li><a href="#${fn:escapeXml(req.factoryId) }"  class="text-danger">${fn:escapeXml(req.name) }</a></li>
				        </c:otherwise>
				    </c:choose>
				    
				</c:forEach>
			</ul>
        </div>
	</c:if>
</web:define>

<h:main title="Channel aspects" subtitle="${pm:channel(channel) }">

<style type="text/css">
.panel-body-section {
    margin-bottom: 1em;
}
.dependency-list a {
    text-decoration: underline;
}
</style>

<script type="text/javascript">
function doAction ( action, factoryId )
{
    var form = document.getElementById("form-" + factoryId);
    form.setAttribute("action", action);
    form.submit ();
    return false;
}
</script>

<h:breadcrumbs cssClass="breadcrumb-compact" />

<div class="container-fluid">

<div class="row">

<div class="col-sm-6">

	<div>
	
        <h2>Assigned aspects</h2>
	
		<div role="tabpanel">
	
	        <ul class="nav nav-pills" role="tablist" id="assigned-tab-list">
	            <c:forEach items="${web:sort ( groupedAssignedAspects.keySet() ) }" var="group" varStatus="s">
	                <li role="presentation" class="${ s.first ? 'active' : '' }">
	                    <a role="tab" href="#cgroup-${group.id}" data-toggle="pill" aria-controls="cgroup-${group.id }">${fn:escapeXml(group.name) }</a>
	                </li>
	            </c:forEach>
	        </ul>
	        
	        <p></p>
	        
            <div class="tab-content">
                <c:forEach items="${web:sort ( groupedAssignedAspects.keySet() ) }" var="group" varStatus="s">
                
                    <div role="tabpanel" class="tab-pane ${ s.first ? 'active' : '' }" id="cgroup-${group.id }">
                
						<c:forEach items="${groupedAssignedAspects[group] }" var="aspect">
							<div class="panel panel-default aspect-assigned" id="${aspect.factoryId }">
							    <div class="panel-heading">
							        <h3 class="panel-title">${fn:escapeXml(aspect.name) }
                                        <c:if test="${not aspect.resolved }"><span class="label label-danger">unresolved</span></c:if>
							            <small>${fn:escapeXml(aspect.version) } – ${fn:escapeXml(aspect.factoryId) }</small>
							        </h3>
							    </div>
							    <div class="panel-body">
							    
							     <div class="panel-body-section">
							         ${aspect.information.description }
							     </div>
							         
							     <web:call name="requires" requires="${aspect.requires }"/>
							    
					                <div>
								        <form id="form-${fn:escapeXml(aspect.factoryId) }" action="removeAspect" method="POST">
									        <input type="hidden" name="aspect" value="${fn:escapeXml(aspect.factoryId) }" />
									        <button onclick="doAction('removeAspect', '${fn:escapeXml(aspect.factoryId)}');" class="btn btn-default" type="button" name="command" value="remove">Remove</button>
									        <button onclick="doAction('refreshAspect', '${fn:escapeXml(aspect.factoryId)}');" class="btn btn-default" type="button" name="command" value="refresh" title="Refresh"><span class="glyphicon glyphicon-refresh"></span></button>
									    </form>
								    
								    </div>
							    
							    </div>
							</div>
						
						</c:forEach>
					</div>
				</c:forEach>
			</div>
		
		</div>
	
	</div>

</div>

<div class="col-sm-6" >
<div>

<h2>Additional aspects</h2>

    <div role="tabpanel">

        <ul class="nav nav-pills" role="tablist" id="additional-tab-list">
            <c:forEach items="${web:sort ( addAspects.keySet() ) }" var="group" varStatus="s">
                <li role="presentation" class="${ s.first ? 'active' : '' }">
                    <a role="tab" href="#group-${group.id}" data-toggle="pill" aria-controls="group-${group.id }">${fn:escapeXml(group.name) }</a>
                </li>
            </c:forEach>
        </ul>
        
        <p></p>

        <div class="tab-content">
		    <c:forEach items="${web:sort ( addAspects.keySet() ) }" var="group" varStatus="s">
		    
		      <div role="tabpanel" class="tab-pane ${ s.first ? 'active' : '' }" id="group-${group.id }">
		      
		    	<c:forEach items="${addAspects[group] }" var="aspect">
					<div class="panel panel-default aspect-available" id="${aspect.factoryId }">
					    <div class="panel-heading">
					       <h3 class="panel-title">${fn:escapeXml(aspect.name) } <small>${fn:escapeXml(aspect.version) } – ${fn:escapeXml(aspect.factoryId) }</small></h3>
					    </div>
					    <div class="panel-body">
						    <div class="panel-body-section">${aspect.information.description }</div>
			                <web:call name="requires" requires="${aspect.requires }"/>
						    
			                <div>
			                    <form id="add-${fn:escapeXml(aspect.factoryId) }" class="addAspect" action="addAspect" method="POST" data-factory-id="${fn:escapeXml(aspect.factoryId) }"
			                        <c:set var="missingIds" value="${aspect.getMissingIds(assignedAspects) }" />
			                        <c:if test="${not empty missingIds}">
			                        data-missing-requires="${fn:escapeXml(fn:join(missingIds, ',')) }"
			                        </c:if>
			                        >
			                        <input type="hidden" name="aspect" value="${fn:escapeXml(aspect.factoryId) }">
			                        <input type="submit" value="Add" class="btn btn-default" />
			                    </form>
						    </div>
					    </div>
					</div>
				</c:forEach>
				</div>
		    </c:forEach>
        </div>
    </div>
    
</div>
</div>

</div>

</div> <%-- container --%>

<div class="modal" id="modal-requires">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">Unsatisfied Dependencies</h4>
      </div>
      <div class="modal-body">
        <p>
            This aspects requires other channel aspects which are not assigned to the channel up to now:
        </p>
        <ul id="modal-req-list">
        </ul>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
        <button type="button" class="btn btn-warning" id="modal-req-without">Add without dependencies</button>
        <button type="button" class="btn btn-primary" id="modal-req-with">Add with dependencies</button>
      </div>
    </div><%-- modal-content --%>
  </div><%-- modal-dialog --%>
</div><%-- modal --%>

<script type="text/javascript">

function escapeId ( theid ) {
    return theid.replace ( /([:,\.\[\]])/g, "\\\\$1" ); <%-- double escape, one for JSP, one for JavaScript --%>
}

var nameMap = ${nameMapJson};

$("form.addAspect[data-missing-requires]").click ( function(event){
	var req = $(this).data("missing-requires").split( "," );
	// console.log ( req);
	event.preventDefault();
	
	var list = $("#modal-req-list");
    list.children().remove ();
	
	for (var i = 0; i < req.length; i++) {
		var r = req[i];
		console.log ( r );
		var rn = nameMap[r];
		console.log ( rn );
		var li = document.createElement ( "li");
		li.appendChild ( document.createTextNode ( rn ));
		list.append ( li );
	}
	
	// console.log ( $(this).data("factory-id") );
	
	var modal = $("#modal-requires");
	modal.data ( "factoryId", $(this).data("factoryId") );
	modal.modal({
		backdrop: 'static'
	});
} );

$('#modal-req-without').click ( function(event){
	var modal = $("#modal-requires");
	var factoryId = modal.data("factoryId");
	
	var form = $("#add-" + escapeId ( "" + factoryId ) );
	form.attr ( "action", "addAspect");
	form.submit ();
	
	$("input[type=submit]").attr("disabled","disabled");
	$("button").attr("disabled","disabled");
} );

$('#modal-req-with').click ( function(event){
	var modal = $("#modal-requires");
    var factoryId = modal.data("factoryId");
	
	var form = $("#add-" + escapeId ( factoryId ) );
    form.attr ( "action", "addAspectWithDependencies");
    form.submit ();
    
    $("input[type=submit]").attr("disabled","disabled");
    $("button").attr("disabled","disabled");
} );
</script>

</h:main>