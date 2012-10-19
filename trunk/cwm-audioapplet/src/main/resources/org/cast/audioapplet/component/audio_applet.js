/* Javascript to control audio recording Java applet */

/* FIXME: these variables need to be made specific to a single applet, not global! */
var state = '';
var lastState = '';

/* Functions invoked by button clicks */
function audioRecord(appletId) {
	if(!$('#' + appletId).parent().find('.actions .record').hasClass('off') &&
		!$('#' + appletId).parent().find('.actions .record').hasClass('on')) { 
		document.getElementById(appletId).messageFromJavascript("RECORD");
	}
}

function audioStop(appletId) {
	if(!$('#' + appletId).parent().find('.actions .stop').hasClass('off')) {
		document.getElementById(appletId).messageFromJavascript("STOP");
	}
}

function audioPlay(appletId) {
	if(!$('#' + appletId).parent().find('.actions .play').hasClass('off') &&
			!$('#' + appletId).parent().find('.actions .play').hasClass('on')) {
		document.getElementById(appletId).messageFromJavascript("PLAY");
	}
}

function audioPause(appletId) {
	if(!$('#' + appletId).parent().find('.actions .pause').hasClass('off')) {
		document.getElementById(appletId).messageFromJavascript("PAUSE");
	}
}

/* Callbacks from the applet */
function showLoadingStyle(id) {
	audioStatus(id, "loading", "Loading");
	audioIndicator(id, "loading");
	audioAction(id, "loading");
	state = 'loading';
}

function showReadyStyle(id) {
	audioStatus(id, "ready", "Ready");
	audioIndicator(id, "ready");
	audioAction(id, "ready");
	state = 'ready';
}

function showRecordingStyle(id) {
	audioStatus(id, "recording", "Recording");
	audioIndicator(id, "recording");
	audioAction(id, "record");
	state = 'recording';
}

function showPlaybackStyle(id) {
	audioStatus(id, "playback", "Playing");
	audioIndicator(id, "playback");
	audioAction(id, "play");
	state = 'playing';
}

function showPauseStyle(id) {
	if(state == 'paused') {
		if(lastState == 'playing') {
			showPlaybackStyle(id);
		}
		else {
			showRecordingStyle(id);
		}
	}
	else {
		audioStatus(id, "paused", "Paused");
		audioAction(id, "pause");
		lastState = state;
		state = 'paused';
	}
}

function showProgressStyle(id) {
	audioStatus(id, "progress", "Saving");
	audioIndicator(id, "progress");
	state = 'progress';
}

function error(id) {
	setStatusBar(id, "An Error Occurred");
	audioIndicator(id, 'progress');
	$("#" + id + " .actions .play").addClass("off");
    $("#" + id + " .actions .pause").addClass("off");
    $("#" + id + " .actions .record").addClass("off");
    $("#" + id + " .actions .stop").addClass("off");
}

function setStatusBar(id, msg) {
	$("#" + id + " .status").html("");
    if (msg==null ||msg=='') msg="&nbsp;";
    $("#" + id + " .status").html(msg);
}

/* audioStatus()
    id - player HTML id
    state - status message state - recording, playback, or default
    msg - message text to display
*/
function audioStatus(id, state, msg) {
    // Reset status state
    $("#" + id + " .status").removeClass("status_recording status_playback");
    // Set new state
    switch(state) {
        case 'playback':  { $("#" + id + " .status").addClass("status_playback"); break; }
        case 'recording': { $("#" + id + " .status").addClass("status_recording"); break; }
        default: { }
    }
    // Update msg
    setStatusBar(id, msg);
    return false;
}

/* audioIndicator()
    id - player HTML id
    state - indicator state - loading, recording, playback (ie: busy, volume, or slider)
*/
function audioIndicator(id, audioState) {
    // Reset indicator state
	$("#" + id + " .indicator").removeClass("indicator_busy indicator_volume indicator_slider indicator_progress");
    $("#" + id + " .indicator").attr("style", "");
    $("#" + id + " .indicator .mask").attr("style", "");
    // Set new state
    if(state != 'paused') {
    	$("#" + id + " .indicator .mask").attr("style", "");
    }
    // Set new state
    switch(audioState) {
        case 'loading':  { $("#" + id + " .indicator").addClass("indicator_busy"); break; }
        case 'playback':  { $("#" + id + " .indicator").addClass("indicator_slider"); break; }
        case 'recording': { $("#" + id + " .indicator").addClass("indicator_volume"); break; }
        case 'progress': { $("#" + id + " .indicator").addClass("indicator_progress"); break; }
        default: { }
    }

    return false;
}

