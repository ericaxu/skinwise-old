function cleanupErrors() {
    $('.error_msg').hide();
}

function setupPopups() {
    // Closing popup
    $('.wrapper, .close_btn, #cancel_btn').on('click', function (e) {
        $('.popup').hide();
    });

    // Allow user to close popup with ESC key
    $(document).keydown(function (e) {
        if (e.keyCode == 27) {
            $('.popup, .ingredient_infobox').hide();
        }
    });

    $('.form_container').click(function (e) {
        e.stopPropagation();
    });

    setupLoginPopup();
    setupSignupPopup();
    setupFeedbackPopup();
    setupForgotPasswordPopup();
}

function setupForgotPasswordPopup() {
    $('#forget_password_link').on('click', function (e) {
        e.preventDefault();
        $('.popup').hide();
        $('.forgot_password.popup').show();
        $('#forgot_password_email').focus();
    })
}

function setupLoginPopup() {
    $('#login_link').on('click', function (e) {
        $('.popup').hide();
        $('.login.popup').show();
        $('#login_email').focus();
        e.preventDefault();
    });

    setupLoginCall();
}

function setupLoginCall() {
    $('#login_btn').on('click', function (e) {
        cleanupErrors();
        $(this).val('Signing in...');
        postToAPI('/user/login', {
            email: $('#login_email').val(),
            password: $('#login_password').val()
        }, loginSuccess, loginError);
    });
}

function setupSignupCall() {
    $('#signup_btn').on('click', function (e) {
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
    $('#logout_link').on('click', function (e) {
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

function showFeedbackErrorMsg(message) {
    $('#feedback_error').text(message).show();
}

function showAddFilterError(message) {
    $('#add_filter_error').text(message).show();
}

function loginError(status) {
    $('#login_btn').val('Sign in');
    if (status.code == 'Unauthorized') {
        showLoginErrorMsg('Email or password is incorrect.');
    }
}

function setupSignupPopup() {
    $('#signup_link').on('click', function (e) {
        $('.popup').hide();
        $('.signup.popup').show();
        $('#signup_name').focus();
        e.preventDefault();
    });

    setupSignupCall();
}

function setupExpandableSearchbar() {
    $('#search_icon').on('click', function () {
        $(this).hide();
        $('#search_category_select, #search_category_arrow, #nav_searchbar, #nav_searchbar_btn').show();
    });
}

function confirmAction(action, callback, key) {
    // Do we want to ask again?
    if (key === undefined || getAskAgain(key) === null || JSON.parse(getAskAgain(key))) {
        $('.popup').hide();
        $('.confirm.popup .action').text(action);
        $('#confirm_btn').off('click').on('click', function () {
            $('.popup').hide();
            callback();
        });
        $('#dont_ask_again_btn').off('click').on('click', function() {
            setAskAgain(key, false);
            $('.popup').hide();
            callback();
        });

        // Do we provide the option "Don't ask again"?
        if (key === undefined) {
            $('#dont_ask_again_btn').hide();
            $('.confirm.popup .form_container').css('width', '400px');
        } else {
            $('#dont_ask_again_btn').show();
            $('.confirm.popup .form_container').css('width', '600px');
        }
        $('.confirm.popup').show();
    } else {
        callback();
    }
}

function setupFeedbackCall() {
    $('#feedback_btn').on('click', function (e) {
        cleanupErrors();

        if ($('#feedback_email').val() === '') {
            showFeedbackErrorMsg('We need your email address to get back to you :)');
            return;
        }

        if ($('#feedback_type').val() === 'none') {
            showFeedbackErrorMsg('Please select a type!');
            return;
        }

        if ($('#feedback_message').val() === '') {
            showFeedbackErrorMsg('Please write something?');
            return;
        }

        setFeedbackEmail($('#feedback_email').val());
        $(this).val('Sending...');
        postToAPI('/feedback/report', {
            email: $('#feedback_email').val(),
            path: location.href,
            type: $('#feedback_type').val(),
            content: $('#feedback_message').val()
        }, feedbackSuccess);
    });
}

function feedbackSuccess(response) {
    $('.feedback.popup').hide();
    // Reset feedback popup
    $('#feedback_type').val('none');
    $('#feedback_message').val('');
    $('#feedback_btn').val('Send feedback');

}

function setupFeedbackPopup() {
    $('.feedback_link').on('click', function (e) {
        $('#feedback_email').val(getFeedbackEmail());
        $('.feedback.popup').show();
        $('#feedback_email').focus();
        e.preventDefault();
    });

    setupFeedbackCall();
}

$(document).ready(function () {
    setupPopups();
    setupLogoutCall();
    setupExpandableSearchbar();

    $('.notice_container').on('click', '.close_btn', function () {
        $(this).parent().fadeOut(200);
    });
});