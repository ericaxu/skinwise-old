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

function setupUserSearchCall() {
    $('#user_by_id_btn').on('click', function() {
        var user_id = $('#user_by_id').val();
        if (!isInteger(user_id)) {
            showError('User ID must be an integer.');
            return;
        }

        postToAPI('/admin/user/byid', {
            id: user_id
        }, userSearchSuccess, null, 'Looking up user...');
    });

    $('#user_by_email_btn').on('click', function() {
        var user_email = $('#user_by_email').val();

        postToAPI('/admin/user/byemail', {
            email: user_email
        }, userSearchSuccess, null, 'Looking up user...');
    });
}

function userSearchSuccess(response) {
    $('#edit_user_id').val(response.id);
    $('#edit_user_name').val(response.name);
    $('#edit_user_email').val(response.email);
    $('#edit_user_group').val(response.group);
    $('#edit_user_permissions').val(response.permissions.join(SW.CONFIG.PERMISSION_DELIMITER));
    $('.edit_user_container').show();
}

function setupUserDeleteCall() {
    $('#delete_user_btn').on('click', function() {
        var user_id = $('#edit_user_id').val();
        if (!isInteger(user_id)) {
            showError('User ID must be an integer.');
            return;
        }
        confirmAction('delete user ' + user_id, function() {
            postToAPI('/admin/user/delete', {
                id: user_id
            }, hideUserEdit, null, 'Deleting user ' + user_id);
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

        postToAPI('/admin/user/update', new_user_info, null, null, 'Updating user...');
    })
}

function hideUserEdit() {
    $('.edit_user_container').hide();
}


function setupGroupSearchCall() {
    $('#group_by_id_btn').on('click', function() {
        var group_id = $('#group_by_id').val();
        if (!isInteger(group_id)) {
            showError('Group ID must be an integer.');
            return;
        }

        postToAPI('/admin/group/byid', {
            id: group_id
        }, groupSearchSuccess, null, 'Looking up group...');
    });

    $('#group_by_name_btn').on('click', function() {
        var group_name = $('#group_by_name').val();

        postToAPI('/admin/group/byname', {
            name: group_name
        }, groupSearchSuccess, null, 'Looking up group...');
    });
}

function groupSearchSuccess(response) {
    $('#edit_group_id').val(response.id);
    $('#edit_group_name').val(response.name);
    $('#edit_group_permissions').val(response.permissions.join(SW.CONFIG.PERMISSION_DELIMITER));
    $('.edit_group_container').show();
}

function setupGroupDeleteCall() {
    $('#delete_group_btn').on('click', function() {
        var group_id = $('#edit_group_id').val();
        if (!isInteger(group_id)) {
            showError('Group ID must be an integer.');
            return;
        }
        confirmAction('delete group ' + group_id, function() {
            postToAPI('/admin/group/delete', {
                id: group_id
            }, hideGroupEdit, null, 'Deleting group ' + group_id);
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

        postToAPI('/admin/group/update', new_group_info, null, null, 'Updating group...');
    });
}

function hideGroupEdit() {
    $('.edit_group_container').hide();
}

function setupCreateGroupCall() {
    $('#create_group_btn').on('click', function() {
        groupSearchSuccess({
            id: 'Not assigned yet',
            name: '',
            permissions: []
        });
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

    $('#import_ingredient_btn').on('click', function() {
        postToAPI('/admin/import/ingredients', {}, null, null, 'Importing ingredent database...');
    });

    $('#import_product_btn').on('click', function() {
        postToAPI('/admin/import/products', {}, null, null, 'Importing product database...');
    });
});