function disablePlay(id) {
	$("#" + id + " .actions .play").addClass("off");
}

/* audioAction()
    id - player HTML id
    state - player state - loading, recording, playback
    action - selected action
*/
function audioAction(id, action) {
	$("#" + id + " .actions a").removeClass("on off");
	if($("#" + id).hasClass("readOnly")) {
		$("#" + id + " .actions .record").addClass("off");
	}
    // Set action buttons
    switch(action) {
        case 'record': {
        	$("#" + id + " .actions .record").addClass("on");
        	$("#" + id + " .actions .play").addClass("off");
            break;
        }
        case 'play': {
        	$("#" + id + " .actions .record").addClass("off");
            $("#" + id + " .actions .play").addClass("on");
            break;
        }
        case 'pause': {
            if (state == 'recording') {
            	$("#" + id + " .actions .record").addClass("on");
            	$("#" + id + " .actions .play").addClass("off");
                $("#" + id + " .actions .pause").addClass("on");
            } else {
            	if(!$("#" + id).hasClass("readOnly")) {
            		$("#" + id + " .actions .record").addClass("off");
            	}
            	$("#" + id + " .actions .record").addClass("off");
                $("#" + id + " .actions .play").addClass("on");
                if($("#" + id + " .actions .pause").hasClass("on")) {
                	$("#" + id + " .actions .pause").addClass("off");
                }
                else {
                	$("#" + id + " .actions .pause").addClass("on");
                }
            }
            break;
        }
        case 'loading': {
        	$("#" + id + " .actions a").addClass("off");
        	break;
        }
        case 'ready': {
        	$("#" + id + " .actions .pause").addClass("off");
        	$("#" + id + " .actions .stop").addClass("off");
    	}
    }
    return false;
}

/* audioVolume(id, level)
    id - player HTML id
    level - volume level as representation of %
*/
function audioVolume(id, level) {
    if ( $("#" + id + " .indicator").hasClass("indicator_volume") ) {
        var maxWidth = parseInt($("#" + id + " .indicator_volume .mask").css('max-width'));
        var minWidth = parseInt($("#" + id + " .indicator_volume .mask").css('min-width'));
        var levelWidth = ((maxWidth - minWidth) * (level/20));
        var newWidth = minWidth + levelWidth;
        var newMargin = (newWidth/2) * -1;
        $("#" + id + " .indicator_volume .mask").css('width', newWidth + "px");
        $("#" + id + " .indicator_volume .mask").css('margin-left', newMargin + "px");
    }
    return false;
}

/* audioSlider(id, level)
    id - player HTML id
    time - playback %
*/
function audioSlider(id, time, timeString) {
	if ( $("#" + id + " .indicator").hasClass("indicator_slider") ) {
        var maxWidth = parseInt($("#" + id + " .indicator_slider").width());
        var caretWidth = parseInt($("#" + id + " .indicator_slider .mask").css('width'));
        var newOffset = ((maxWidth - caretWidth) * (time/100));
        $("#" + id + " .indicator_slider .mask").css('left', newOffset + "px");
        if(state != "paused") {
        	setStatusBar(id, "Playing: " + timeString);
        }
    }
    else {
    	if(state != "paused") {
    		setStatusBar(id, "Recording: " + timeString);
    	}
    }
    return false;
}

/* audioProgress(id, complete)
id - player HTML id
complete - progress %
*/
function audioProgress(id, complete) {
	if ( $("#" + id + " .indicator").hasClass("indicator_progress") ) {
		var maxWidth = parseInt($("#" + id + " .indicator_progress").width());
		var newWidth = maxWidth * (complete/100);
		$("#" + id + " .indicator_progress .mask").css('width', newWidth + "px");
	}
	return false;
}
