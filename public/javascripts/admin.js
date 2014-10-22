function setupTabSystem() {
    var current_tab = $('.current').data('tabName');
    $('.tab').hide();
    $('#' + current_tab).show();
    $('.tab_title').on('click', function (e) {
        e.preventDefault();
        $('.tab').hide();
        $('.tab_title').removeClass('current');
        $(this).addClass('current');
        $('#' + $(this).data('tabName')).show();
    })
}

function hideEdit() {
    $('.edit_container').hide();
}


function setupDatabaseManager() {
    $('#import_btn').on('click', function () {
        postToAPI('/admin/import', {}, null, null, 'Importing data to database...');
    });
    $('#export_btn').on('click', function () {
        postToAPI('/admin/export', {}, null, null, 'Exporting database...');
    });
}

function listenForEnter() {
    $("input").focus(function () {
        $(this).addClass('focused');
    }).blur(function () {
        $(this).removeClass('focused');
    });

    $(document.body).on('keyup', function (e) {
        // 13 is ENTER
        if (e.which === 13 && $('.focused').length > 0) {
            var btn_id = $('.focused').attr('id') + '_btn';
            $('#' + btn_id).click();
        }
    });
}

// USERS

function setupUserSearchCall() {
    $('#user_by_id_btn').on('click', function () {
        var user_id = $('#user_by_id').val();
        if (!isInteger(user_id)) {
            showError('User ID must be an integer.');
            return;
        }

        postToAPI('/admin/user/byid', {
            id: user_id
        }, userLoadSuccess, null, 'Looking up user...');
    });

    $('#user_by_email_btn').on('click', function () {
        var user_email = $('#user_by_email').val();

        postToAPI('/admin/user/byemail', {
            email: user_email
        }, userLoadSuccess, null, 'Looking up user...');
    });
}

function userLoadSuccess(response) {
    $('#edit_user_id').val(response.id);
    $('#edit_user_name').val(response.name);
    $('#edit_user_email').val(response.email).data('original', response.email);
    $('#edit_user_group').val(response.group);
    $('#edit_user_permissions').val(response.permissions.join(SW.CONFIG.PERMISSION_DELIMITER));
    $('#edit_user').show();
}

function setupUserDeleteCall() {
    $('#delete_user_btn').on('click', function () {
        var user_email = $('#edit_user_email').data('original');
        confirmAction('delete user ' + user_email, function () {
            postToAPI('/admin/user/delete', {
                id: $('#edit_user_id').val()
            }, hideEdit, null, 'Deleting user ' + user_email);
        });
    });
}

function setupUserEditSaveCall() {
    $('#save_user_btn').on('click', function () {
        var new_user_info = {
            id: $('#edit_user_id').val(),
            name: $('#edit_user_name').val(),
            email: $('#edit_user_email').val(),
            group: $('#edit_user_group').val(),
            permissions: $('#edit_user_permissions').val().split(SW.CONFIG.PERMISSION_DELIMITER)
        }

        postToAPI('/admin/user/update', new_user_info, userLoadSuccess, null, 'Updating user...');
    })
}


// GROUPS

function setupGroupSearchCall() {
    $('#group_by_id_btn').on('click', function () {
        var group_id = $('#group_by_id').val();
        if (!isInteger(group_id)) {
            showError('Group ID must be an integer.');
            return;
        }

        postToAPI('/admin/group/byid', {
            id: group_id
        }, groupLoadSuccess, null, 'Looking up group...');
    });

    $('#group_by_name_btn').on('click', function () {
        var group_name = $('#group_by_name').val();

        postToAPI('/admin/group/byname', {
            name: group_name
        }, groupLoadSuccess, null, 'Looking up group...');
    });
}

function groupLoadSuccess(response) {
    $('#edit_group_id').val(response.id);
    $('#edit_group_name').val(response.name).data('original', response.name);
    $('#edit_group_permissions').val(response.permissions.join(SW.CONFIG.PERMISSION_DELIMITER));
    $('#edit_group').show();
}

function setupGroupDeleteCall() {
    $('#delete_group_btn').on('click', function () {
        var group_name = $('#edit_group_name').data('original');
        confirmAction('delete group ' + group_name, function () {
            postToAPI('/admin/group/delete', {
                id: $('#edit_group_id').val()
            }, hideEdit, null, 'Deleting group ' + group_name);
        });
    });
}

