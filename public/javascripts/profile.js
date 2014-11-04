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
    var $list = addEl('ul', $div, 'preference_list');
    for (var i = 0; i < list.items.length; i++) {
        var item = list.items[i];
        $list.append(preferenceListItemHTML(item.name, item.id));
    }
    var $add_ingredient = addEl('div', $div, 'field ' + list.id + '_add');
    addEl('label', $add_ingredient, '', 'Add another: ');
    var $add_ingredient_input = addEl('input', $add_ingredient);
    var $add_ingredient_btn = addEl('input', $add_ingredient, '', '',  { type: 'button', value: 'Add' });
    $add_ingredient_btn.on('click', function() {
        var name = $add_ingredient_input.val();
        var id = $add_ingredient_input.data('id');

        if (name === '' || id === undefined) {
            return;
        }
        $list.append(preferenceListItemHTML(name, id))
        $add_ingredient_input.val('');
    });
    var $no_result = addEl('div', $div, 'preference_not_found', 'No results found.');
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