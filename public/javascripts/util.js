function postToAPI(url, params, successCallback, errorCallback) {
    console.log(url, params);
    $.ajax(url, {
        contentType: 'text/plain',
        type: 'POST',
        data: JSON.stringify(params),
        dataType: 'json',
        success: function(response, status, jqxhr) {
            console.log(response);

            for (var i = 0; i < response.messages.length; i++) {
                var message = response.messages[i];
                var $notice_box = $(getNoticeHTML(message.type, message.message));
                $('.notice_container').append($notice_box);
                $notice_box.fadeIn(200);
                setTimeout(function() {
                    $notice_box.fadeOut(200);
                }, message.timeout);
            }

            if (response.code == "Ok") {
                successCallback && successCallback(response);
            } else if (errorCallback) {
                console.log("Error communicating with API: " + response.code);
                errorCallback(response);
            }
        },

        error: function(jqxhr, status, err) {
            console.error("Network error occurred when trying to post to " + url + ": " + status);
            errorCallback && errorCallback(response);
        }
    });
}

function getNoticeHTML(type, message) {
    return '<div class="' + type + '"><span class="close_btn"></span><p class="message">' + message + '</p></div>';
}