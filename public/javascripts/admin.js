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

        postToAPI('/admin/userbyid', {
            id: user_id
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
            postToAPI('/admin/deleteuser', {
                id: user_id
            }, hideUserEdit, null, 'Deleting user ' + user_id);
        });
    });
}

function hideUserEdit() {
    $('.edit_user_container').hide();
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

        postToAPI('/admin/edituser', new_user_info, null, null, 'Updating user...');
    })
}

$(document).ready(function() {
    setupTabSystem();
    setupUserSearchCall();
    setupUserEditSaveCall();
    setupUserDeleteCall();
});