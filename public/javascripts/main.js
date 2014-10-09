var SW_TEST_ING_LIST = {
    'zinc oxide': {
        functions: [ 'Sunscreen', 'Anti-inflammatory' ],
        short_desc: 'Zinc oxide is a mineral that is primarily used as a sunscreen ingredient, providing physical protection ' +
            'against both UVA and UVB rays. It also has antibacterial and anti-inflammatory properties, often making an appearance ' +
            'in acne treatments, calamine lotion and diaper rash creams.'
    }
};

var ING_INFOBOX_TIMEOUT_EVENT = null;
var ING_INFOBOX_TIMEOUT = 500;
var SEARCHBAR_EXPAND_TIMEOUT_EVENT = null;

function cleanupErrors() {
    $('.error_msg').hide();
}

function setupPopups() {
    // Closing popup
    $('.wrapper, .close_btn').on('click', function(e) {
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
        postToAPI('/api/user/login', {
            email: $('#login_email').val(),
            password: $('#login_password').val()
        }, loginSuccess, loginError);
    });
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
}

function setupExpandableSearchbar() {
    $('#search_icon').on('click', function() {
        $(this).hide();
        $('#nav_searchbar').addClass('activated');
    }).on('mouseenter', function(e) {
        SEARCHBAR_EXPAND_TIMEOUT_EVENT = setTimeout($.proxy(function() {
            $(this).hide();
            $('#nav_searchbar').addClass('activated');
        }, this), 400);
    }).on('mouseleave', function() {
        clearTimeout(SEARCHBAR_EXPAND_TIMEOUT_EVENT);
    });
}

function setupIngredientInfobox() {
    var $ingredient = $('.ingredient');

    $ingredient.on('click', function(e) {
        var ingredient_name = $(this).text()
        $('.ingredient_infobox').remove();
        if (SW_TEST_ING_LIST[ingredient_name.toLowerCase()]) {

            var ingredient_data = SW_TEST_ING_LIST[ingredient_name.toLowerCase()];
            var ingredient_info = $('<div/>', { class: 'ingredient_infobox' }).on('click', function(e) {
                e.stopPropagation();
            });
            var close_button = $('<span/>', { class: 'close_btn' }).on('click', function() {
                $('.ingredient_infobox').remove();
            })
            ingredient_info.append(close_button);
            ingredient_info.append($('<h2/>', { text: ingredient_name }));
            var functions = $('<p/>', { class: 'functions' });
            for (var i = 0; i < ingredient_data.functions.length; i++) {
                functions.append($('<span/>', {
                    class: 'function neutral',
                    text: ingredient_data.functions[i]
                }));
            }
            ingredient_info.append(functions);
            ingredient_info.appendTo('body');
            ingredient_info.append($('<p/>', { text: ingredient_data.short_desc }));
            ingredient_info.append('<p><a class="explicit" href="#">More details</a></p>');
            ingredient_info.show().offset({ top: e.pageY, left: e.pageX });
        }
    });

    $(document).on('click', function() {
        $('.ingredient_infobox').remove();
    });

    $ingredient.on('click', function(e) {
        e.stopPropagation();
    });
}

$(document).ready(function() {
    setupPopups();

    setupIngredientInfobox();

    setupExpandableSearchbar();

    $("#import_ingredient_btn").on('click', function() {
        postToAPI('/api/admin/import', {});
    });
});