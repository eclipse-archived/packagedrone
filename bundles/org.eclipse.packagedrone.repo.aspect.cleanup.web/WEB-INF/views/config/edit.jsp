<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="storage" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>


<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<h:main title="Channel" subtitle="${storage:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }" />

<h:nav menu="${menuManager.getViews(channel) }"/>

<div class="container-fluid form-padding">

    <div class="row">
    
        <div class="col-md-8">

			<form:form action="" method="POST" cssClass="form-horizontal">
			   <h:formEntry label="Number of Entries" path="numberOfVersions">
			       <form:input path="numberOfVersions" cssClass="form-control" type="number"/>
			        <span class="help-block">
			           The number of artifacts per aggregation group you want to keep.
		            </span>
			   </h:formEntry>
			   
			   <h:formCheckbox label="Only root artifacts" path="onlyRootArtifacts" command="command">
			     <span class="help-block">
			     If checked, then only root elements will be cleared and child elements will be ignored.
			     </span>
			   </h:formCheckbox>
			   
               <div class='form-group ${form:validationState(pageContext,"command","aggregator", "", "has-error")}'>
                   <form:label path="aggregator" cssClass="col-sm-2 control-label">Aggregation</form:label>
                   
                   <div class="col-sm-10" id="aggregatorList">
                   </div>
                   
                   <input type="hidden" name="aggregator" id="aggregator"/>
                   
                   <div class="col-sm-10 col-sm-offset-2">
                       <form:errorList path="aggregator" cssClass="help-block" />
                   </div>
               </div>
                
			   <div class='form-group ${form:validationState(pageContext,"command","sorter", "", "has-error")}'>
				    <form:label path="sorter" cssClass="col-sm-2 control-label">Sorting</form:label>
				    
				    <div class="col-sm-10" id="sorterList">
				    </div>
				    
				    <input type="hidden" name="sorter" id="sorter"/>
				    
				    <div class="col-sm-10 col-sm-offset-2">
				        <form:errorList path="sorter" cssClass="help-block" />
				    </div>
				</div>
				
		       <div class="form-group">
		           <div class="col-sm-offset-2 col-sm-10">
		               <button type="submit" class="btn btn-primary">Update</button>
		               <button class="btn btn-info" id="test">Test</button>
		           </div>
		       </div>
		    </form:form>
	    
	    </div>
	    
	    <div class="col-md-4">
	       <form id="add">
	           <div class="form-group">
	               <label for="sortNamespace">Namespace</label>
	               <input class="form-control" name="namespace" id="namespace" type="text"/>
	           </div>
	           <div class="form-group">
                   <label for="sortKey">Key</label>
                   <input class="form-control" name="key" id="key" type="text"/>
               </div>
               <button class="btn btn-default" id="addAggregator"><span class="glyphicon glyphicon-plus"></span> Add Aggregation</button>
               <button class="btn btn-default" id="addSorter"><span class="glyphicon glyphicon-plus"></span> Add Sorter</button>
	       </form>
	    </div>
	    
    </div>

</div>

<script type="text/javascript">

var sorterState = ${web:json(command.sorter)};
var aggrState = ${web:json(command.aggregator)};

console.log (sorterState);
console.log (aggrState);

function updateArrange () {
	$('.entry').each ( function (entry) {
	    var h = $(this);
		var ele = h[0];
		
	    var up = h.find('button[data="up"]');
	    var down = h.find('button[data="down"]');
		   
	    var isFirst = ele.previousSibling == null || ele.previousSibling.nodeType != Node.ELEMENT_NODE;
	    var isLast = ele.nextSibling == null || ele.nextSibling.nodeType != Node.ELEMENT_NODE;
	    
	    if ( isFirst ) {
	    	up.attr ( "disabled", "disabled" );     
	    }
	    else {
	    	up.removeAttr ( "disabled" );
	    }
	    
	    if ( isLast ) {
	    	down.attr ( "disabled", "disabled" );
	    }
	    else {
	    	down.removeAttr ( "disabled" );
	    }
	});
}

function attachButtons ( ele ) {
   var h = $(ele);
   
   var up = h.find('button[data="up"]');
   var down = h.find('button[data="down"]');
   
   up.click (function(event){
	    console.log ( ele );
	    ele.parentNode.insertBefore(ele,ele.previousSibling);
	    updateArrange ();
        event.preventDefault();
        return false;
   });
   
   down.click (function(event){
       console.log ( ele );
       ele.parentNode.insertBefore(ele,ele.nextSibling.nextSibling);
       updateArrange ();
       event.preventDefault();
       return false;
   });
}

