/**
 * This is an n-Color highlighting system.  Each <div class="hlregion"> is parsed for child
 * class='hlpassage' elements.  Those elements are split by word and assigned a hook for highlighting
 * behaviors.  Each <div class="hlRegion" is assumed to have it's own 
 * <input type="hidden" class="XHighlightedWords"> where 'X' is a color's character
 * representation (e.g. 'B' for blue).  This character is used by the CSS to apply colored
 * styles to the selected words.
 * 
 * Style Guide:
 * 
 * .X .wordHighlighted {...} - A saved highlight display for color X
 * .X .wordSelectedHighlight {...} - An unsaved 'drag' highlight display color X
 * .wordCurrentHighlight {...} - The most recently added word to a 'drag.'  Used for keyboard support
 * .wordCurrentErase {...} - The most recently added word to an erasing 'drag.'
 * 
 * Functionality:
 * 
 * init(boolean) - Initialized the highlighting for this page.
 * changeMode('X') - Activate a highlighter.  Call on the same color to turn that color off.
 * changeMode('E') - Activate the eraser.  Can only be called when a highlight color is active.
 * 
 * @author jbrookover
 */

// State Indicators
var currentMode = 'none'; // Current Highlighter. 'E'=Erase, 'none'=none, 'X'=Color X
var currentScopeId = null; // Current highlighting scope
var highlighting = false; // Is a 'drag' active?
var erasing = false; // Currently erasing?
var mouseState = 'up'; // Mouse click state: 'up' or 'down'

// Drag data; contstantly updated during drag.  Note: 'startWordId' can be AFTER 'lastWordId'
var currentWordId = null; // ID of the currently selected word
var startWordId = null; // ID of the word that starts the 'drag'
var lastWordId = null; // ID of the word that ends the 'drag'

var isReadOnly = false; // Whether the highlights on this page can be changed


/**
 * Startup processing
 * 
 * @param {Object} readonly - if true, user cannot change highlights
 */
function initHighlighterTool(readonly) {

	// Set Read Only State
	isReadOnly = readonly;
	
	// Indicators on buttons to show that highlights exist
	showIndicators();

	// Add <span class='word'> tags to every word in every '.hlregion'
	$('div.hlregion').each(function() {
		$(this).data('wordCount', 0);
		$('.hlpassage', this).each(function() {
			addSpansToElement(this);	
		});
    });
	
	// Add mouse state handlers
	if (!isReadOnly) {
	    $(document)
			.bind('mousedown', documentMouseDown)
	    	.bind('mouseup', documentMouseUp);
		// $(document).bind('keydown', documentKeyDown);
	}
	
	// setup hints
	setHint();
}


/**
 * Add a hint <span = "key keyhint"> to the first highlight span if the author
 * did not create a hint.  If there are no authored highlights, then this won't
 * do anything.
 */
function setHint() {
	if ($('span').hasClass('key')) {
		if (!($('span').hasClass('keyhint'))) {
			$('span.key:first').addClass("keyhint");
		}
	}
}

/**
 * Parse text in element for words; add a sequentially-numbered <span> around each word.
 * 
 * @param {Object} elt
 */
function addSpansToElement(elt) {
	
	var $elt = $(elt);

	// Skipped flagged elements
	if ($elt.closest(".nohlpassage").size() > 0)
		return;

    $elt.contents().each (function() {

		// NodeType 1=Element, 3=Text
	  	if(this.nodeType==1) {
	  		addSpansToElement(this);
	  	} else if (this.nodeType==3) {
	        var html = '';
	        var part = 0;
	        var text = this.data.replace( /\s+/g, ' ');
	        var pos = 0;
	        var index;
			var regionElt = $(this).closest('.hlregion');
			
			// Skip pure whitespace nodes
			if ($.trim(text) == "") {
				return;
			}

			
			// Split a text node into words and construct an HTML string of <span> wraps
	        while (pos<text.length && (index = text.indexOf(' ', pos)) >= 0) {
	            var word = text.substring(pos, index);
				if( word != '') {
					var wordId = regionElt.attr('id') + "_" + regionElt.data('wordCount');
					regionElt.data('wordCount', regionElt.data('wordCount') + 1);
					
					html = html + '<span class="word" id="' + wordId + '">'
						+ word
						+ ' '
						+ '</span>';
				} else {
					html = html + ' ';
				}
				pos = index+1;
			}

			// Grab any remaining characters as the final word.
	        if (pos < text.length) {
	        	word = text.substring(pos);
	        	
				var wordId = regionElt.attr('id') + "_" + regionElt.data('wordCount');
				regionElt.data('wordCount', regionElt.data('wordCount') + 1);
	        	
				html = html + '<span class="word" id="' + wordId + '">'
	        	       + word
	        	       + "</span>";
	        }

			// Replacement HTML node
			var $replacementHtml = $(html);
			
			// Highlighting Event Handlers
			if (!isReadOnly) {
				$replacementHtml
				.bind('mousedown', highlightMouseDown)
				.bind('mousemove', highlightMouseMove)
				.bind('mouseup', highlightMouseUp)
				.bind('click', highlightClick);
			}
            
			// Insert new HTML and remove old text node
	        $(this).before($replacementHtml).remove();
			
			// For some reason, jQuery drops leading space when doing before(jQuery) vs. before(htmlString)
			if (html.charAt(0) === ' ')
				$replacementHtml.before(' ');
	  	}
	});
}


