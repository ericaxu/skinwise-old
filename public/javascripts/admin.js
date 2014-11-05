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


function setupDatabaseManager() {
    $('#import_btn').on('click', function() {
        confirmAction('import to database', function() {
            postToAPI('/admin/import', {}, null, null, 'Importing data to database...');
        });
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

// FEEDBACK

function feedbackHTML(feedback) {
    var $div = addEl('div', null, 'feedback_item');
    addEl('h2', $div, '', SW.FEEDBACK[feedback.type]({user: feedback.reported_by || 'Someone'}));
    addEl('p', $div, '', feedback.content);
    var $contact = addEl('p', $div, '', 'Reach me at ');
    addEl('span', $contact, 'emphasis', feedback.email);
    var $timestamp = addEl('p', $div, '', 'Reported ' + getReadableTime(feedback.timestamp) + ' at ');
    addEl('a', $timestamp, '', feedback.path, { href: feedback.path });
    addEl('input', $div, '', '', {
        type: 'button',
        value: 'Mark resolved'
    }).on('click', function() {
        postToAPI('/admin/report/resolve', {
            id: feedback.id
        }, fetchFeedback, null, 'Marking feedback resolved...');
    });
    return $div;
}

function fetchFeedback() {
    postToAPI('/admin/report/list', {}, loadFeedback, null, 'Fetching feedback...');
}

function loadFeedback(response) {
    var $container = $('.feedback_container');
    $container.empty();
    for (var i = 0; i < response.results.length; i++) {
        $container.append(feedbackHTML(response.results[i]));
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

    setupDatabaseManager();

    setupUserSearchCall();
    setupUserEditSaveCall();
    setupUserDeleteCall();

    setupGroupSearchCall();
    setupGroupEditSaveCall();
    setupGroupDeleteCall();
    setupCreateGroupCall();

    $('#refresh_feedback_btn').on('click', function() {
        if ($(this).val() === 'Load') {
            $(this).val('Refresh');
        }
        fetchFeedback();
    });

    $('#refresh_unmatched_btn').on('click', function() {
        if ($(this).val() === 'Load') {
            $(this).val('Refresh');
        }
        fetchUnmatched();
    });

    listenForEnter();
});