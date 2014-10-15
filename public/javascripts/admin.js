function setupTabSystem() {
    var current_tab = $('.current').data('tabName');
    $('.tab').hide();
    $('#' + current_tab).show();
    $('.tab_title').on('click', function(e) {
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

// USERS

function setupUserSearchCall() {
    $('#user_by_id_btn').on('click', function() {
        var user_id = $('#user_by_id').val();
        if (!isInteger(user_id)) {
            showError('User ID must be an integer.');
            return;
        }

        postToAPI('/admin/user/byid', {
            id: user_id
        }, userLoadSuccess, null, 'Looking up user...');
    });

    $('#user_by_email_btn').on('click', function() {
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
    $('#delete_user_btn').on('click', function() {
        var user_email = $('#edit_user_email').data('original');
        confirmAction('delete user ' + user_email, function() {
            postToAPI('/admin/user/delete', {
                id: $('#edit_user_id').val()
            }, hideEdit, null, 'Deleting user ' + user_email);
        });
    });
}

function setupUserEditSaveCall() {
    $('#save_user_btn').on('click', function() {
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
    $('#group_by_id_btn').on('click', function() {
        var group_id = $('#group_by_id').val();
        if (!isInteger(group_id)) {
            showError('Group ID must be an integer.');
            return;
        }

        postToAPI('/admin/group/byid', {
            id: group_id
        }, groupLoadSuccess, null, 'Looking up group...');
    });

    $('#group_by_name_btn').on('click', function() {
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
    $('#delete_group_btn').on('click', function() {
        var group_name = $('#edit_group_name').data('original');
        confirmAction('delete group ' + group_name, function() {
            postToAPI('/admin/group/delete', {
                id: $('#edit_group_id').val()
            }, hideEdit, null, 'Deleting group ' + group_name);
        });
    });
}

function setupGroupEditSaveCall() {
    $('#save_group_btn').on('click', function() {
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
    $('#create_group_btn').on('click', function() {
        groupLoadSuccess({
            id: 'Not assigned yet',
            name: '',
            permissions: []
        });
    });
}


// INGREDIENTS

function setupIngredientSearchCall() {
    $('#ingredient_by_id_btn').on('click', function() {
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
    $('#edit_ingredient_description').val(response.description);
    $('#edit_ingredient_functions').val(response.functions.join(SW.CONFIG.PERMISSION_DELIMITER));
    $('#edit_ingredient').show();
}

function setupIngredientDeleteCall() {
    $('#delete_ingredient_btn').on('click', function() {
        var ingredient_name = $('#edit_ingredient_name').data('original');
        confirmAction('delete ingredient ' + ingredient_name, function() {
            postToAPI('/admin/ingredient/delete', {
                id: $('#edit_ingredient_id').val()
            }, hideEdit, null, 'Deleting ingredient ' + ingredient_name);
        });
    });
}

function setupIngredientEditSaveCall() {
    $('#save_ingredient_btn').on('click', function() {
        var ingredient_id = $('#edit_ingredient_id').val();
        if (ingredient_id === 'Not assigned yet') {
            ingredient_id = '-1';
        }
        var new_ingredient_info = {
            id: ingredient_id,
            name: $('#edit_ingredient_name').val(),
            cas_number: $('#edit_ingredient_cas_number').val(),
            description: $('#edit_ingredient_description').val(),
            functions: $('#edit_ingredient_functions').val().split(SW.CONFIG.PERMISSION_DELIMITER)
        };

        postToAPI('/admin/ingredient/update', new_ingredient_info, null, null, 'Updating ingredient...');
    });
}

function setupCreateIngredientCall() {
    $('#create_ingredient_btn').on('click', function() {
        ingredientLoadSuccess({
            id: 'Not assigned yet',
            name: '',
            cas_name: '',
            description: '',
            functions: []
        });
    });
}

// Product

function setupProductSearchCall() {
    $('#product_by_id_btn').on('click', function() {
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
    $('#create_product_btn').on('click', function() {
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
    $('#edit_product_id').val(response.id);
    $('#edit_product_name').val(response.name).data('original', response.name);
    $('#edit_product_brand').val(response.brand);
    $('#edit_product_line').val(response.line);
    $('#edit_product_description').val(response.description);
    $('#edit_ingredient').show();
}

function setupProductDeleteCall() {
    $('#delete_product_btn').on('click', function() {
        var product_name = $('#edit_product_name').data('original');
        confirmAction('delete product ' + product_name, function() {
            postToAPI('/admin/product/delete', {
                id: $('#edit_product_id').val()
            }, hideEdit, null, 'Deleting product ' + product_name);
        });
    });
}

function setupProductEditSaveCall() {
    $('#save_product_btn').on('click', function() {
        var product_id = $('#edit_product_id').val();
        if (product_id === 'Not assigned yet') {
            product_id = '-1';
        }
        var new_product_info = {
            id: product_id,
            name: $('#edit_product_name').val(),
            brand: $('#edit_product_brand').val(),
            line: $('#edit_product_line').val(),
            description: $('#edit_product_description').val()
        };

        postToAPI('/admin/product/update', new_product_info, null, null, 'Updating product...');
    });
}

function setupDatabaseManager() {
    $('#import_btn').on('click', function() {
        postToAPI('/admin/import', {}, null, null, 'Importing data to database...');
    });
    $('#export_btn').on('click', function() {
        postToAPI('/admin/export', {}, null, null, 'Exporting database...');
    });
}

function listenForEnter() {
    $("input").focus(function() {
        $(this).addClass('focused');
    }).blur(function() {
        $(this).removeClass('focused');
    });

    $(document.body).on('keyup', function(e) {
        // 13 is ENTER
        if (e.which === 13 && $('.focused').length > 0) {
            var btn_id = $('.focused').attr('id') + '_btn';
            $('#' + btn_id).click();
        }
    });
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
    setupIngredientDeleteCall();
    setupCreateIngredientCall();

    setupProductSearchCall();
    setupProductEditSaveCall();
    setupProductDeleteCall();
    setupCreateProductCall();

    setupDatabaseManager();

    listenForEnter();
});