function setupGroupEditSaveCall() {
    $('#save_group_btn').on('click', function () {
        var group_id = $('#edit_group_id').val();
        if (group_id === 'Not assigned yet') {
            group_id = '-1';
        }
        var new_group_info = {
            id: group_id,
            name: $('#edit_group_name').val(),
            permissions: $('#edit_group_permissions').val().split(SW.CONFIG.PERMISSION_DELIMITER)
        }

        postToAPI('/admin/group/update', new_group_info, groupLoadSuccess, null, 'Updating group...');
    });
}

function setupCreateGroupCall() {
    $('#create_group_btn').on('click', function () {
        groupLoadSuccess({
            id: 'Not assigned yet',
            name: '',
            permissions: []
        });
    });
}


// INGREDIENTS

function setupIngredientSearchCall() {
    $('#ingredient_by_id_btn').on('click', function () {
        var ingredient_id = $('#ingredient_by_id').val();
        if (!isInteger(ingredient_id)) {
            showError('Ingredient ID must be an integer.');
            return;
        }

        postToAPI('/ingredient/byid', {
            id: ingredient_id
        }, ingredientLoadSuccess, null, 'Looking up ingredient...');
    });
}

function ingredientLoadSuccess(response) {
    $('#edit_ingredient_id').val(response.id);
    $('#edit_ingredient_name').val(response.name).data('original', response.name);
    $('#edit_ingredient_cas_number').val(response.cas_number);
    $('#edit_ingredient_popularity').val(response.popularity);
    $('#edit_ingredient_description').val(response.description);
    $('#edit_ingredient_functions').val(response.functions.join(SW.CONFIG.PERMISSION_DELIMITER));
    $('#edit_ingredient').show();
}

function setupIngredientEditSaveCall() {
    $('#save_ingredient_btn').on('click', function () {
        var ingredient_id = $('#edit_ingredient_id').val();
        if (ingredient_id === 'Not assigned yet') {
            ingredient_id = '-1';
        }
        var new_ingredient_info = {
            id: ingredient_id,
            name: $('#edit_ingredient_name').val(),
            cas_number: $('#edit_ingredient_cas_number').val(),
            popularity: $('#edit_ingredient_popularity').val(),
            description: $('#edit_ingredient_description').val(),
            functions: $('#edit_ingredient_functions').val().split(SW.CONFIG.PERMISSION_DELIMITER)
        };

        postToAPI('/admin/ingredient/update', new_ingredient_info, null, null, 'Updating ingredient...');
    });
}

function setupCreateIngredientCall() {
    $('#create_ingredient_btn').on('click', function () {
        ingredientLoadSuccess({
            id: 'Not assigned yet',
            name: '',
            cas_name: '',
            description: '',
            functions: []
        });
    });
}


// PRODUCT

function setupProductSearchCall() {
    $('#product_by_id_btn').on('click', function () {
        var product_id = $('#product_by_id').val();
        if (!isInteger(product_id)) {
            showError('Product ID must be an integer.');
            return;
        }

        postToAPI('/product/byid', {
            id: product_id
        }, productLoadSuccess, null, 'Looking up product...');
    });
}

function setupCreateProductCall() {
    $('#create_product_btn').on('click', function () {
        productLoadSuccess({
            id: 'Not assigned yet',
            name: '',
            brand: '',
            line: '',
            description: ''
        });
    });
}

function productLoadSuccess(response) {
    log(response);
    $('#edit_product_id').val(response.id);
    $('#edit_product_name').val(response.name).data('original', response.name);
    $('#edit_product_brand').val(response.brand);
    $('#edit_product_line').val(response.line);
    $('#edit_product_image').val(response.image);
    $('#edit_product_popularity').val(response.popularity);
    $('#edit_product_description').val(response.description);
    $('#edit_product').show();
}

function setupProductEditSaveCall() {
    $('#save_product_btn').on('click', function () {
        var product_id = $('#edit_product_id').val();
        if (product_id === 'Not assigned yet') {
            product_id = '-1';
        }
        var new_product_info = {
            id: product_id,
            name: $('#edit_product_name').val(),
            brand: $('#edit_product_brand').val(),
            line: $('#edit_product_line').val(),
            image: $('#edit_product_image').val(),
            popularity: $('#edit_product_popularity').val(),
            description: $('#edit_product_description').val()
        };

        postToAPI('/admin/product/update', new_product_info, null, null, 'Updating product...');
    });
}


