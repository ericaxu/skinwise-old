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
        list.append(preferenceListItemHTML(name, id));
    });
}

function preferenceListItemHTML(name, id) {
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

function loadPreferenceList(list) {
    var $div = $('#' + list.id + '_section');
    var $list = $('<ul/>', { class: 'preference_list' });
    for (var i = 0; i < list.items.length; i++) {
        var item = list.items[i];
        $list.append(preferenceListItemHTML(item.name, item.id));
    }
    $div.append($list);
    var $add_ingredient = $('<div/>', { class: 'field ' + list.id + '_add' });
    $add_ingredient.append($('<label/>').text('Add another: '));
    var $add_ingredient_input = $('<input/>');
    $add_ingredient.append($add_ingredient_input);
    $div.append($add_ingredient);
    var $add_ingredient_btn = $('<input/>', { type: 'button', value: 'Add' }).on('click', function() {
        var name = $add_ingredient_input.val();
        var id = $add_ingredient_input.data('id');
        $list.append(preferenceListItemHTML(name, id))
        $add_ingredient_input.val('');
    });
    $div.append($add_ingredient_btn);
    var $no_result = $('<div/>', { class: 'preference_not_found' }).text('No results found.');
    $div.append($no_result);
    enableAutocomplete('ingredient', $add_ingredient_input, '.' + list.id + '_add', SW.AUTOCOMPLETE_LIMIT.ADD_FILTER, $no_result);
}

function fetchIngredientPreference(key) {
    postToAPI('/user/ingredient/get', {
        key: key
    }, function(response) {
        loadPreferenceList({
            id: key,
            items: response.results
        });
    });
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

    for (var i = 0; i < SW.PREFERENCES.INGREDIENT.length; i++) {
        var key = SW.PREFERENCES.INGREDIENT[i];
        fetchIngredientPreference(key);
    }
});