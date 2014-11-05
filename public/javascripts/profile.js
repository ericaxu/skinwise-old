function preferenceListItemHTML(name, id, list_key) {
    var $li = $('<li/>').data('id', id);
    addEl('a', $li, 'preference_list_tem', name, { href: '/ingredient/' + id });
    $li.append(' (');
    addEl('a', $li, 'delete_preference', 'remove', { href: '#' }).on('click', function(e) {
        e.preventDefault();

        $li.remove();
        postToAPI('/user/ingredient/set', {
            key: list_key,
            ids: getIdsInList($('#' + list_key + '_section').find('.preference_list'))
        }, null, function() {
            showError('Failed to save your preferences. Please try again.');
        });
    });
    $li.append(')');
    return $li;
}

function getIdsInList($list) {
    var ids = [];
    log($list);
    $list.find('li').each(function() {
        ids.push($(this).data('id'));
    });

    return ids;
}

function loadPreferenceList(list) {
    var $div = $('#' + list.id + '_section');
    var $list = addEl('ul', $div, 'preference_list');
    for (var i = 0; i < list.items.length; i++) {
        var item = list.items[i];
        $list.append(preferenceListItemHTML(item.name, item.id, list.id));
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

        $list.append(preferenceListItemHTML(name, id, list.id));
        postToAPI('/user/ingredient/set', {
            key: list.id,
            ids: getIdsInList($list)
        }, function() {
            $add_ingredient_input.val('');
        }, function() {
            showError('Failed to save your preferences. Please try again.');
        });
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

    for (var i = 0; i < SW.PREFERENCES.INGREDIENT.length; i++) {
        var key = SW.PREFERENCES.INGREDIENT[i];
        fetchIngredientPreference(key);
    }
});