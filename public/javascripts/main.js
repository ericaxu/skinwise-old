function setupPopups() {
    // Closing popup
    $(".wrapper, .close_btn").on("click", function(e) {
        $(".popup").hide();
    });

    // Allow user to close popup with ESC key
    $(document).keydown(function(e){
        if(e.keyCode == 27){
            $(".popup").hide();
        }
    });

    $(".form_container").click(function(e) {
        e.stopPropagation();
    });

    setupLoginPopup();
    setupSignupPopup();
}

function setupLoginPopup() {
    $("#login_link").on("click", function(e) {
        $(".login.popup").show();
        e.preventDefault();
    });
}

function setupSignupPopup() {
    $("#signup_link").on("click", function(e) {
        $(".signup.popup").show();
        e.preventDefault();
    });
}

$(document).ready(function() {
    setupPopups();
});