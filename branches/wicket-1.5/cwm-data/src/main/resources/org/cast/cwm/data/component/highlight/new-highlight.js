// CAST_Highlighter requires Rangy
// A cross-browser JavaScript range and selection library
// http://code.google.com/p/rangy/

// Base structure of code according to: http://docs.jquery.com/Plugins/Authoring (as of 09/12/2012)

/*
// calls the init method
$().CAST_Highlighter();

// calls the init method - setting read only and two color definitions
$().CAST_Highlighter({
    colors: ['Y','B'],
    readonly : true
});

// calls init method - setting two colors, allows edits, and sample callback on create
$().CAST_Highlighter({
    colors: ['Y','B'],
    readonly : false,
    create : function(options) { console.log("available colors:" + options.colors); }
});

// Callback definitions
create      - highligher initialization complete
before      - before highlighter updates are spplied (when a range is available and apply/erase clicked)
add         - after highlight section is added
remove      - after highlight section is removed
update      - after highlight section is added or removed
show        - when a highlighter is turned on and made visible
hide        - when a highlighter is turned off and no longer visible
hintShow    - when a hint is turned on and made visible
hintHide    - when a hitn is turned off and no longer visible

*/

(function($) {
    var settings = {
        // Default Settings
        'colors'            : ['Y'],        // Available highlighter color definitions, this should be an array
        'readonly'          : false,        // Whether the highlights on this page can be changed

        // State Indicators
        'currentColor'      : null,         // Highlighter color: null=none, 'X'=Color X
        'currentRange'      : null,         // Selection item (Rangy range)
        'currentScope'      : null,         // Highlighting scope id
        'currentHint'       : null,         // Hint class name
        'currentCompare'    : null,         // Color that hint is comparing against
        'saveState'		    : false,        // save state back to db for persistence

        // Callback hooks
        'create'            : null,
        'before'            : null,
        'add'               : null,
        'remove'            : null,
        'update'            : null,
        'show'              : null,
        'hide'              : null,
        'hintShow'          : null,
        'hintHide'          : null,
    };

    var methods = {
        // Initialize
        init : function(options) {
            settings = $.extend(settings, options);

            // Setup words to allow highlights
            methods.initWords();
            // Setup hints/authored highlights
            methods.initHint();
            // Indicators on buttons to show that highlights exist
            methods.indicatorsUpdate();

            methods._trigger("create");
        },

        /**
         * Add <span class='word'> tags to every word in every '.hlregion'
         */
        initWords : function() {
            $('div.hlregion').each(function() {
                $(this).data('wordCount', 0);
                $('.hlpassage', this).each(function() {
                    methods.wordsAddSpan(this);
                });
            });
        },

               /**
         * Add a hint <span class="key keyhint"> to the first highlight span if the author did not
         * create a hint.  If there are no authored highlights, then this won't do anything.
         */
        initHint : function() {
            if ($('span').hasClass('key')) {
                if (!($('span').hasClass('keyhint'))) {
                    $('span.key:first').addClass("keyhint");
                }
            }
        },

        /**
         * Set new highlighting mode and update highlights
         *
         * Modes:
         * ======
         * - No Selection + Color: toggle color on/off
         * - No Selection + Erase: toggle erase on/off
         * - No Selection + Different Color: toggle old color off, new color on
         * - Selection + Color off: toggle color on, add/revise highlight
         * - Selection + Color on: add/revise highlight
         * - Selection + Color on + Erase: remove/revise highlight
         *
         * @param {String} clicked - color character, 'E' (erase), or null
         * @param {String} id - optional id restriction
         */
        modify : function(clicked, id) {

            if (clicked === "none") { clicked = null; }
            if (id === undefined) { id = null; }

            // Check for valid color
            var $validColor = ($.inArray(clicked, settings.colors) != -1) ? true : false;

            var $currColor = settings.currentColor;
            var $currScope = settings.currentScope;
            var $saveState = settings.saveState;
            
            // Check for and update with new range if needed
            var $currRange = (settings.currentRange == null) ? methods.rangeGet() : settings.currentRange;

            // Find scope
            var $controlScope = (id ? $("#" + id) : $("#globalHighlight"));
            var $highlightScope = (id ? $("#" + id) : $("body"));

            // Turn off highlight (No Selection + Same Color)
            if (clicked == null || ($currRange == false && $currColor == clicked && $currScope == id) || (settings.readonly == true && $currColor == clicked && $currScope == id)) {

            	// store the state change to off
            	if ($saveState) {
                	highlightStateChangeEvent($currColor, "false");
            	}

                // Store/unset existing highlighting
                if ($currColor != null) {
                    //methods.wordSaveHighlights($currScope);
                    $("." + $currColor).removeClass($currColor);
                }

                // Reset highlighter
                settings.currentColor = null;
                settings.currentScope = null;
                settings.currentRange = null;
                methods.indicatorsBlank();

                // Turn off hints
                methods.hintDisplay(null, null, null, false, null);

                // Hide higlights
                methods.wordShowHighlights(null);

            // Turn on highlighting color
            } else if ($validColor) {

                // Check for new color change
                if ($currColor != clicked) {

                    // Store/unset previous color highlighting
                    if ($currColor != null) {
                        //methods.wordSaveHighlights($currScope);
                        $("." + $currColor).removeClass($currColor);
                        methods._trigger("hide");
                    }

                    // Turn off hints
                    methods.hintDisplay(null, null, null, false, null);

                    // Display highlights for new color
                    methods.wordShowHighlights(clicked, id);

                    // store the state change of new color to on
                    if (clicked != null && $saveState) {
                    	highlightStateChangeEvent(clicked, "true");
                    }
                }

                // Set new mode
                settings.currentColor = clicked;
                settings.currentScope = id;

                methods.indicatorsBlank();

                // Enable controls (highlighter and eraser)
                $controlScope.find(".control" + clicked).addClass("on");
                if (!settings.readonly) {
                    $controlScope.find(".controlE").addClass("enable");
                }

                // Check for selected range
                if ($currRange != false) {
                    methods._trigger("before");
                    // Update highlighted items
                    methods.rangeHighlight(clicked, id, $currRange, false);
                }

            // Clicked the eraser (no selection)
            } else if (clicked == 'E') {

                if (settings.readonly) {
                    //throw "Error! Read-only highlights should not be able to erase!";
                    return false;
                }

                // Do nothing if not currently highlighting in this scope
                if ($currColor == null || $currScope != id) {
                    return;
                }

                // Check for selected range
                if ($currRange != false) {
                    methods._trigger("before");
                    // Force update with erase on
                    methods.rangeHighlight($currColor, id, $currRange, true);
                }
            }

            // Adjust display style classes
            if (settings.currentColor != null) {
                $highlightScope.addClass(settings.currentColor);
            }

            return false;
        },

        /**
         * Return current selection of false if nothing selected.
         */
        rangeGet : function() {
            // Check to make sure there is a selection
            var s = rangy.getSelection();
            var r = s.rangeCount ? s.getRangeAt(0) : null;
            if (!r) return false;
            if ($.trim(r) == "") return false;
            settings.currentRange = r;
            return r;
        },

        rangeHighlight : function(color, id, range, erase) {

            if (settings.readonly) {
                return;
            }

            settings.currentColor = color;

            // Get HTML and put into holding
            var selHTML = range.toHtml();
            // Place into temp node for parsing
            var tempHTML = $("<div/>").append(selHTML);

            // Pull out words
            var wordList = new Array();
            $(tempHTML).find(".word").each(function() {
                wordList.push($(this).attr("id"));
            });

            // Kill temp node
            $(tempHTML).empty();

            if (!erase) {
                // Find all instance of current highlight color
                var allSelector = '.' + color + 'highlightedWords';
                var allNums = new Array();

                $(allSelector).each(function() {
                    var highlightedWords = $(this).attr('value');
                    // This section has no words; move to the next
                    if (highlightedWords == null || highlightedWords == '') { return true; }

                    // Remove spaces and split comma delimited string
                    highlightedWords = highlightedWords.replace(/\s*,\s*/,',');
                    var wordNums = highlightedWords.split(',');
                    allNums = allNums.concat(wordNums);
                });
            }

            // Modify Highlight
            if (wordList.length > 0) {
                // Find acceptible words for highlight modfication
                // Also check to see if any existing hightlighted words are selected
                var validWordList = new Array();
                var matchCount = 0;

                for (var i in wordList) {
                    word = $("#" + wordList[i]);
                    // Verify position of 'words' in 'hlpassage' in 'hlregion'
                    if (word.closest('.hlregion .hlpassage').length > 0) {

                        if ((i == 0) && (range.startOffset == $.trim(word.text()).length)) {
                            // Do not highlight previous word that is not visibly selected (Firefox)
                        } else if ((i == ((wordList.length)-1)) && (range.endOffset == 0)) {
                            // Do not highlight next word that is not visibly selected (Firefox)
                        } else if (range.startOffset == range.endOffset && !$.browser.msie) {
                            // Do not highlight current word that is not visibly selected (Firefox)
                        } else {
                            // Looks good, add to valid word list
                            valWord = wordList[i];
                            validWordList.push(valWord);
                            if ($.inArray(methods.wordGetNum(word), allNums) != -1) {
                                matchCount += 1;
                            };
                        }
                    }
                }

                // Update higlighted words
                if (validWordList.length > 0) {
                    // Determine update mode (append or remove)
                    if ( (matchCount > 0) && (matchCount == validWordList.length) ) {
                        var remove = true;
                    } else {
                        var remove = false;
                    }

                    for (var i in validWordList) {
                        modWword = $("#" + validWordList[i]);
                        // Check for Erase/Remove mode
                        if (erase || remove) {
                            modWword.removeClass('wordHighlighted');
                        } else {
                            modWword.addClass('wordHighlighted');
                        }
                    }

                    if (erase || remove) {
                        methods._trigger("remove");
                    } else {
                        methods._trigger("add");
                    }
                }

                methods.wordSaveHighlights(id);

            } else {
                // Check to see if only single word selected
                if (range.startContainer.nodeType == 3) {
                    // Text node
                    parNode = $(range.startContainer).closest(".word");
                    if (parNode.length > 0) {
                        parNodeNum = methods.wordGetNum(parNode);

                        if (erase) {
                            // Forced Erase mode
                            parNode.removeClass('wordHighlighted');
                        } else if ($.inArray(parNodeNum, allNums) != -1) {
                            // Word previously highlighted - now erase it
                            parNode.removeClass('wordHighlighted');
                        } else {
                            parNode.addClass('wordHighlighted');
                        }
                        methods.wordSaveHighlights(id);
                    }
                }
            }

            // Remove any unused selections
            methods.rangeClear();
        },

        /**
         * Remove selection/range
         */
        rangeClear : function() {
            if (window.getSelection) {
                if (window.getSelection().empty) {  // Chrome
                    window.getSelection().empty();
                } else if (window.getSelection().removeAllRanges) {  // Firefox
                    window.getSelection().removeAllRanges();
                }
            } else if (document.selection) {  // IE?
                document.selection.empty();
            }
            settings.currentRange = null;
        },

        /**
         * Blanks all indicators (buttons) throughout the document.
         * Any time highlighting changes, this is called.
         *
         * This does not have a scope restriction since changing mode
         * may also change scope.
         *
         */
        indicatorsBlank : function() {
            $.each(settings.colors, function(index, value) {
                $('.control' + value).removeClass("on");
            });
            $('.controlE').removeClass("enable on");
        },

        /**
          * Set indicators to show highlights exist
          */
        indicatorsUpdate : function() {
            for (var i=0; i < settings.colors.length; i++) {
                var color = settings.colors[i];
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
        },

        /**
         * Parse text in element for words; add a sequentially-numbered <span> around each word.
         *
         * @param {Object} elt
         */
        wordsAddSpan : function(elt) {
            var $elt = $(elt);

            // Skipped flagged elements
            if ($elt.closest(".nohlpassage").size() > 0) { return; }

            $elt.contents().each(function() {
                // NodeType 1=Element, 3=Text
                if(this.nodeType==1) {
                    methods.wordsAddSpan(this);
                } else if (this.nodeType==3) {
                    var html = '';
                    var part = 0;
                    var text = this.data.replace( /\s+/g, ' ');
                    var pos = 0;
                    var index;
                    var regionElt = $(this).closest('.hlregion');

                    if (regionElt.data('wordCount') === undefined) {
                        regionElt.data('wordCount', 0);
                    }

                    // Skip pure whitespace nodes
                    if ($.trim(text) == "") {
                        return;
                    }
                    // Split a text node into words and construct an HTML string of <span> wraps
                    while (pos < text.length && (index = text.indexOf(' ', pos)) >= 0) {
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

                    // Insert new HTML and remove old text node
                    $(this).before($replacementHtml).remove();

                    // For some reason, jQuery drops leading space when doing before(jQuery) vs. before(htmlString)
                    if (html.charAt(0) === ' ') { $replacementHtml.before(' '); }
                }
            });
        },

        /**
         * Clears existing visible highlighting and then displays
         * a new set of highlights based on the contents of a hidden
         * form field.  If color is null, simply hides all highlighting.
         *
         * @param {String} color - color to display
         * @param {String} id - optional restricted scope
         */
        wordShowHighlights : function(color, id) {

            // Remove old highlighting, if any
            $('.hlregion .wordHighlighted').removeClass('wordHighlighted');

            // No color so we are done
            if (color == null) {
                methods._trigger("hide");
                return;
            }

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
                if( highlightedWords == null || highlightedWords == '') { return true; }

                // Remove spaces and split comma delimited string
                highlightedWords = highlightedWords.replace(/\s*,\s*/,',');
                var wordNums = highlightedWords.split(',');
                for( var i=0; i<wordNums.length; i++ ) {
                    var wordNum = wordNums[i];
                    if( wordNum != '') {
                        methods.wordAddHighlight($('#' + regionElt.attr('id') + '_' + wordNum));
                    }
                }
            });
            methods._trigger("show");
        },

        /**
         * Add highlighting color to a word.
         *
         * @param {Object} elem
         */
        wordAddHighlight : function(elem) {
            $(elem).addClass("wordHighlighted");
            $(elem).find(".word").each(function() {
                methods.wordAddHighlight($(this));
            });
        },

        /**
         * Returns the word number (within an .hlregion) of
         * the provided element.
         *
         * For now, just reads the ID and returns the bit that is the word number
         *
         * @param {Object} elt
         */
        wordGetNum : function(elt) {
            var id = $(elt).attr('id');
            return id.substring(id.lastIndexOf('_') + 1);
        },

        /**
         * Read the visible highlighted words and copy their locations
         * into a hidden form field.
         *
         * @param {String} id - optional location restriction
         */
        wordSaveHighlights : function(id) {

            if (settings.readonly) {
                return;
            }

            try {
                $((id ? '#' + id : '') + '.hlregion').each(function() {

                    var words = '';
                    var phrases = '';
                    var lastNum = 0;

                    $('.wordHighlighted', this).each(function() {
                        var wordNum = methods.wordGetNum(this);

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

                    $('.' + settings.currentColor + 'highlightedWords', this).val(words);
                    $('.' + settings.currentColor + 'highlightedPhrases', this).val(phrases);

                });
            } catch( Error ) {alert (Error)}

            methods.rangeClear();
            methods.indicatorsUpdate();

            methods._trigger("update");
        },

        /**
         * Toggles the display of model highlights by adding the
         * 'hintClass' to given scope (or body).
         * Optional 'show' parameter forces state.
         *
         * @param {Event}  event - the click that triggered this call
         * @param {String} scope - restricted scope, or null
         * @param {String} color - color of user highlight to compare against
         * @param {String} show - true to display; false to hide; null to toggle
         * @param {String} hintClass - className of hint type (eg: "hint" if you want hints, "model" if you want models (default))
         * @param {String} btnId - btnId to activate
         * @param {Boolean} compare - is this a comparison highlight
         */
        hintDisplay : function(e, scope, color, show, hintClass, btnId, compare) {
            if (scope === undefined) { scope = null; }
            if (color === undefined) {
                color = null;
            } else {
                // Check for valid color
                color = ($.inArray(color, settings.colors) != -1) ? color : null;
            }
            if (show === undefined) { show = null; }
            if (hintClass === undefined) { hintClass = null; }

            if (show == false && hintClass == null) {
                // All hints being forced off
                $('.highlightHelper').find(".collapseBox").removeClass("expOpen");
                $('.highlightHelper').find(".collapseBody").hide();
                $('.highlightHelper').find(".toggle").attr("alt", lang['EXPAND']).attr("title", lang['EXPAND']).removeClass("expOpen");
            }

            if (show == false && settings.currentHint == null) {
                return;
            }

            // Find scope
            var $controlScope = (scope ? $("#" + scope) : $("#globalHighlight"));
            var $controlHelper = ($('.control' + color + '.helper', $controlScope).length > 0) ? true : false;
            var $highlightScope = (scope ? $("#" + scope) : $("body"));

            if (!$controlHelper) {
                // Turn off any current highlights
                if (settings.currentColor != null) {
                //if (settings.currentColor != color) {
                    $("." + settings.currentColor).removeClass(settings.currentColor);
                    methods.wordShowHighlights(null);
                }
                settings.currentColor = null;
                settings.currentScope = null;
                settings.currentRange = null;
                methods.indicatorsBlank();
            }

            // Hide all previous hint displays
	        $("." + settings.currentHint).not($highlightScope).removeClass(settings.currentHint);
	        $("." + settings.currentCompare).not($highlightScope).removeClass(settings.currentCompare);

	        // Hide all other hints of same type not in current scope
            $("." + hintClass).not($highlightScope).removeClass(hintClass);
            $("." + color).not($highlightScope).removeClass(color);

            var $currTarget = null;
            var $btnTarget = ($("#" + btnId).length) ? $("#" + btnId) : null;
            // Reset all other hint toggle items not the specified button
            if ($btnTarget != null) {
                e.stopImmediatePropagation();
                $('.highlightHelper').find(".collapseBox").not($btnTarget).removeClass("expOpen");
                $('.highlightHelper').find(".collapseBox").not($btnTarget).find(".collapseBody").hide();
                $('.highlightHelper').find(".toggle").not($btnTarget).attr("alt", lang['EXPAND']).attr("title", lang['EXPAND']).removeClass("expOpen");
            } else {
                $('.highlightHelper').find(".collapseBox").not($highlightScope).removeClass("expOpen");
                $('.highlightHelper').find(".collapseBody").not($highlightScope).hide();
                $('.highlightHelper').find(".toggle").not($highlightScope).attr("alt", lang['EXPAND']).attr("title", lang['EXPAND']).removeClass("expOpen");
            }

            // Is scope currently showing hints?
            if ( settings.currentHint != null && settings.currentHint != hintClass) {
                var showing = $highlightScope.hasClass(hintClass);
            } else {
                var showing = $highlightScope.hasClass(settings.currentHint);
            }

            // Adjust state, if necessary
            if (showing || show == false) {
                // Turn off
                if (settings.currentHint != null) {
                    $highlightScope.removeClass(settings.currentHint);
                }
                if ( ((settings.currentCompare != null) && (settings.currentCompare != color)) || ((settings.currentCompare != null) && (color == null)) ) {
                    // Remove highlight color and disable controls
                    $highlightScope.removeClass(settings.currentCompare);
                    methods.wordShowHighlights(null);
                    $controlScope.find(".control" + color).removeClass("on");
                    $controlScope.find(".controlE").removeClass("enabled");
                }
                // Close specified toggle item
                if ($("#" + btnId + ".collapseBox.expOpen").length) {
                    toggleChildBox($("#" + btnId), null);
                }
                $highlightScope.removeClass(hintClass);
                settings.currentColor = color;
                settings.currentColor = scope;
                settings.currentHint = null;
                settings.currentCompare = null;
                methods._trigger("hintHide");
            } else {
                // Turn on
                if (settings.currentHint != null) {
                    $highlightScope.removeClass(settings.currentHint);
                }
                if (settings.currentCompare != null) {
                    $highlightScope.removeClass(settings.currentCompare);
                    methods.wordShowHighlights(null);
                }
                if (color != null) {
                    $highlightScope.addClass(color);
                    methods.wordShowHighlights(color, scope);
                    // Enable controls (highlighter and eraser)
                    $controlScope.find(".control" + color).addClass("on");
                    if (!settings.readonly) {
                        $controlScope.find(".controlE").addClass("enable");
                    }
                }
                if (compare) {
                    // Check to see if there are highlights to compare against
                    if ($('input.' + color + 'highlightedWords').attr('value') == '') {
                        var modalWin = $('#noHighlightModal');
                        // Get modal size
                        modalWin.css("position", "absolute").css("left", "-9999px").css("display", "block");
                        var modalWidth = modalWin.outerWidth();
                        var modalHeight = modalWin.outerHeight();
                        modalWin.css("left", "auto").css("display", "none");
                        if ($btnTarget != null) {
                            // Get button offset and width
    		                var btnOffset = $btnTarget.offset();
    		                var btnWidth = $btnTarget.outerWidth(true);
    		                // Position modal
    		                var newLeft = (btnOffset.left + (btnWidth/2)) - (modalWidth/2);
    		                modalWin.css("position", "absolute");
                            modalWin.css("top", btnOffset.top);
                            modalWin.css("right", "auto");
                            modalWin.css("left", newLeft + "px");
                        } else {
                            // Use center() from DialogBorder.js
                            modalWin.center();
                        }
                        // Store trigger
		                modalWin.data("detailTrigger", btnId);
		                // Show modal
		                modalWin.show();
		                modalWin.attr("tabindex", "-1");
	                    modalWin.get(0).focus();
		                return;
		            }
                }

                // Open specified toggle item
                if ($("#" + btnId + ".collapseBox").not(".expOpen").length) {
                    toggleChildBox($("#" + btnId), null);
                }
                $highlightScope.addClass(hintClass);
                settings.currentColor = color;
                settings.currentColor = scope;
                settings.currentHint = hintClass;
                settings.currentCompare = color;
                methods._trigger("hintShow");
            }
        },

        /**
         * Callback trigger function
         *
         * @param {String} callback function name
         */
        _trigger : function(callback) {

            // Restrict informtion that is sent back
            var options = {
                'colors'            : settings.colors,
                'readonly'          : settings.readonly,
                'currentColor'      : settings.currentColor,
                'currentRange'      : settings.currentRange,
                'currentScope'      : settings.currentScope,
                'currentHint'       : settings.currentHint,
                'currentCompare'    : settings.currentCompare,
            };

            //console.log(callback);
            //console.log(settings);

            if ($.isFunction(settings[callback])) {
                settings[callback](options);
            }
        },
    };

    $.fn.CAST_Highlighter = function(method) {

        // Method calling logic
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || ! method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' +  method + ' does not exist on jQuery.CAST_Highlighter');
        }
    };

})(jQuery);


// Helper functions for backwards compatibility
function changeMode(arg1, arg2) {
    $().CAST_Highlighter('modify', arg1, arg2);
    return false;
}

function toggleModelHighlightDisplay(arg1, arg2, arg3, arg4) {
    $().CAST_Highlighter('hintDisplay', arg1, arg2, arg3, arg4);
    return false;
}

// log the state change - to be stored in the db for persistence
function highlightStateChangeEvent(highlightColor, highlightOn) {
	wicketAjaxGet(highlightStateChangeCallbackUrl + '&highlightColor=' +  highlightColor + '&highlightOn=' + highlightOn , function() {}, function() {});
}
