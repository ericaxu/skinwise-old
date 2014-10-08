function postToAPI(url, params, successCallback, errorCallback) {
    console.log(url, params);
    $.ajax(url, {
        contentType: "text/plain",
        type: "POST",
        data: JSON.stringify(params),
        success: function(response, status, jqxhr) {
            console.log(response);
            successCallback && successCallback(response);
        },

        error: function(jqxhr, status, err) {
            console.error("Network error occurred when trying to post to " + url + ": " + status);
            errorCallback && errorCallback(response);
        }
    });
}