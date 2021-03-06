/* audioStatus()
    id - player HTML id
    state - status message state - recording, playback, or default
    msg1 - message text to display (used in playback mode to show current time position during playback, pairs with audioSlider() to indicate position
    msg2 - used in playback mode to indicate track length
*/
function audioStatus(id, state, msg0, msg1) {
    // Reset status state
    $("#" + id + " .status").removeClass("status_recording status_playback");
    $("#" + id + " .status .msg0").html("");
    $("#" + id + " .status .msg1").html("");
    // Set new state
    switch(state) {
        case 'playback':  { $("#" + id + " .status").addClass("status_playback"); break; }
        case 'recording': { $("#" + id + " .status").addClass("status_recording"); break; }
        default: { }
    }
    // Update msg
    if (msg0==null || msg0=='') msg0="&nbsp;";
    if (msg1==null || msg1=='') msg1="&nbsp;";
    $("#" + id + " .status .msg0").html(msg0);
    $("#" + id + " .status .msg1").html(msg1);

    return false;
}

/* audioIndicator()
    id - player HTML id
    state - indicator state - loading, recording, playback (ie: busy, volume, or slider)
*/
function audioIndicator(id, state) {
    // Reset indicator state
    $("#" + id + " .indicator").removeClass("indicator_busy indicator_volume indicator_slider indicator_progress");
    $("#" + id + " .indicator").attr("style", "");
    $("#" + id + " .indicator .mask").attr("style", "");
    // Set new state
    switch(state) {
        case 'loading':  { $("#" + id + " .indicator").addClass("indicator_busy"); break; }
        case 'playback':  { $("#" + id + " .indicator").addClass("indicator_slider"); break; }
        case 'recording': { $("#" + id + " .indicator").addClass("indicator_volume"); break; }
        case 'progress': { $("#" + id + " .indicator").addClass("indicator_progress"); break; }
        default: { }
    }

    return false;
}

/* audioAction()
    id - player HTML id
    state - player state - loading, recording, recording_nt (no exising track), playback
    action - selected action
*/
/* Button css states:
    hide - button hidden/not visible
    off - visible and in 'gray/disabled' state
    on  - visible and in 'available' state
    active - visible and in 'active' state
    recording nt - used as a pair for recording with the no existing track option

*/
function audioAction(id, state, action) {
    // Reset actions
    $("#" + id + " .actions").addClass("on");
    $("#" + id + " .actions a").removeClass("on off active hide nt recording");
    // Set action buttons
    switch(action) {
        case 'record': {
            $("#" + id + " .actions .record").addClass("hide");
            $("#" + id + " .actions .play").addClass("hide");
            $("#" + id + " .actions .pause").addClass("hide");
            $("#" + id + " .actions .stop").addClass("recording nt");
            /*
            $("#" + id + " .actions .record").addClass("active");
            $("#" + id + " .actions .play").addClass("off");
            $("#" + id + " .actions .pause").addClass("hide");
            $("#" + id + " .actions .stop").addClass("on");
            */
            break;
        }
        case 'play': {
            $("#" + id + " .actions .record").addClass("off");
            $("#" + id + " .actions .play").addClass("hide");
            $("#" + id + " .actions .pause").addClass("on");
            $("#" + id + " .actions .stop").addClass("on");

            break;
        }
        case 'pause': {
            if (state == 'recording') {
                $("#" + id + " .actions .record").addClass("off");
                $("#" + id + " .actions .play").addClass("hide");
                $("#" + id + " .actions .pause").addClass("on");
                $("#" + id + " .actions .stop").addClass("on");
            } else {
                $("#" + id + " .actions .record").addClass("off");
                $("#" + id + " .actions .play").addClass("on");
                $("#" + id + " .actions .pause").addClass("hide");
                $("#" + id + " .actions .stop").addClass("on");
            }
            break;
        }
        case 'stop': { }
        default: {
            if (state == 'loading') {
                $("#" + id + " .actions .record").addClass("hide");
                $("#" + id + " .actions .play").addClass("hide");
                $("#" + id + " .actions .pause").addClass("hide");
                $("#" + id + " .actions .stop").addClass("hide");
            } else if (state == 'recording_nt') {
               $("#" + id + " .actions .record").addClass("nt");
                $("#" + id + " .actions .play").addClass("hide");
                $("#" + id + " .actions .pause").addClass("hide");
                $("#" + id + " .actions .stop").addClass("hide");
            } else if (state == 'recording') {
                $("#" + id + " .actions .record").addClass("nt");
                $("#" + id + " .actions .play").addClass("hide");
                $("#" + id + " .actions .pause").addClass("hide");
                $("#" + id + " .actions .stop").addClass("hide");
            } else {
                $("#" + id + " .actions .record").addClass("on");
                $("#" + id + " .actions .play").addClass("on");
                $("#" + id + " .actions .pause").addClass("hide");
                $("#" + id + " .actions .stop").addClass("off");
            }
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
        var levelWidth = ((maxWidth - minWidth) * (level/100));
        var newWidth = minWidth + levelWidth;
        var newMargin = (newWidth/2) * -1;
        $("#" + id + " .indicator_volume .mask").css('width', newWidth + "px");
        $("#" + id + " .indicator_volume .mask").css('margin-left', newMargin + "px");
    }
    return false;
}

function audioVolumeResponsive(id, level) {
    if ( $("#" + id + " .indicator").hasClass("indicator_volume") ) {
        var maxWidth = parseInt($("#" + id + " .indicator_volume .mask").css('max-width'));
        var minWidth = parseInt($("#" + id + " .indicator_volume .mask").css('min-width'));
        var percent = 0;
        if ( level < 2 ) {
            percent = 0;
        } else if ( level < 5 ) {
            percent = .25;
        } else if ( level < 10 ) {
            percent = .50;
        } else if ( level < 20 ) {
            percent = .75;
        } else {
            percent = 1;
        }
        var levelWidth = ((maxWidth - minWidth) * (percent) );
        var newWidth = minWidth + levelWidth;
        var newMargin = (newWidth/2) * -1;
        $("#" + id + " .indicator_volume .mask").css('width', newWidth + "px");
        $("#" + id + " .indicator_volume .mask").css('margin-left', newMargin + "px");
    }
    return false;
}

/* audioSlider(id, time)
    id - player HTML id
    time - playback %
*/
function audioSlider(id, time) {
    if ( $("#" + id + " .indicator").hasClass("indicator_slider") ) {
        var maxWidth = parseInt($("#" + id + " .indicator_slider").width());
        var caretWidth = parseInt($("#" + id + " .indicator_slider .mask").css('width'));
        var newOffset = ((maxWidth - caretWidth) * (time/100));
        $("#" + id + " .indicator_slider .mask").css('left', newOffset + "px");
    }
    return false;
}

/* audioSlider(id, complete)
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

/* audioSlider(id, show)
    id - player HTML id
    show - boolean value (true=show, false=hide)
*/
function audioModal(id, show) {
    if (show) {
        $("#" + id + " .audio_modal").show();
    } else {
        $("#" + id + " .audio_modal").hide();
    }
}