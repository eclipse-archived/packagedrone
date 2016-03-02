<%@ page
  language="java" 
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pm" uri="http://eclipse.org/packagedrone/repo/channel" %>
<%@ taglib prefix="web" uri="http://eclipse.org/packagedrone/web" %>

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/storage" %>

<h:main title="Channel triggers" subtitle="${pm:channel(channel) }">

<jsp:attribute name="subtitleHtml"><s:channelSubtitle channel="${channel }" /></jsp:attribute>

<jsp:body>

  <style>
<!--
.dragging .processor-drag-handle {
}

.over {
  background-color: #999999;
}

.processor-drag-handle {
  cursor: move;
  padding-left: 0.2em;
  padding-right: 0.5em;
  
  color: #CCCCCC;
  
  width: 30px;
}
.processor-content {
}
.actions {
  white-space: nowrap;
}
-->
</style>

  <h:breadcrumbs/>
  
  <div class="container-fluid">
    <div class="row">
      <div class="col-md-1">
        <c:if test="${not empty triggerFactories }">
        <button class="btn btn-default" type="button" data-toggle="modal" data-target="#add-trigger-model" >Add trigger</button>
        </c:if>
      </div>
      <div class="col-md-11">
  
  <table class="table table-hover">
  
    <c:forEach var="trigger" items="${triggers }">
    
      <tr class="trigger ${ empty trigger.descriptor ? 'danger' : ''}" data-trigger-id="${trigger.id }">
      
        <c:choose>
          <c:when test="${not empty trigger.configuration }">
            <c:set var="ti" value="${triggerFactoryTracker.apply(trigger.configuration.triggerFactoryId)}"/>
          </c:when>
          <c:otherwise><c:remove var="ti"/></c:otherwise>
        </c:choose>
      
        <td colspan="2" id="row-trigger-${fn:escapeXml(trigger.id) }">
        
          <h4>
            <c:choose>
              <c:when test="${not empty trigger.descriptor }">
                <strong>${fn:escapeXml(trigger.descriptor.label) }</strong>
              </c:when>
              <c:when test="${not empty trigger.configuration }">
                ${fn:escapeXml(trigger.configuration.triggerFactoryId) } <span class="label label-danger" title="The factory implementing the functionality is missing or not active" data-toggle="tooltip">unbound</span>
              </c:when>
              <c:otherwise>
                ${fn:escapeXml(trigger.id) } <span class="label label-danger" title="The factory implementing the functionality is missing or not active" data-toggle="tooltip">unbound</span>
              </c:otherwise>
            </c:choose>
            
            <c:if test="${not empty trigger.configuration }">
              <span class="label label-default">configured</span>
            </c:if>
          </h4>
          
          <p>
          <c:choose>
            <%-- use get syntax due to issues with Java 8 default interface methods --%>
            <c:when test="${not empty trigger.descriptor.getHtmlState() }">${trigger.descriptor.getHtmlState() }</c:when>
            <c:otherwise>${fn:escapeXml(trigger.descriptor.description) }</c:otherwise>
          </c:choose>
          
          </p>
        </td>
        
        <td class="actions" align="right">
          
          <form method="POST" action="removeTrigger">
            <input type="hidden" name="triggerId" value="${fn:escapeXml(trigger.id) }">
        
            <button class="btn btn-default ${ empty trigger.availableProcessors ? 'disabled' : '' }" type="button" data-toggle="modal" data-target="#add-modal-${fn:escapeXml(trigger.id) }"><span class="glyphicon glyphicon-plus"></span></button>
            
            <c:if test="${not empty trigger.configuration }">
            <button class="btn btn-danger"><span class="glyphicon glyphicon-trash"></span></button>
            </c:if>
            
            <c:if test="${not empty ti.configurationUrl }">
              <c:url var="url" value="${ti.configurationUrl }">
                <c:param name="channelId" value="${channel.id }" />
                <c:param name="triggerId" value="${trigger.id }" />
              </c:url>
              <a href="${url }" class="btn btn-default" title="Edit"><span class="glyphicon glyphicon-edit"></span></a>
            </c:if>
              
          </form>
          
        </td>  
      </tr>
      
      <web:push name="modal">
        <div id="add-modal-${fn:escapeXml(trigger.id) }" class="modal" tabindex="-1" role="dialog" aria-labelledby="add-modal-${fn:escapeXml(trigger.id) }-label">
          <div class="modal-dialog modal-lg">
            <div class="modal-content">
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="add-modal-${fn:escapeXml(trigger.id) }-label">Add processor</h4>
              </div>
              
              <div class="modal-body">
              
                <div class="list-group">
                
                  <c:forEach var="processor" items="${trigger.availableProcessors }">
                    <c:if test="${not empty processor.configurationUrl }">
                      <c:url var="url" value="${processor.configurationUrl }">
                        <c:param name="channelId" value="${channel.id }" />
                        <c:param name="triggerId" value="${trigger.id }" />
                      </c:url>
                      <a href="${url }" class="list-group-item">
                        <h4 class="list-group-item-heading">${fn:escapeXml(processor.label) }</h4>
                        <p class="list-group-item-text">${fn:escapeXml(processor.description) }</p>
                      </a>
                    </c:if>
                  </c:forEach>
              
                </div>
              
              </div>
              
              <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              </div>
              
            </div>
          </div>
        </div>
      </web:push>
      
      <c:forEach var="processor" items="${trigger.processors }">
      
        <c:set var="pi" value="${processorFactoryTracker.apply(processor.configuration.factoryId)}"/>
        
        <tr class="processor" data-trigger-id="${fn:escapeXml(trigger.id)}" data-processor-id="${ fn:escapeXml(processor.id) }">
          
          <td class="processor-drag-handle" draggable="true">
            <h4><i class="fa fa-bars"></i></h4>
          </td>
          
          <td class="processor-content">
            <c:if test="${ not empty pi }">
              <h4>${fn:escapeXml(pi.label)} <small>${processor.configuration.factoryId }</small></h4>
              
              <c:choose>
                <c:when test="${ processor.state.present and not empty processor.state.get().htmlState}">
                  <p>${processor.state.get().htmlState}</p><%-- don't escape html on purpose --%>
                </c:when>
                <c:otherwise>
                  <p>${fn:escapeXml(pi.description) }</p>
                </c:otherwise>
              </c:choose>
              
            </c:if>
            <c:if test="${empty pi }">
              <h4>${processor.configuration.factoryId } <span class="label label-danger" title="The factory implementing the functionality is missing or not active" data-toggle="tooltip">unbound</span></h4>
            </c:if>
          </td>
          
          <td class="actions" align="right">
            <form method="POST" action="removeProcessor">
              <input type="hidden" name="triggerId" value="${fn:escapeXml(trigger.id) }">
              <input type="hidden" name="processorId" value="${processor.id }">
              <button class="btn btn-danger"><span class="glyphicon glyphicon-trash"></span></button>
              
              <c:if test="${not empty pi.configurationUrl }">
                <c:url var="url" value="${ pi.configurationUrl }">
                  <c:param name="channelId" value="${channel.id }" />
                  <c:param name="triggerId" value="${trigger.id }" />
                  <c:param name="processorId" value="${processor.id }" />
                </c:url>
                <a href="${url }" title="Edit" class="btn btn-default"><span class="glyphicon glyphicon-edit"></span></a>
              </c:if>
              
            </form>
          </td>
          
        </tr>
      </c:forEach>
        
    </c:forEach>
  </table>

      </div>
    </div>
  </div>

