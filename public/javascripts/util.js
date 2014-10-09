function postToAPI(url, params, successCallback, errorCallback) {
    console.log(url, params);
    $.ajax(url, {
        contentType: 'text/plain',
        type: 'POST',
        data: JSON.stringify(params),
        dataType: 'json',
        success: function(response, status, jqxhr) {
            if (response.info) {
                $('.notice_container .info .message').text(response.info);
                $('.notice_container .info').fadeIn(200);
            }

            if (response.error) {
                $('.notice_container .error .message').text(response.error);
                $('.notice_container .error').fadeIn(200);
            }

            setTimeout(function() {
                $('.info, .error').fadeOut(200);
            }, 5000);

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