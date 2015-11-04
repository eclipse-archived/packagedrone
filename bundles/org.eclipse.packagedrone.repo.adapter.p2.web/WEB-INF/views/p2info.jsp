<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>

<h:main title="P2 Channel Information" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<div class="container-fluid">

    <div class="row">

        <div class="col-xs-12">

			<h3 class="details-heading">Core Attributes</h3>
			             
			<dl class="dl-horizontal details">
			    <dt>Title</dt>
			    <dd>${fn:escapeXml(channelInfo.title)}</dd>
			    
			    <dt>Mirrors URL</dt>
			    <dd><c:if test="${not empty channelInfo.mirrorsUrl }"><a href="${fn:escapeXml(channelInfo.mirrorsUrl) }" target="_blank">${fn:escapeXml(channelInfo.mirrorsUrl) }</a></c:if></dd>
			    
		        <dt>Statistics URL</dt>
                <dd><c:if test="${not empty channelInfo.statisticsUrl }"><a href="${fn:escapeXml(channelInfo.mirrorsUrl) }" target="_blank">${fn:escapeXml(channelInfo.statisticsUrl) }</a></c:if></dd>
	        </dl>
        </div>
   
   </div>
   
</div>
        
</h:main>