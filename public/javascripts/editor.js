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

function loadTab(name, data) {
    var $tab = $('#' + name + '_tab');
    var url_type = name === 'type' ? 'producttype' : name;
    addEl('h1', $tab, '', fullyCapitalize(name) + ' Editor');
    var $lookup_field = addEl('div', $tab, 'field');
    var $lookup_labels = addEl('div', $lookup_field, 'labels');
    addEl('label', $lookup_labels, '', 'Enter name:', { for: name + '_by_id' });
    var $lookup_inputs = addEl('div', $lookup_field, 'inputs');
    var $search_input = addEl('input', $lookup_inputs, '', '', { id: name + '_by_id' });
    addEl('input', $lookup_inputs, '', '', {
        id: name + '_by_id_btn',
        value: 'Search',
        type: 'button'
    }).on('click', function() {
        var id = $search_input.data('id');

        postToAPI('/' + url_type + '/byid', {
            id: id
        }, function(response) {
            var item = response.results[0];
            loadItem(item, name, data);
        }, null, 'Looking up ' + name + '...');
    });
    addEl('input', $tab, 'primary', '', {
        id: 'create_' + name + '_btn',
        value: 'Create ' + name,
        type: 'button'
    }).on('click', function() {
        loadItem({
            id: 'Not assigned yet'
        }, name, data);
    });

    var $edit_container = addEl('div', $tab, 'edit_container', '', { id: 'edit_' + name });
    addEl('h2', $edit_container, '', 'Edit ' + fullyCapitalize(name));
    var $edit_fields = addEl('div', $edit_container, 'field');
    var $edit_labels = addEl('div', $edit_fields, 'labels');
    var $edit_inputs = addEl('div', $edit_fields, 'inputs');

    for (var i = 0; i < data.fields.length; i++) {
        var field_name = data.fields[i].name;
        var label_text = data.fields[i].label;
        var type = data.fields[i].type;
        addEl('label', $edit_labels, '', label_text, { for: 'edit_' + name + '_' + field_name });
        if (i > 0) {
            addEl('br', $edit_inputs);
        }
        if (type == 'short_text') {
            addEl('input', $edit_inputs, '', '', { id: 'edit_' + name + '_' + field_name });
        } else if (type == 'long_text') {
            addEl('textarea', $edit_inputs, '', '', { id: 'edit_' + name + '_' + field_name });
        }
    }

    var $save_field = addEl('div', $edit_container, 'field');
    addEl('input', $save_field, 'primary', '', {
        type: 'button',
        id: 'save_' + name + '_btn',
        value: 'Save'
    }).on('click', function() {
        var type_id = $('#edit_' + name + '_id').val();
        var new_type_info = {
            id: type_id
        };

        for (var i = 0; i < data.fields.length; i++) {
            var field_name = data.fields[i].name;
            new_type_info[field_name] = $('#edit_' + name + '_' + field_name).val();
        }

        if (new_type_info.id === 'Not assigned yet') {
            new_type_info.id = '-1';
        }

        postToAPI('/admin/' + url_type + '/update', new_type_info, null, null, 'Updating ' + name + '...');
    });

    enableAutocomplete(name, $search_input, '#' + name + '_tab .inputs', SW.AUTOCOMPLETE_LIMIT.EDITOR);

    if (name == 'product') {
        enableAutocomplete('brand', $('#edit_product_brand'), '#edit_product .inputs', SW.AUTOCOMPLETE_LIMIT.EDITOR);
    }
}

function loadItem(item, content_type, data) {
    for (var i = 0; i < data.fields.length; i++) {
        var field_name = data.fields[i].name;
        $('#edit_' + content_type + '_' + field_name).val(item[field_name] || '');
        if (data.key == field_name) {
            $('#edit_' + content_type + '_' + field_name).data('original', item[field_name]);
        }
    }

    $('#edit_' + content_type).show();
}

