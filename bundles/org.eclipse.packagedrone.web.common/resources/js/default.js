'use strict';

/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/

/* tags */

+function ($) {
	'use strict';
	
	var Taglist = function (element, options) {
		this.$element = $(element);
		this.id = Math.random();
		this.options = options;
		
		this.$list = $('.drone-tag-list-content', this.$element);
		
		if ( this.options.validator != null && this.options.validatorMessagesContainer) {
			this.$val = $(this.options.validatorMessagesContainer);
		}
		
		var that = this;
		
		$('[data-add-trigger="drone-tag"]', this.$element).click(function(){
			that.addFromInput ($(this));
		});
		$('[data-add="drone-tag"]', this.$element).keydown (function(e) {
			if ( e.which == 13 ) {
				e.preventDefault();
				that.addFromInput ($(this));
			}
		});
		
		if ( this.options.data == null ) {
			this.options.data = [];
		}
		
		var i = 0;
		this.options.data.forEach ( function (entry) {
			that.appendRenderTag (entry, i );
			i++;
		} );
	};
	
	var ItemHandler = function (taglist, element, index, draggable) {
		this.taglist = taglist;
		this.element = element;
		this.index = index;

		var that = this;
		
		this.element.data("drone.tag.itemHandler", this);
		
		$('[data-dismiss="drone-tag"]', element).click(function(){
			that.handleDeleteTag ();
		});
		
		if ( draggable ) {
			
			this.element.on("dragstart", function (e) {
				that.element.addClass ("drone-tag-dragging");
				
				that.taglist.dragSource = that;
				
				e.originalEvent.dataTransfer.effectAllowed = "move";
				e.originalEvent.dataTransfer.setData("text/html", that.element.innerHTML);
			});
			this.element.on("dragover", function(e) {
				if ( e.preventDefault ) {
					e.preventDefault();
				}
				
				if ( that.taglist.dragSource == null ) return; // different instance
				
				var handler = $(this).data("drone.tag.itemHandler");
				if ( handler == null ) return; // no handler
				
				if ( handler != that.taglist.dragSource ) {
					handler.setOver(true); // different element
				}
				
				e.originalEvent.dataTransfer.dropEffect = "move";
				
				return false;
			});
			this.element.on("dragenter", function(e) {
				if ( e.preventDefault ) {
					e.preventDefault();
				}
				// we can't use dragenter/dragleave since leave fires also when entering the button inside the element
			});
			this.element.on("dragleave", function(e) {
				var handler = $(this).data("drone.tag.itemHandler");
				
				if ( handler == null || handler.taglist != that.taglist )
					return;
				
				handler.setOver(false);
			});
			this.element.on("drop", function(e) {
				if ( e.stopPropagation) {
					e.stopPropagation ();
				}
	
				if ( that.taglist.dragSource == null ) return; // different instance
				
				var dragSource = that.taglist.dragSource;
				
				if ( dragSource != that ) {
					that.swap ( dragSource );
				}
				
				return false;
			});
			this.element.on("dragend", function(e) {
				that.element.removeClass ("drone-tag-dragging");
				
				$('.drone-tag-over', that.taglist.$element).removeClass ( "drone-tag-over");
				that.taglist.dragSource = null;
			});
		
		}
	};

	ItemHandler.prototype.handleDeleteTag  = function () {
		// clean up follow up indices
		this.element.nextAll().each( function (idx, ele) {
			var handler = $(this).data("drone.tag.itemHandler");
			handler.index--;
		});
		// remove UI element
		this.element.detach();
		// remove data element
		this.taglist.options.data.splice(this.index, 1);
	};
	
	ItemHandler.prototype.setOver = function ( state ) {
		this.element.toggleClass ( "drone-tag-over", state );
	};
	
	ItemHandler.prototype.swap = function ( other ) {
		if ( this.index > other.index )
			other.element.before ( this.element );
		else
			this.element.before ( other.element );
		
		var oldIndex = this.index;
		var oldEntry = this.taglist.options.data[this.index];
		
		this.taglist.options.data[this.index] = this.taglist.options.data[other.index];
		this.index = other.index;
		
		this.taglist.options.data[other.index] = oldEntry;
		other.index = oldIndex;
	};
	
	Taglist.DEFAULTS = {
			labelProvider: function ( entry, element ) { return element.text(entry); },
			entryProvider: function ( data ) { return data.trim(); },
			sortable: false
	};
	
	Taglist.prototype.appendRenderTag = function ( tag, index ) {
		var content = $('<li class="drone-tag-list-item"><span class="drone-tag-list-item-label"></span><button type="button" class="close" data-dismiss="drone-tag" aria-label="Delete"><span aria-hidden="true">&times;</span></button></li>');
		var label = this.options.labelProvider(tag, $('.drone-tag-list-item-label', content));
		
		if ( this.options["hiddenInputs"] != null ) {
			var i = $(document.createElement("input"));
			
			i.attr("type","hidden");
			i.attr("name",this.options["hiddenInputs"]);
			
			if ( this.options.valueProvider != null ) {
				i.val(this.options.valueProvider(tag));
			}
			else {
				i.val(tag);
			}
			content.append (i);
		}
		
		var that = this;
		
		if ( this.options.sortable ) {
			content.addClass ("drone-tag-sortable");
			content.attr("draggable", "true");
		}
		
		new ItemHandler ( this, content, index, this.options.sortable );
				
		this.$list.append (content);
	};
	
	Taglist.prototype.add = function ( data, variant ) {
		
		if ( this.options.validator != null ) {
			try {
				this.options.validator ( data, variant );
				this.setValidationMessage ( null );
			}
			catch ( e )  {
				if ( typeof e == "string" ) {
					this.setValidationMessage ( e );
				}
				else {
					this.setValidationMessage ( e.message );
				}
				return false; // don't add
			}
		}
		
		var entry = this.options.entryProvider ( data, variant );
		if ( entry != null ) {
			this.options.data.push ( entry );
			this.appendRenderTag ( entry, this.options.data.length - 1 );			
		}
		return true;
	}
	
	Taglist.prototype.addFromInput = function (sourceElement) {
		var ele = $('[data-add="drone-tag"]', this.$element);
		
		var variant = sourceElement.data("add-trigger-variant");
		
		if ( ele != null ) {
			if ( this.add(ele.val(), variant) ) {
				ele.val('');	
			}
		}
	}
	
	Taglist.prototype.setValidationMessage = function ( msg )  {
		if ( this.$val == null )
			return;
		
		var $sc = this.options.validationStatusContainer == null ? this.$element : $(this.options.validationStatusContainer);
		
		if ( msg != null ) {
			var ele = $('<span></span>');
			$sc.addClass("has-error");
			ele.text ( msg );
			this.$val.append ( ele );
		} else {
			this.$val.empty();
			$sc.removeClass("has-error");
		}
	}
	
	Taglist.prototype.cloneData = function ()  {
		return this.options.data.slice ();
	}
	
	function Plugin(option, _relatedTarget) {
		return this.each(function () {
			
			var that = $(this);
			var data = that.data('drone.taglist');
			var options = $.extend({}, Taglist.DEFAULTS, that.data(), typeof option == 'object' && option);

			if ( data == null ) {
				data = new Taglist(this, options);
				that.data('drone.taglist', data);
			}
		})
	}
	
	var old = $.fn.taglist

	$.fn.taglist             = Plugin
	$.fn.taglist.Constructor = Taglist

	$.fn.taglist.noConflict = function () {
		$.fn.taglist = old
		return this
	}
	
}(jQuery);