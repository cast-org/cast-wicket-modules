/**
 * A set of methods that will allow any element to be disabled, either by
 * adding 'disabled=true' to form elements or removing click behaviors.
 * 
 * TODO: Test efficiency.
 */

(function($) {
	
	var ajaxDisabledObjects = new Object();
	
	$.fn.extend({
		ajaxDisable: function() {
			return this.each(function() {
				
				// Create a copy of the node
				var $original = $(this);
				var $copy = $original.clone(false);
				$copy.addClass('disabled');

				// Remove 'onclick' attributes and DOM handler
				$copy.find("[onclick]").andSelf().each(function() {
					this.onclick = null;
					$(this).removeAttr('onclick');
				});
				
				// Remove 'href' behaviors, but leave attribute
				$copy.find("[href]").andSelf().each(function() {
					$(this).click(function(event) {
						event.preventDefault();
					})
				});
				
				// Disable form components				
				$copy.find(":input").each(function() {
					$(this).attr('disabled', 'true');
				})
				if ($copy.is(":input")) {
					$copy.attr('disabled', 'true');
				}
				
				// Avoid duplicate 'id' fields when creating copy, except for
				// Wicket's Ajax Indicators
				$copy.find("[id]").andSelf().not(".wicket-ajax-indicator").each(function() {
					$(this).attr('id', 'd_' + $(this).attr('id'));
				});
				
				// Modify the original's id indicators
				$original.find(".wicket-ajax-indicator[id]").each(function() {
					$(this).attr('id', 'd_' + $(this).attr('id'));
				})
				
				// Replace the original with a disabled copy
				$original.after($copy);
				$original.hide();
			});
		},
		ajaxEnable: function() {
			return this.each(function() {

				var $original = $(this);
				var $copy = $original.next('#d_' + $original.attr('id'));
				
				// Restore indicator IDs, copying over increment count from $copy
				$original.find(".wicket-ajax-indicator[id]").each(function() {
					$(this).attr('id', $(this).attr('id').substring(2));
					$(this).attr('showincrementallycount', $("#" + $(this).attr('id'), $copy).attr('showincrementallycount'));
				});
				
				// Replace disabled copy with original
				$copy.remove();
				$original.show();
				
			});
		}
	});
	
})(jQuery);
