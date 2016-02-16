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
    
      <tr class="${ empty trigger.descriptor ? 'danger' : ''}">
      
        <c:choose>
          <c:when test="${not empty trigger.configuration }">
            <c:set var="ti" value="${triggerFactoryTracker.apply(trigger.configuration.triggerFactoryId)}"/>
          </c:when>
          <c:otherwise><c:remove var="ti"/></c:otherwise>
        </c:choose>
      
        <td id="row-trigger-${fn:escapeXml(trigger.id) }">
        
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
        
        <td align="right">
          
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
        
        <tr>
          
          <td style="padding-left: 3em;">
            <c:if test="${ not empty pi }">
              <h4>${fn:escapeXml(pi.label)} <small>${processor.configuration.factoryId }</small></h4>
              <div>
                <c:choose>
                  <c:when test="${ processor.state.present and not empty processor.state.get().htmlState}">
                    <p>${processor.state.get().htmlState}</p><%-- don't escape html on purpose --%>
                  </c:when>
                  <c:otherwise>
                    <p>${fn:escapeXml(pi.description) }</p>
                  </c:otherwise>
                </c:choose>
              </div>
              
              
            </c:if>
            <c:if test="${empty pi }">
              <h4>${processor.configuration.factoryId } <span class="label label-danger" title="The factory implementing the functionality is missing or not active" data-toggle="tooltip">unbound</span></h4>
            </c:if>
          </td>
          
          <td align="right">
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

<%-- activate tooltips --%>
  
<script>
$(function () {
	  $('[data-toggle="tooltip"]').tooltip();
})
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