/**
 * Set indicators to show highlights exist
 * 
 * @param {Object} id - optional id restriction
 */
function showIndicators() {
	for (var i=0; i<colors.length; i++) {
		var color = colors[i];
		var anyExist = false;
		
		// Find all highlighting input fields
		$('input.' + color + 'highlightedWords').each(function() {
			var $field = $(this);
			var $localControl = $field.closest("div.hlregion").find(".control" + color);
			
			// Adjust local controls; register existence for global controls
			if ($field.attr('value') != '') {
				anyExist = true;
				$localControl.addClass("has");
			} else {
				$localControl.removeClass("has");
			}
 		});
		
		// Adjust global controls
		var $globalControl = $("#globalHighlight .control" + color); 
		if (anyExist) {
			$globalControl.addClass('has')
		} else {
			$globalControl.removeClass('has');
		}
	}
}

/**
 * Set new highlighting mode
 * 
 * @param {Object} clicked - color character, 'E' (erase), or 'none'
 * @param {Object} id - optional id restriction
 */
function changeMode(clicked, id ) {

	// Find scope
	var $controlScope = (id ? $("#" + id) : $("#globalHighlight"));
	var $highlightScope = (id ? $("#" + id) : $("body"));

	
	// Turn off Highlighting
	if (clicked == null || clicked == 'none' || (currentMode == clicked && currentScopeId == id)) {
		
		erasing = false;
		
		// Store/unset existing highlighting
		if (currentMode != 'none') {
			saveHighlightedWords(currentScopeId);
			$("." + currentMode).removeClass(currentMode);
		}	
		
		currentMode = 'none';
		currentScopeId = null;
		blankAllButtons();
		
		// Hide higlights	
        displayHighlightedWords(null);

        // Hide any visible Model or hint Highlights and reset the toggle fields
    	toggleModelHighlightDisplay(null, null, false);
        $('.highlightHelper').children(".collapseBox").removeClass("expOpen");
        $('.highlightHelper').children().find(".collapseBody").hide();
        $('.highlightHelper').children().find(".toggle").attr("alt", lang['EXPAND']).attr("title", lang['EXPAND']).removeClass("expOpen");
    	
		
        
	// Turn on Highlighting
    } else if ($.inArray(clicked, colors) != -1) {
	

    	erasing = false;

		// Store/unset previous color's highlighting
    	if (currentMode != 'none') {
			saveHighlightedWords(currentScopeId);
			$("." + currentMode).removeClass(currentMode);
		}

		// Hide any visible Model or hint Highlights
		toggleModelHighlightDisplay(null, null, false);
        // Hide any visible Model or hint Highlights and reset the toggle fields
    	toggleModelHighlightDisplay(null, null, false);
        $('.highlightHelper').children(".collapseBox").removeClass("expOpen");
        $('.highlightHelper').children().find(".collapseBody").hide();
        $('.highlightHelper').children().find(".toggle").attr("alt", lang['EXPAND']).attr("title", lang['EXPAND']).removeClass("expOpen");
    	
		// Set new mode	
    	currentMode = clicked;
		currentScopeId = id;
        blankAllButtons();

		// Enable controls (highlighter and eraser)
		$controlScope.find(".control" + clicked).addClass("on");
		$controlScope.find(".controlE").addClass("enable");

		// Display highlights for new color
        displayHighlightedWords(clicked, id);

	// Clicked the eraser
    } else if (clicked == 'E') {
		
		if (isReadOnly) 
			throw "Error! Read-only highlights should not be able to erase!";
			
		// Do nothing if not currently highlighting in this scope
		if (currentMode == 'none' || currentScopeId != id)
			return;
		
    	// Toggle Erasing buttons and state
    	if (erasing) {
			$controlScope.find(".controlE").removeClass("on");
			$highlightScope.removeClass("erasing");
    		
    	} else {
			$controlScope.find(".controlE").addClass("on");
			$highlightScope.addClass("erasing");
    	}
		
		erasing = !erasing;
    }

	
    // Adjust display style classes
	if (currentMode != 'none')
		$highlightScope.addClass(currentMode);
	
	// Adjust active section and current word
	if (!isReadOnly) {
		$(".hlactive").removeClass("hlactive");
		if (currentMode != 'none') 
			$highlightScope.addClass("hlactive");
			
		// Reset currentWordId if it is outside the scope
		if (currentScopeId != null && $("#" + currentWordId).parents("#" + currentScopeId).length == 0)
			currentWordId = null;
		
		// Identify currentWordId	
		updateCurrentWord();		
	}
}

