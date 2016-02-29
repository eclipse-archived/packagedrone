<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>
  
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ taglib prefix="form" uri="http://eclipse.org/packagedrone/web/form" %>

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>

<h:main title="Add Cleanup" subtitle="to trigger '${triggerId }'" subtitleHtml="to trigger <code>${triggerId }</code>">

  <div class="container">
  
    <form:form method="POST" cssClass="form-horizontal" command="command">
  
        <h:formEntry label="Aggregator" path="aggregator.fields" command="command" id="aggregator-group">
          
          <div class="drone-tag-list drone-tag-list-form" id="aggregator">
            
            <ul class="drone-tag-list-content">
            </ul>
            
            <div class="input-group">
              <input type="text" class="form-control" data-add="drone-tag">  
              <span class="input-group-btn">
                <button class="btn btn-default" data-add-trigger="drone-tag" type="button"><span class="glyphicon glyphicon-plus"></span></button>
              </span>
            </div>
          </div>
          
          <span class="help-block" id="aggregator-validation"></span>
          
          <span class="help-block">
              The names of the attributes which will get aggregated in one group. One or more items in the form <code>namespace:key</code>.
          </span>
        </h:formEntry>
        
        <h:formEntry label="Sorter" path="aggregator.fields" command="command" id="sorter-group">
          
          <div class="drone-tag-list drone-tag-list-form" id="sorter">
            
            <ul class="drone-tag-list-content">
            </ul>
            
            <div class="input-group">
              <input type="text" class="form-control" data-add="drone-tag">  
              <span class="input-group-btn">
                <button class="btn btn-default" data-add-trigger="drone-tag" data-add-trigger-variant="ASCENDING" type="button" title="Add ascending"><span class="glyphicon glyphicon-sort-by-attributes"></span></button>
                <button class="btn btn-default" data-add-trigger="drone-tag" data-add-trigger-variant="DESCENDING" type="button" title="Add descending"><span class="glyphicon glyphicon-sort-by-attributes-alt"></span></button>
              </span>
            </div>
          </div>
          
          <span class="help-block" id="sorter-validation"></span>
          
          <span class="help-block">
              The names and sort order of the attributes which will be used to sort artifacts in each group. One or more items in the form <code>namespace:key</code>.
          </span>
        </h:formEntry>
        
        <h:formEntry label="Number of entries" path="numberOfEntries" command="command">
          <form:input path="numberOfEntries" cssClass="form-control" type="number" min="0" />
          
          <span class="help-block">
            The maximum number for entries which will remain in each aggregation group.
          </span>
        </h:formEntry>
        
        <h:formEntry>
          <div class="radio radio-inline">
            <label>
              <form:radio path="rootOnly" value="true" />
              Only root artifacts
            </label>
          </div>
          <div class="radio radio-inline">
            <label>
              <form:radio path="rootOnly" value="false" />
              Root and child artifacts
            </label>
          </div>
          
          <span class="help-block">
            Whether to clean up only root level artifacts or also child artifacts.
            <em>Note:</em> Child artifacts still get deleted when their parents get deleted.
          </span>
          
        </h:formEntry>
        
        <h:formEntry>
          <div class="radio radio-inline">
            <label>
              <form:radio path="ignoreWhenMissingFields" value="true" />
              <strong>Ignore</strong> artifacts which don't have all aggregator fields
            </label>
          </div>
          <div class="radio radio-inline">
            <label>
              <form:radio path="ignoreWhenMissingFields" value="false" />
              <strong>Include</strong> artifacts which don't have all aggregator fields
            </label>
          </div>
          
          <span class="help-block">
            In <strong>ignore</strong> mode, artifacts which are missing an field of
            the aggregator list will be ignored completely. In <strong>include</strong> mode
            the value of a missing field will be an empty string instead.
          </span>
          
        </h:formEntry>
        
        <h:formButtons>
          <input type="hidden" name="configuration" id="configuration" />
          <button class="btn btn-primary" type="button" form="command" onclick="triggerUpdate(); return false;">${buttonLabel}</button>
          <button class="btn btn-info" type="button" onclick="triggerVerify(); return false;">Test</button>
        </h:formButtons>
      
    </form:form>
    
    <form method="POST" target="_blank" action="configure/test" id="test-form">
      <input type="hidden" name="configuration" id="test-data" />
      <input type="hidden" name="channelId" id="test-channel-id" />
    </form>
  
  </div>
  
<script type="text/javascript">
var cfg = JSON.parse('${command}');

var metaKeyRE = /[a-zA-Z0-9]+\:[a-zA-Z0-9]+/;

$('#aggregator').taglist ({
	labelProvider: function ( entry, element ) { element.text(entry); },
	entryProvider: function ( data ) {
		return data.trim();
	},
	validator: function (newTag) {
		var OK = metaKeyRE.exec ( newTag );
		if ( !OK ) throw "Meta key format is 'namespace:key'";
	},
	validatorMessagesContainer: '#aggregator-validation',
	validationStatusContainer: '#aggregator-group',
	sortable: true,
	data: cfg.aggregator.fields
});

$('#sorter').taglist ({
	labelProvider: function ( entry, element ) {
		element.append(document.createTextNode(entry.key));
		
		if ( entry.order == "ASCENDING" ) {
			element.append ('&nbsp;<span class="glyphicon glyphicon-sort-by-attributes"></span>');	
		}
		else
			element.append ('&nbsp;<span class="glyphicon glyphicon-sort-by-attributes-alt"></span>');
	},
	entryProvider: function ( data, variant ) {
		return {
			key: data.trim(),
			order: variant == null ? "ASCENDING" : variant
		}
	},
	validator: function (data, variant) {
		var OK = metaKeyRE.exec ( data );
		if ( !OK ) throw "Meta key format is 'namespace:key'";
	},
	validatorMessagesContainer: '#sorter-validation',
	validationStatusContainer: '#sorter-group',
	sortable: true,
	data: cfg.sorter.fields
});

function update () {
	cfg.numberOfEntries = $('#numberOfEntries').val();
	cfg.rootOnly = $("input[name=rootOnly]:checked").val();
	cfg.ignoreWhenMissingFields = $("input[name=ignoreWhenMissingFields]:checked").val();
}

function triggerVerify () {
	update();
	
	$('#test-data').val(JSON.stringify(cfg));
	$('#test-channel-id').val('${channelId}');
	$('#test-form').submit ();
}

function triggerUpdate () {
	update();
	
	$('#configuration').val(JSON.stringify(cfg));;
	$('#command').submit ();
}

</script>

</h:main>