// FUNCTION

function setupFunctionSearchCall() {
    $('#function_by_id_btn').on('click', function () {
        var function_id = $('#function_by_id').val();
        if (!isInteger(function_id)) {
            showError('Function ID must be an integer.');
            return;
        }

        postToAPI('/ingredient/function/byid', {
            id: function_id
        }, functionLoadSuccess, null, 'Looking up function...');
    });
}

function setupCreateFunctionCall() {
    $('#create_function_btn').on('click', function () {
        functionLoadSuccess({
            id: 'Not assigned yet',
            name: '',
            brand: '',
            line: '',
            description: ''
        });
    });
}

function functionLoadSuccess(response) {
    $('#edit_function_id').val(response.id);
    $('#edit_function_name').val(response.name).data('original', response.name);
    $('#edit_function_description').val(response.description);
    $('#edit_function').show();
}

function setupFunctionEditSaveCall() {
    $('#save_function_btn').on('click', function () {
        var function_id = $('#edit_function_id').val();
        if (function_id === 'Not assigned yet') {
            function_id = '-1';
        }
        var new_function_info = {
            id: function_id,
            name: $('#edit_function_name').val(),
            description: $('#edit_function_description').val()
        };

        postToAPI('/admin/function/update', new_function_info, null, null, 'Updating function...');
    });
}


// BRAND

function setupBrandSearchCall() {
    $('#brand_by_id_btn').on('click', function () {
        var brand_id = $('#brand_by_id').val();
        if (!isInteger(brand_id)) {
            showError('Brand ID must be an integer.');
            return;
        }

        postToAPI('/product/brand/byid', {
            id: brand_id
        }, brandLoadSuccess, null, 'Looking up brand...');
    });
}

function setupCreateBrandCall() {
    $('#create_brand_btn').on('click', function () {
        brandLoadSuccess({
            id: 'Not assigned yet',
            name: '',
            brand: '',
            line: '',
            description: ''
        });
    });
}

function brandLoadSuccess(response) {
    log(response);
    $('#edit_brand_id').val(response.id);
    $('#edit_brand_name').val(response.name).data('original', response.name);
    $('#edit_brand_brand').val(response.brand);
    $('#edit_brand_line').val(response.line);
    $('#edit_brand_image').val(response.image);
    $('#edit_brand_description').val(response.description);
    $('#edit_brand').show();
}

function setupBrandEditSaveCall() {
    $('#save_brand_btn').on('click', function () {
        var brand_id = $('#edit_brand_id').val();
        if (brand_id === 'Not assigned yet') {
            brand_id = '-1';
        }
        var new_brand_info = {
            id: brand_id,
            name: $('#edit_brand_name').val(),
            brand: $('#edit_brand_brand').val(),
            line: $('#edit_brand_line').val(),
            description: $('#edit_brand_description').val()
        };

        postToAPI('/admin/brand/update', new_brand_info, null, null, 'Updating brand...');
    });
}


// PRODUCT TYPE

function setupTypeSearchCall() {
    $('#type_by_id_btn').on('click', function () {
        var type_id = $('#type_by_id').val();
        if (!isInteger(type_id)) {
            showError('Type ID must be an integer.');
            return;
        }

        postToAPI('/product/type/byid', {
            id: type_id
        }, typeLoadSuccess, null, 'Looking up type...');
    });
}

function setupCreateTypeCall() {
    $('#create_type_btn').on('click', function () {
        typeLoadSuccess({
            id: 'Not assigned yet',
            name: '',
            type: '',
            line: '',
            description: ''
        });
    });
}

function typeLoadSuccess(response) {
    log(response);
    $('#edit_type_id').val(response.id);
    $('#edit_type_name').val(response.name).data('original', response.name);
    $('#edit_type_description').val(response.description);
    $('#edit_type').show();
}

function setupTypeEditSaveCall() {
    $('#save_type_btn').on('click', function () {
        var type_id = $('#edit_type_id').val();
        if (type_id === 'Not assigned yet') {
            type_id = '-1';
        }
        var new_type_info = {
            id: type_id,
            name: $('#edit_type_name').val(),
            type: $('#edit_type_type').val(),
            line: $('#edit_type_line').val(),
            description: $('#edit_type_description').val()
        };

        postToAPI('/admin/producttype/update', new_type_info, null, null, 'Updating type...');
    });
}