/**
 * Toggles the display of model highlights by adding
 * class 'model' to the body.  Optional 'show' parameter
 * forces state.
 * 
 * Optionally restrict by '.hlregion' id.
 *
 * TODO: I think 'id=null' used to turn off display will fail if 
 * there is ever a display that was turned on via 'id=null.'  However,
 * this doesn't happen in Google, so we'll ignore for now.
 *
 * @param {Object} id - restricted scope, or null
 * @param {Object} compare - true to turn on/off user highlighting along with model
 * @param {Object} show - true to display; false to hide; null to toggle
 * @param {Object} modelType = "hint" if you want hints, "model" if you want models (default)
 */
function toggleModelHighlightDisplay(id, compare, show, modelType) {
	
	// default the currentClass to model for backwards compatibility
	var currentClass="model";
	if (modelType=='hint') {
		currentClass="hint";
		// collapse the compare box if it is open
	    if ($("#compareBox").hasClass("expOpen")) {
		    $("#compareBox").removeClass("expOpen");
		    $("#compareBox").find(".collapseBody").hide();
		    $("#compareBox").find(".toggle").attr("alt", lang['EXPAND']).attr("title", lang['EXPAND']).removeClass("expOpen");
	    }
	} else if (modelType=='model') {
		// collapse the hintbox if it is open
	    if ($("#hintBox").hasClass("expOpen")) {
		    $("#hintBox").removeClass("expOpen");
		    $("#hintBox").find(".collapseBody").hide();
		    $("#hintBox").find(".toggle").attr("alt", lang['EXPAND']).attr("title", lang['EXPAND']).removeClass("expOpen");
	    }
	}

	// Find scope
	var $scope = (id ? $("#" + id) : $("body"));	
	
	// Are we toggling or forcing state?
	var toggle = true;
	if ((show == true) || (show == false))
		toggle = false;
	
	// Is scope currently showing highlights?
	var showing = $scope.hasClass(currentClass);

	// Hide all other model displays
	$(".hint").removeClass("hint");
	$(".model").removeClass("model");

	// Adjust state, if necessary
	if (showing) {
		if (toggle || !show) {
			if (compare) {
				displayHighlightedWords(null);
			}
		}
	} else {
		if (toggle || show) {
			$scope.addClass(currentClass);
		}
	} 
}

/**
 * Blanks all buttons throughout the document.  Any time
 * highlighting changes, this is called.
 * 
 * This does not have a scope restriction since changing mode
 * may also change scope.
 * 
 */
function blankAllButtons() {
	$.each(colors, function(index, value) {
		$('.control' + value).removeClass("on");
	});
	
	$('.controlE').removeClass("enable on");
}

/**
 * Clears existing visible highlighting and then displays
 * a new set of highlights based on the contents of a hidden
 * form field.  If color is null, simply hides all highlighting.
 * 
 * @param {Object} color - color to display
 * @param {Object} id - optional restricted scope
 */
