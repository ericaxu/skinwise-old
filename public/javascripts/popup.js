function cleanupErrors() {
    $('.error_msg').hide();
}

function setupPopups() {
    // Closing popup
    $('.wrapper, .close_btn, #cancel_btn').on('click', function(e) {
        $('.popup').hide();
    });

    // Allow user to close popup with ESC key
    $(document).keydown(function(e){
        if(e.keyCode == 27){
            $('.popup').hide();
        }
    });

    $('.form_container').click(function(e) {
        e.stopPropagation();
    });

    setupLoginPopup();
    setupSignupPopup();
    setupForgotPasswordPopup();
}

function setupForgotPasswordPopup() {
    $('#forget_password_link').on('click', function(e) {
        e.preventDefault();
        $('.popup').hide();
        $('.forgot_password.popup').show();
    })
}

function setupLoginPopup() {
    $('#login_link').on('click', function(e) {
        $('.popup').hide();
        $('.login.popup').show();
        e.preventDefault();
    });

    setupLoginCall();
}

function setupLoginCall() {
    $('#login_btn').on('click', function(e) {
        cleanupErrors();
        $(this).val('Signing in...');
        postToAPI('/user/login', {
            email: $('#login_email').val(),
            password: $('#login_password').val()
        }, loginSuccess, loginError);
    });
}

function setupSignupCall() {
    $('#signup_btn').on('click', function(e) {
        cleanupErrors();
        $(this).val('Signing you up...');
        postToAPI('/user/signup', {
            name: $('#signup_name').val(),
            email: $('#signup_email').val(),
            password: $('#signup_password').val()
        }, signupSuccess);
    });
}

function setupLogoutCall() {
    $('#logout_link').on('click', function(e) {
        e.preventDefault();
        postToAPI('/user/logout', {}, logoutSuccesss);
    });
}

function logoutSuccesss() {
    location.reload();
}

function signupSuccess() {
    location.reload();
}

function signupError() {
    $('#signup_btn').val('Sign up');
}

function loginSuccess() {
    location.reload();
}

function showLoginErrorMsg(message) {
    $('#login_error').text(message).show();
}

function loginError(status) {
    $('#login_btn').val('Sign in');
    if (status.code == 'Unauthorized') {
        showLoginErrorMsg('Email or password is incorrect.');
    }
}

function setupSignupPopup() {
    $('#signup_link').on('click', function(e) {
        $('.popup').hide();
        $('.signup.popup').show();
        e.preventDefault();
    });

    setupSignupCall();
}

function setupExpandableSearchbar() {
    $('#search_icon').on('click', function() {
        $(this).hide();
        $('#nav_searchbar').addClass('activated');
    }).on('mouseenter', function(e) {
        SW.SEARCHBAR_EXPAND_TIMEOUT = setTimeout($.proxy(function() {
            $(this).hide();
            $('#nav_searchbar').addClass('activated');
        }, this), SW.CONFIG.SEARCHBAR_EXPAND_TIMEOUT);
    }).on('mouseleave', function() {
        clearTimeout(SW.SEARCHBAR_EXPAND_TIMEOUT);
    });
}

function confirmAction(action, callback) {
    $('.popup').hide();
    $('.confirm.popup .action').text(action);
    $('#confirm_btn').off('click').on('click', function() {
        $('.popup').hide();
        callback();
    });
    $('.confirm.popup').show();
}


$(document).ready(function() {
    setupPopups();
    setupLogoutCall();
    setupExpandableSearchbar();

    $('.notice_container').on('click', '.close_btn', function() {
        console.log($(this).parent())
        $(this).parent().fadeOut(200);
    });
});