function setupDatabaseManager() {
    $('#import_btn').on('click', function () {
        postToAPI('/admin/import', {}, null, null, 'Importing data to database...');
    });
    $('#export_btn').on('click', function () {
        postToAPI('/admin/export', {}, null, null, 'Exporting database...');
    });
}

function listenForEnter() {
    $("input").focus(function () {
        $(this).addClass('focused');
    }).blur(function () {
        $(this).removeClass('focused');
    });

    $(document.body).on('keyup', function (e) {
        // 13 is ENTER
        if (e.which === 13 && $('.focused').length > 0) {
            var btn_id = $('.focused').attr('id') + '_btn';
            $('#' + btn_id).click();
        }
    });
}

// FEEDBACK

function feedbackHTML(feedback) {
    var $div = $('<div/>', { class: 'feedback_item' });
    $div.append($('<h2/>').text(SW.FEEDBACK[feedback.type]({ user: feedback.reported_by })));
    $div.append($('<p/>').text(feedback.content));
    $div.append($('<p/>').html('Happened ' + getReadableTime(feedback.timestamp) + ' at <a href="' + feedback.path + '">' + feedback.path + '</a>'));


    return $div;
}

function fetchFeedback() {
    postToAPI('/admin/report/list', {}, loadFeedback);
}

function loadFeedback(response) {
    for (var i = 0; i < response.results.length; i++) {
        $('.feedback_container').append(feedbackHTML(response.results[i]));
    }
}

// Get a human friendly description of a timestamp that's relative to current time
function getReadableTime(timestamp) {
    var now = Date.now();
    var timestamp = Math.min(parseInt(timestamp, 10), now);
    var seconds = (now - timestamp) / 1000;

    // If last update was within 1 min, we consider the device online
    if (seconds <= 60) {
        return "just now";
    }

    // Less than an hour
    var minutes = Math.floor(seconds / 60);
    if (minutes < 60) {
        if (minutes === 1) {
            return "1 minute ago";
        } else {
            return minutes + " minutes ago";
        }
    }

    // Less than a day
    var hours = Math.floor(minutes / 60);
    if (hours < 24) {
        if (hours === 1) {
            return "last hour";
        } else {
            return hours + " hours ago";
        }
    }

    // Less than 31 days
    var past_date = new Date(timestamp);
    var now_date = new Date(now);

    var that_day = new Date(past_date.getFullYear(), past_date.getMonth(), past_date.getDate(), 0, 0, 0, 0);
    var today = new Date(now_date.getFullYear(), now_date.getMonth(), now_date.getDate(), 0, 0, 0, 0);

    var milliseconds_in_day = 1000 * 60 * 60 * 24;
    var day_diff = (today.getTime() - that_day.getTime()) / milliseconds_in_day;

    if (day_diff <= 31) {
        if (day_diff === 1) {
            return "yesterday";
        } else {
            return day_diff + " days ago";
        }
    }

    // Same year
    if (past_date.getFullYear() === now_date.getFullYear()) {
        var month_diff = now_date.getMonth() - past_date.getMonth();
        if (month_diff === 1) {
            return "last month";
        } else {
            return month_diff + " months ago";
        }
    }

    // Last year but within 12 months
    if (past_date.getFullYear() === now_date.getFullYear() - 1) {
        var month_diff = now_date.getMonth() + (11 - past_date.getMonth());
        if (month_diff < 12) {
            return month_diff + " month ago";
        }
    }

    return "on " + getMonthName(past_date.getMonth()) + " " + past_date.getDate() + ", " + past_date.getFullYear();
}


$(document).ready(function() {
    setupTabSystem();

    setupUserSearchCall();
    setupUserEditSaveCall();
    setupUserDeleteCall();

    setupGroupSearchCall();
    setupGroupEditSaveCall();
    setupGroupDeleteCall();
    setupCreateGroupCall();

    setupIngredientSearchCall();
    setupIngredientEditSaveCall();
    setupCreateIngredientCall();

    setupProductSearchCall();
    setupProductEditSaveCall();
    setupCreateProductCall();

    setupFunctionSearchCall();
    setupFunctionEditSaveCall();
    setupCreateFunctionCall();

    setupBrandSearchCall();
    setupBrandEditSaveCall();
    setupCreateBrandCall();

    setupTypeSearchCall();
    setupTypeEditSaveCall();
    setupCreateTypeCall();

    setupDatabaseManager();

    fetchFeedback();

    listenForEnter();
});