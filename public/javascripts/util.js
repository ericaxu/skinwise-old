function isInteger(str) {
    return /^\+?(0|[1-9]\d*)$/.test(str);
}

function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function postToAPI(url, params, successCallback, errorCallback, message) {
    if (message) {
        var $message_box = $(SW.TEMPALTES.NOTICE({
            type: 'info',
            message: message
        }));
        $('.notice_container').append($message_box);
        $message_box.fadeIn(SW.CONFIG.NOTICE_FADE_IN);
    }
    $.ajax(SW.CONFIG.API_ROOT + url, {
        contentType: 'text/plain',
        type: 'POST',
        data: JSON.stringify(params),
        dataType: 'json',
        success: function(response, status, jqxhr) {

            if (message) {
                $message_box.fadeOut(SW.CONFIG.NOTICE_FADE_OUT);
            }

            _.forEach(response.messages, showMessage);

            if (response.code == "Ok") {
                successCallback && successCallback(response);
            } else if (errorCallback) {
                console.log("Error communicating with API: " + response.code);
                errorCallback(response);
            }
        },

        error: function(jqxhr, status, err) {
            console.error("Network error occurred when trying to post to " + url + ": " + status);

            if (message) {
                $message_box.fadeOut(SW.CONFIG.NOTICE_FADE_OUT);
            }

            errorCallback && errorCallback(response);
        }
    });
}

function showMessage(message) {
    var $notice_box = $(SW.TEMPALTES.NOTICE({
        type: message.type,
        message: message.message
    }));
    $('.notice_container').append($notice_box);
    $notice_box.fadeIn(SW.CONFIG.NOTICE_FADE_IN);
    setTimeout(function() {
        $notice_box.fadeOut(200);
    }, message.timeout || SW.CONFIG.DEFAULT_NOTICE_TIMEOUT);
}

function showError(message) {
    showMessage({ type: 'error', message: message });
}

function showInfo(message) {
    showMessage({ type: 'info', message: message });
}