function addSorterEntry (entry) {
	
	var h = $('<div class="form-control-static sorterEntry entry"><div class="form-control-static">\
			<button class="btn btn-default" data="up"><span class="glyphicon glyphicon-chevron-up"></span></button>\
			<button class="btn btn-default" data="down"><span class="glyphicon glyphicon-chevron-down"></span></button>\
			<button class="btn btn-default" data="toggle-order"><span data="order"></span></button>\
			<button data="remove" class="btn btn-default"><span class="glyphicon glyphicon-trash"></span></button>\
			<code data="namespace"></code> : <code data="key"></code>\
			</div></div>');
	
	h.find('code[data="namespace"]').text ( entry.key.namespace );
	h.find('code[data="key"]').text ( entry.key.key );
	h.find('button[data="remove"]').click(function(event){
		h.remove ();
		updateArrange ();
		event.preventDefault ();
		return false;
	});
	
	h.find('button[data="toggle-order"]').click(function(event){
	    if ( entry.order == 'ASCENDING') {
	    	  entry.order = 'DESCENDING';
	          h.find('span[data="order"]').attr("class", "glyphicon glyphicon-sort-by-attributes-alt");
	    } else {
	    	  entry.order = 'ASCENDING';
	          h.find('span[data="order"]').attr("class", "glyphicon glyphicon-sort-by-attributes");
	    }
        event.preventDefault ();
        return false;
    });
	
	if ( entry.order == 'ASCENDING')
		   h.find('span[data="order"]').attr("class", "glyphicon glyphicon-sort-by-attributes");
	else
		   h.find('span[data="order"]').attr("class", "glyphicon glyphicon-sort-by-attributes-alt");
	
    var root = $('#sorterList');
    root.append ( h );
    
    attachButtons ( h[0] );
}

function addAggregatorEntry (entry) {
     var h = $('<div class="form-control-static aggrEntry entry"><div class="form-control-static">\
    		<button class="btn btn-default" data="up"><span class="glyphicon glyphicon-chevron-up"></span></button>\
            <button class="btn btn-default" data="down"><span class="glyphicon glyphicon-chevron-down"></span></button>\
    		<button data="remove" class="btn btn-default"><span class="glyphicon glyphicon-trash"></span></button>\
    		<code data="namespace"></code> : <code data="key"></code>\
    		</div></div>');
    
    h.find('code[data="namespace"]').text ( entry.namespace );
    h.find('code[data="key"]').text ( entry.key );
    h.find('button[data="remove"]').click(function(event){
        h.remove ();
        updateArrange ();
        event.preventDefault ();
        return false;
    });
    
    var root = $('#aggregatorList');
    root.append ( h );
    
    attachButtons ( h[0] );
}

if ( sorterState != null ) {
    for ( var i in sorterState.fields ) {
        addSorterEntry ( sorterState.fields[i] );	
    }
}
if ( aggrState != null ) {
    for ( var i in aggrState.fields ) {
        addAggregatorEntry ( aggrState.fields[i] );    
    }
}

updateArrange ();

$("#test").click ( function(event){
	console.log ( "test" );
	
	var form = $( '#command' );
	
	form.attr("action", "<c:url value="test"/>");
	form.submit ();
	
	return false;
});

$("#command").submit (function(event) {
	console.log ( "submit" );
	
	var sortField = $('#sorter');
	var aggrField = $('#aggregator');
	
	var form = $( this );
	
	var sort = { fields: [] };
	var aggr = { fields: [] };
	
	$(".sorterEntry").each (function() {
		sort.fields.push ( {
			key : {
				namespace: $(this).find('code[data="namespace"]').text(),
				key: $(this).find('code[data="key"]').text()
			},
			  order: $(this).find('span[data="order"]').hasClass('glyphicon-sort-by-attributes') ? "ASCENDING" : "DESCENDING"
		} );
	});
	
	$(".aggrEntry").each (function() {
		aggr.fields.push ( {
            namespace: $(this).find('code[data="namespace"]').text(),
            key: $(this).find('code[data="key"]').text()
        } );
    });
	
	console.log ( sort );
	console.log ( aggr );
	
	var sortStr = JSON.stringify ( sort, null, 2 );
	sortField.val ( sortStr );
	
	var aggrStr = JSON.stringify ( aggr, null, 2 );
	aggrField.val ( aggrStr );
});

$("#addSorter").click ( function (event) {
	var form = $("#add");
	addSorterEntry ( { 
		"key" : {
			"namespace" : form.find("#namespace").val (),
			"key" : form.find("#key").val ()
		},
		"order": "ASCENDING"
	} ) ;
	updateArrange ();
	event.preventDefault ();
	return false;
});

$("#addAggregator").click ( function (event) {
    var form = $("#add");
    addAggregatorEntry ( { 
        "namespace" : form.find("#namespace").val (),
        "key" : form.find("#key").val ()
    } ) ;
    updateArrange ();
    event.preventDefault ();
    return false;
});
</script>


</h:main>