function displayHighlightedWords(color, id) {
	
	// Remove old highlighting, if any
    $('.hlregion .wordHighlighted').removeClass('wordHighlighted');
    
	// No color; just return
	if (color == null)
    	return;
    
	// Optionally restrict display
	var selector;
	if (id) {
		selector = '#' + id + ' .' + color + 'highlightedWords';
	} else {
		selector = '.' + color + 'highlightedWords';
	}
	
	// Display highlights
	$(selector).each(function() {
		
		var highlightedWords = $(this).attr('value');
		var regionElt = $(this).closest('.hlregion');
		
		// This section has no words; move to the next
		if( highlightedWords == null || highlightedWords == '') 
			return true;

		highlightedWords = highlightedWords.replace(/\s*,\s*/,',');
		var wordNums = highlightedWords.split(',');
	    for( var i=0; i<wordNums.length; i++ ) {
	        var wordNum = wordNums[i];
	        if( wordNum != '') {
				addUserHighlighting($('#' + regionElt.attr('id') + '_' + wordNum));
			}
	    }
	});    
}

/**
 * Read the visible highlighted words and copy their locations
 * into a hidden form field.
 * 
 * @param {Object} id - optional location restriction
 */
function saveHighlightedWords(id) {
	
	if (isReadOnly) {
		return;
	}
	
	try {
		$((id ? '#' + id : '') + '.hlregion').each(function() {
			
			var words = '';
			var phrases = '';
			var lastNum = 0;
			
			$('.wordHighlighted', this).each(function() {
				var wordNum = getWordNum(this);
			
				words = words + wordNum + ',';
				if (phrases && wordNum-lastNum>1) {
					if (phrases.charAt(phrases.length-1) == ' ')
						phrases = phrases.substring(0, phrases.length-1);
					phrases = phrases + '|';
				}
				phrases = phrases + $(this).text();
				lastNum = wordNum;	
			});
			
			if( words.charAt(words.length-1) == ',' ) {
				words = words.substring( 0, words.length-1 );
			}
			
			$('.' + currentMode + 'highlightedWords', this).val(words);
    		$('.' + currentMode + 'highlightedPhrases', this).val(phrases);
			
		});
	} catch( Error ) {alert (Error)}

    showIndicators();
}

/**
 * Returns the word number (within an .hlregion) of 
 * the provided element.
 * 
 * For now, just reads the ID and returns the bit that is the word number
 * 
 * @param {Object} elt
 */
function getWordNum(elt) {
	var id = $(elt).attr('id');
	return id.substring(id.lastIndexOf('_') + 1);
}

/***************************
 * Event Handler Functions *
 ***************************/

/**
 * Global mouse handler to set mouse status.  It also prevents
 * the default behavior if a highlighter is active.  This allows
 * a user to click on a non-'.word' element and still
 * have highlighting occur when making a highlighting 'drag.'
 * 
 * TODO: This prevents right clicks when highlighting enabled
 */
function documentMouseDown () {
    mouseState = 'down';
    return currentMode == 'none' ? true : false;
}

/**
 * Global mouse handler to set mouse status.  It also prevents
 * the default behavior if the highlighter is active.  This allows
 * a user to release the mouse on a non-'.word' element
 * and still complete a highlighting 'drag.'
 * 
 * TODO: This prevents right clicks when highlighting enabled
 * 
 * @param {Object} e
 */
function documentMouseUp (e) {
    
    mouseState = 'up';
	
	// We're highlighting and ended on a non-'.word' element
	// Trigger highlightMouseUp()
	if (highlighting && !$(e.target).hasClass('word')) {
		return highlightMouseUp();
	}

    return true; // Default Behavior
}

/**
 * This has been ignored until we get mouse highlighting working.
 * @param {Object} e
 */
