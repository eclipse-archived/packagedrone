<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<h:main title="YUM Repository Configuration" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<div class="container-fluid form-padding">

	 <form:form action="" method="POST" cssClass="form-horizontal">
	
	    <div class="row">
	    
	        <div class="col-md-6">
	        
	           <fieldset>
                   <legend>Signing</legend>
                   
                    <h:formEntry label="Service" path="signingServiceId">
                       <form:select path="signingServiceId" cssClass="form-control">
                           <form:option value="" label="Don't sign"/>
                           <form:optionList items="${signingServices }" itemValue="id" itemLabel="label"/>
                       </form:select>
                   </h:formEntry>
               </fieldset>
	        
	        </div>
	    
	    </div>
	    
	    <div class="row">
            <div class="col-md-6">
	            <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">Update</button>
                        <button type="reset" class="btn btn-default">Reset</button>
                    </div>
                </div>
            </div> 
	    </div>
	    
	</form:form>    

</div>

</h:main>