function hideEdit() {
    $('.edit_container').hide();
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

// PRODUCT

function productLoadSuccess(product) {
    var brand_name = SW.BRANDS[product.brand].name || '';

    if (!brand_name) {
        showError('Brand ID ' + product.brand + ' not found.');
        return;
    }

    $('#edit_product_id').val(product.id);
    $('#edit_product_name').val(product.name).data('original', product.name);
    $('#edit_product_brand').val(brand_name).data('id', product.brand);
    $('#edit_product_line').val(product.line);
    $('#edit_product_price').val(product.price);
    $('#edit_product_size').val(product.size);
    $('#edit_product_size_unit').val(product.size_unit);
    $('#edit_product_image').val(product.image);
    $('#edit_product_popularity').val(product.popularity);
    $('#edit_product_description').val(product.description);
    $('#edit_product').show();
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
            brand_id: $('#edit_product_brand').data('id'),
            line: $('#edit_product_line').val(),
            image: $('#edit_product_image').val(),
            popularity: $('#edit_product_popularity').val(),
            description: $('#edit_product_description').val()
        };

        postToAPI('/admin/product/update', new_product_info, null, null, 'Updating product...');
    });
}

// UNMATCHED ALIAS

function unmatchedHTML(alias) {
    var $div = addEl('div', null, 'unmatched_alias_item');
    addEl('h2', $div, '', alias.name);
    addEl('p', $div, '', alias.description || '');
    var $input_container = addEl('div', null, '', '', {id: 'unmatched_' + alias.id});
    var $ingredient_search = addEl('input');
    enableAutocomplete('ingredient', $ingredient_search, '#' + 'unmatched_' + alias.id)
    var $mark_resolved = $('<input/>', {
        type: 'button',
        value: 'Mark resolved'
    }).on('click', function() {
        postToAPI('/admin/report/resolve', {
            id: feedback.id
        }, fetchFeedback, null, 'Marking feedback resolved...');
    });
    //$div.append($('<p/>').append($mark_resolved));

    return $div;
}

function fetchUnmatched() {
    postToAPI('/alias/unmatched', {
        page: 1
    }, loadUnmatched, null, 'Fetching unmatched alias...');
}

function loadUnmatched(response) {
    var $container = $('.unmatched_container');
    $container.empty();
    for (var i = 0; i < response.results.length; i++) {
        $container.append(unmatchedHTML(response.results[i]));
    }
}


// REMATCH ALIAS

function setupRematch() {
    enableAutocomplete('alias', $('#rematch_alias'), '#rematch_tab .inputs', SW.AUTOCOMPLETE_LIMIT.EDITOR);
    enableAutocomplete('ingredient', $('#rematch_ingredient'), '#rematch_tab .inputs', SW.AUTOCOMPLETE_LIMIT.EDITOR);

    $('#rematch_btn').on('click', function() {
        var request = {
            id: $('#rematch_alias').data('id'),
            ingredient_id: $('#rematch_ingredient').data('id'),
            name: $('#rematch_alias').val()
        }

        postToAPI('/admin/alias/update', request, function() {
            $('#rematch_alias, #rematch_ingredient').val('').data('id', null);
        }, 'Updating alias...');
    });

    $('#rematch_alias_lookup').on('click', function() {
        var id = $('#rematch_alias').data('id');
        postToAPI('/alias/byid', {
            id: id
        }, function(response) {
            var alias = response.results[0];
            if (alias.ingredient) {
                if (alias.name === fullyCapitalize(alias.ingredient.name)) {
                    $('#alias_match_info').empty()
                        .append('This ingredient already points to <a href="/ingredient/' + alias.ingredient.id + '">itself</a>');
                } else {
                    $('#alias_match_info').empty()
                        .append('Currently matched to <a href="/ingredient/' + alias.ingredient.id + '">'
                        + fullyCapitalize(alias.ingredient.name) + '</a>');
                }
            }

        }, null, 'Fetching alias #' + id);
    });
}


$(document).ready(function() {
    setupTabSystem();

    for (var tab in SW.EDITOR) {
        loadTab(tab, SW.EDITOR[tab]);
    }

    //setupProductEditSaveCall();

    $('#refresh_unmatched_btn').on('click', function() {
        if ($(this).val() === 'Load') {
            $(this).val('Refresh');
        }
        fetchUnmatched();
    });

    setupRematch();

    postToAPI('/brand/all', {}, getBrandsSuccess);

    listenForEnter();
});