function documentKeyDown (e) {
    if( ! highlighting ) return true;
    if( !e ) var e = window.event;
    var c = 0;
    if( e.keyCode ) c = e.keyCode;
    else if( e.which ) c = e.which;
    //alert( "Key down: #" + c );
    if( c == 32 || c == 72 || c == 104 ) {  // // SPACEBAR, H, h
        changeMode('highlight');//FIXME
    } else if( c == 8 ) { // BACKSPACE
        eraseUserHighlighting($('#w'+currentWordNum));
       // $('#w'+currentWordNum).removeClass('wordHighlighted');
        changeCurrentWord( currentWordNum -1 );
    } else if( c == 37 ) {   // LEFT ARROW
        changeCurrentWord( currentWordNum -1 );
        if( erasing )
            eraseUserHighlighting($('#w'+currentWordNum));
            //$('#w'+currentWordNum).removeClass('wordHighlighted');
        if( currentMode != 'none')
            //$('#w'+currentWordNum).addClass('wordHighlighted');
            addUserHighlighting($('#w'+currentWordNum));
    } else if( c == 39 ) {  // RIGHT ARROW
        changeCurrentWord( currentWordNum +1 );
        if( erasing )
            eraseUserHighlighting($('#w'+currentWordNum));
            //$('#w'+currentWordNum).removeClass('wordHighlighted');
        if( currentMode != 'none')
            //$('#w'+currentWordNum).addClass('wordHighlighted');
            addUserHighlighting($('#w'+currentWordNum));
    } else if( c == 69 || c == 101 ) {   // E, e
        changeMode('erase'); // FIXME
    } else if( c == 65 || c == 97 ) {   // A, a
        eraseAll();
    }
    return true;
}

/**
 * Bind to prevent default click behavior.  This allows you to
 * highlight inline links.
 * 
 */
function highlightClick () {
	return currentMode == "none";
}

/**
 * MouseDown handler for ".word" elements.  Handles the
 * beginning of a highlighting 'drag.'
 * 
 * @param {Object} e
 */
function highlightMouseDown (e) {
	if (e.which != 1 
		|| currentMode == 'none' 
		|| (currentScopeId != null && $(this).parents("#" + currentScopeId).length == 0)) {
		return true;
	}
	// Change state
    mouseState = 'down';
    highlighting = true;
	
	// Store data for highlighting 'drag'
    startWordId = $(this).attr('id');
    lastWordId = startWordId;
	currentWordId = lastWordId;
	
	// Show the current 'drag'
    showCurrentRegion();
	
	// Update (in this case, hide) current Word Marker
	updateCurrentWord();
	
    return false;
}

/**
 * MouseMove handler for ".word" elements.  Handles the duration
 * of a highlighting 'drag.'
 */
function highlightMouseMove (e) {
    if (mouseState == 'up' 
		|| currentMode == 'none' 
		|| (currentScopeId != null && $(this).parents("#" + currentScopeId).length == 0)) {
		return true;
	}
	
	// Normally set by highlightMouseDown, but use may have started
	// the highlighting 'drag' outside of a '.word' element.
	highlighting = true;

	// Clear current Word Pointer
    $('.wordCurrentErase').removeClass('wordCurrentErase');
    $('.wordCurrentHighlight').removeClass('wordCurrentHighlight');
	
	// Start of 'drag', if user started highlighting outside of a '.word' element.
    if (!startWordId) {
		startWordId = $(this).attr('id');
	}
	
	// End of 'drag'
    lastWordId = $(this).attr('id');
	currentWordId = lastWordId;	
	
    // Erase current 'drag'
    removeCurrentRegion();
	
	// Re-draw current 'drag'
	showCurrentRegion();
  
    return false;
}

/**
 * MouseUp handler for ".word" elements.  Handles the end of
 * a highlighting 'drag.'
 */
function highlightMouseUp () {
	
	mouseState = 'up';

	if (currentMode == 'none'
		|| (currentScopeId != null && $(this).parents("#" + currentScopeId).length == 0)) {
		return true;
	}

    if(startWordId == null) {
		return false;
	}
	
	// Erase current 'drag' styles
    removeCurrentRegion();
	
	// Convert 'drag' into actual highlighting
    selectWords();
	
	// Save actual highlighting into the hidden form fields
	saveHighlightedWords(currentScopeId);
	
	// Reset 'drag' data
	currentWordId = lastWordId;
	startWordId = null;
  	lastWordId = null;
	highlighting = false;
  
	// Redraw current word marker
  	updateCurrentWord();
    
	return false;
}

/**
 * Show a highlighting 'drag' from 'startWordId' to 'lastWordId'.  Since
 * highlighting can go in both directions, this function does NOT assume
 * that 'startWordId' comes before 'lastWordId' in the DOM.
 * 
 */
