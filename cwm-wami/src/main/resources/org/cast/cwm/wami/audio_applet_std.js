/* audioStatus()
    id - player HTML id
    state - status message state - recording, playback, or default
    msg - message text to display
*/
function audioStatus(id, state, msg) {
    // Reset status state
    $("#" + id + " .status").removeClass("status_recording status_playback status_progress");
    $("#" + id + " .status").html("");
    // Set new state
    switch(state) {
        case 'playback':  { $("#" + id + " .status").addClass("status_playback"); break; }
        case 'recording': { $("#" + id + " .status").addClass("status_recording"); break; }
        case 'progress': { $("#" + id + " .status").addClass("status_progress"); break; }
        default: { }
    }
    // Update msg
    if (msg==null || msg=='') msg="&nbsp;";
    $("#" + id + " .status").html(msg);

    return false;
}

/* audioTimestamp()
    id - player HTML id
    ts0 - current timestamp
    ts1 - end timestamp
*/
function audioTimestamp(id, ts0, ts1) {
    // Reset status state
    $("#" + id + " .indicator .ts0").html("");
    $("#" + id + " .indicator .ts1").html("");
    if (ts0==null) ts0="";
    if (ts1==null) ts1="";
    $("#" + id + " .indicator .ts0").html(ts0);
    $("#" + id + " .indicator .ts1").html(ts1);

    return false;
}

/* audioIndicator()
    id - player HTML id
    state - indicator state - loading, recording, playback (ie: busy, volume, or slider)
*/
function audioIndicator(id, state) {
    // Reset indicator state
    $("#" + id + " .indicator").removeClass("indicator_busy indicator_volume indicator_slider indicator_progress");
    //$("#" + id + " .indicator").attr("style", "");
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
function audioAction(id, state, action) {
    // Reset actions
    $("#" + id + " .actions").addClass("on");
    $("#" + id + " .actions a").removeClass("on off active");
    // Set action buttons
    switch(action) {
        case 'record': {
            $("#" + id + " .actions .record").addClass("on");
            $("#" + id + " .actions .play").addClass("off");
            $("#" + id + " .actions .pause").addClass("off");
            break;
        }
        case 'play': {
            $("#" + id + " .actions .record").addClass("off");
            $("#" + id + " .actions .play").addClass("on");
            break;
        }
        case 'pause': {
            if (state == 'recording') {
                $("#" + id + " .actions .play").addClass("off");
                $("#" + id + " .actions .pause").addClass("on");
            } else {
                $("#" + id + " .actions .record").addClass("off");
                $("#" + id + " .actions .pause").addClass("on");
            }
            break;
        }
        case 'stop': { }
        default: {
            if (state == 'loading') {
                $("#" + id + " .actions .record").addClass("off");
                $("#" + id + " .actions .play").addClass("off");
                $("#" + id + " .actions .pause").addClass("off");
                $("#" + id + " .actions .stop").addClass("off");
            } else {
                $("#" + id + " .actions .pause").addClass("off");
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

/* audioSlider(id, level)
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

function audioModal(id, show) {
    if (show) {
        $("#" + id + " .audio_modal").show();
    } else {
        $("#" + id + " .audio_modal").hide();
    }
}