<script>

<%-- activate tooltips --%>
$(function () {
	$('[data-toggle="tooltip"]').tooltip();
})

var dragSource;

$(function () {
	$('.trigger').each ( function () {
		var element = $(this);
		
		element.on("dragover", function(e) {
			if ( e.preventDefault ) {
				e.preventDefault();
			}
			
			var parentRow = $(this).closest('.trigger');
			
			if ( parentRow == null )
				return;
			
			e.originalEvent.dataTransfer.dropEffect = "move";
			
			parentRow.addClass ("over");
			
			return false;
		});
		element.on("dragenter", function(e) {
			if ( e.preventDefault ) {
				e.preventDefault();
			}
		});
		element.on("dragleave", function(e) {
			$(this).removeClass ("over");
		});
		element.on("drop", function(e) {
			if ( e.stopPropagation) {
				e.stopPropagation ();
			}
			
			$('.processor, .trigger').removeClass("dragging over");
			
			reorder ( dragSource, $(this) );
			
			return false;
		});
	} );
	
	$('.processor-drag-handle').each ( function() {
		var element = $(this);
		var row = element.closest('.processor');
		
		if ( row == null && rowTrigger == null )
			return;
		
		element.on("dragstart", function (e) {
			row.addClass("dragging");
			e.originalEvent.dataTransfer.effectAllowed = "move";
			e.originalEvent.dataTransfer.setData("text/html", row.innerHTML);
			e.originalEvent.dataTransfer.setDragImage(row.get(0), 0,0);
			
			dragSource = row;
		});
		row.on("dragover", function(e) {
			if ( e.preventDefault ) {
				e.preventDefault();
			}
			
			var parentRow = $(this).closest('.processor');
			
			if ( parentRow == null )
				return;
			
			e.originalEvent.dataTransfer.dropEffect = "move";
			
			parentRow.addClass ("over");
			
			return false;
		});
		row.on("dragenter", function(e) {
			if ( e.preventDefault ) {
				e.preventDefault();
			}
		});
		row.on("dragleave", function(e) {
			var parentRow = $(this).closest('.processor');
			if ( parentRow == null )
				return;
			
			parentRow.removeClass ("over");
		});
		row.on("drop", function(e) {
			if ( e.stopPropagation) {
				e.stopPropagation ();
			}
			
			$('.processor, .trigger').removeClass("dragging over");
			
			var parentRow = $(this).closest('.processor');
			if ( parentRow == null )
				return;
			
			reorder ( dragSource, row );
			
			return false;
		});
		element.on("dragend", function(e) {
			row.removeClass("dragging");
			dragSource = null;
		});
	});	
})