function showCurrentRegion () {
	
	var startIndex = $('#' + startWordId).index('.word'); // Index within words
	var endIndex = $('#' + lastWordId).index('.word');
	
	// Check to see if highlight 'drag' was backwards
	if (startIndex > endIndex) {
		var temp = startIndex;
		startIndex = endIndex;
		endIndex = temp;
	}
	
	// Apply 'drag' to '.word' spans between startWordId and endWordId (inclusive)
	$(".word").slice(startIndex, endIndex + 1).each(function() {
		var word = $(this);
		if(erasing && wasUserHighlighted(word))
        	word.addClass('wordSelectedErase');	
    	else if( currentMode != 'none' && !wasUserHighlighted(word))
        	word.addClass('wordSelectedHighlight');
	});
	
}

/**
 * Remove all highlighting 'drag' indicators page-wide.
 */
function removeCurrentRegion () {
    $('.wordSelectedErase').removeClass('wordSelectedErase');
    $('.wordSelectedHighlight').removeClass('wordSelectedHighlight');
}

/**
 * Convert the current highlighting 'drag' selection
 * to actual highlighting class attributes.
 */
function selectWords() {
	
	var startIndex = $('#' + startWordId).index('.word'); // Index within all "word" elements
	var endIndex = $('#' + lastWordId).index('.word');
	
	// Check to see if highlight 'drag' was backwards
	if (startIndex > endIndex) {
		var temp = startIndex;
		startIndex = endIndex;
		endIndex = temp;
	}

	// Apply 'drag' to '.word' spans between startWordId and endWordId (inclusive)
	$(".word").slice(startIndex, endIndex + 1).each(function() {
		var word = $(this);
		if(erasing)
			eraseUserHighlighting(word);  
      	else if(currentMode != 'none')
          	addUserHighlighting(word);
	});
}


/**
 * Displays the current word marker (for keyboard navigation).
 */
function updateCurrentWord() {
	
	$('.wordCurrentErase').removeClass('wordCurrentErase');
	$('.wordCurrentHighlight').removeClass('wordCurrentHighlight');
	
    if(currentWordId == null || mouseState == 'down') {
		return true;
	}
	
	if (erasing) 
		$('#' + currentWordId).addClass('wordCurrentErase');
	else if (currentMode != 'none') 
		$('#' + currentWordId).addClass('wordCurrentHighlight');		
}

/**
 * This presumably erases all the highlighting.  It does not
 * change any form fields.
 */
function eraseAll() {
    if( ! confirm( "Are you sure you want to erase all your highlighting?" ))
        return;
    $('.hlregion .wordHighlighted').removeClass('wordHighlighted');
}

/**
 * Remove highlighting from a word.  This should not be confused
 * with the style change that is applied during a highlighting
 * 'drag.'
 * @param {Object} elem
 */
function eraseUserHighlighting(elem) {
	$(elem).removeClass("wordHighlighted");
	$(elem).children(".wordHighlighted").each(function() {
		eraseUserHighlighting($(this));
	});
}

/**
 * Add highlighting to a word.  This should not be confused
 * with the style change that is applied during a highlighting
 * 'drag.'
 * @param {Object} elem
 */
function addUserHighlighting(elem) {
	$(elem).addClass("wordHighlighted");
	$(elem).children(".word").each(function() {
		addUserHighlighting($(this));
	});
}

/**
 * Determines if a word was highlighted.  This does not
 * include an active 'drag' - this returns true only if 
 * the word was highlighted by a previous 'drag.'
 * 
 * @param {Object} elem
 */
function wasUserHighlighted(elem) {
	if($(elem).hasClass("wordHighlighted"))
	  return true;
	return false;
}

/**
 * if there are no user highlights for the color specified, 
 * then don't allow compare - popup modal warning
 */
function showCompareHighlights(e, highlightColor, node) {
	// unbind the regular click action for this collapse box
	if (e != null) e.stopPropagation();
    $(node).unbind('click');
    
    // if there are no user highlights then popup modal
	if ($('input.' + highlightColor + 'highlightedWords').attr('value') == '') {
		$('#noHighlightModel').show();
	} else { 
        // Rebind collapse box
        $(node).bind("click", function(event) {
            toggleChildBox($(node).parents('.collapseBox').eq(0), event);
        });
        // click collapse box to hide/display contents
        toggleChildBox($(node).parents('.collapseBox').eq(0), null);

        // show the comparison highlights
		toggleModelHighlightDisplay(null, false, null, 'model');
	}	
}