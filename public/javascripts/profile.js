function setupProfileAutocomplete() {
    var profile_fields = [ 'add_ingredient_work', 'add_ingredient_doesnt_work', 'add_ingredient_bad_reaction' ];
    for (var i = 0; i < profile_fields.length; i++) {
        var name = profile_fields[i];
        enableAutocomplete('ingredient',
            $('#' + name),
            '#' + name + '_container',
            SW.AUTOCOMPLETE_LIMIT.ADD_FILTER,
            $('#' + name + '_not_found'));
    }
}

function handleAddIngredientToProfile() {
    $('#add_ingredient_work_btn').on('click', function() {
        var name = $('#add_ingredient_work').val();
        var id = $('#add_ingredient_work').data('id');

        var list = $(this).parent().parent().find('.preference_list');
        list.append(profileListItemHTML(name, id));
    });
}

function profileListItemHTML(name, id) {
    var $li = $('<li/>');
    $li.append($('<a/>', {
        href: '/ingredient/' + id,
        text: name
    }));
    $li.append($('<span/>', {
        class: 'delete_preference',
        title: 'Delete this ingredient'
    }));

    return $li;
}

$(document).on('ready', function() {
    $('.profile_add_input').on('focus', function() {
        $(this).next().show();
    }).on('blur', function() {
        if ($(this).val() === '') {
            $(this).next().hide();
        }
    });

    setupProfileAutocomplete();
    handleAddIngredientToProfile();
});