function reorder ( ele1, ele2 ) {
	var data = {
		triggerId1: ele1.data("trigger-id"), 
		processorId1: ele1.data("processor-id"),
		triggerId2: ele2.data("trigger-id"), 
		processorId2: ele2.data("processor-id")
	};
	
	console.log ( "reorder: ", data );
	
	Overlay.show();
	var req = $.post( "reorder", data );
	
	req.done ( function (){ window.location.reload(); } );
	
	req.fail ( function (jqXHR, textStatus, e) {
		// failure
		Overlay.failReload("Failed to execute operation", "Cause: " + e);
	});
}
</script>

<div id="add-trigger-model" class="modal" tabindex="-1" role="dialog" aria-labelledby="add-trigger-model-label">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="add-trigger-model-label">Add configured trigger</h4>
      </div>
      
      <div class="modal-body">
      
        <div class="list-group">
        
          <c:forEach var="factory" items="${triggerFactories }">
            <c:if test="${not empty factory.configurationUrl }">
              <c:url var="url" value="${factory.configurationUrl }">
                <c:param name="channelId" value="${channel.id }" />
              </c:url>
              <a href="${url }" class="list-group-item">
                <h4 class="list-group-item-heading">${fn:escapeXml(factory.label) }</h4>
                <p class="list-group-item-text">${fn:escapeXml(factory.description) }</p>
              </a>
            </c:if>
          </c:forEach>
      
        </div>
      
      </div>
      
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      </div>
      
    </div>
  </div>
</div>
  
</jsp:body>

</h:main>