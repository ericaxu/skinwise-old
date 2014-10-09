function postToAPI(url, params, successCallback, errorCallback) {
    console.log(url, params);
    $.ajax(url, {
        contentType: "text/plain",
        type: "POST",
        data: JSON.stringify(params),
        dataType: "json",
        success: function(response, status, jqxhr) {
            console.log(response);
            if (response.status.code == "Ok") {
                successCallback && successCallback(response);
            } else if (errorCallback) {
                console.log("Error communicating with API: " + response.status.code);
                errorCallback(response.status);
            }
        },

        error: function(jqxhr, status, err) {
            console.error("Network error occurred when trying to post to " + url + ": " + status);
            errorCallback && errorCallback(response);
        }
    });
}