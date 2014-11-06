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

        if (name == 'type') {
            var url = '/producttype/byid';
        } else {
            var url = '/' + name + '/byid';
        }

        postToAPI(url, {
            id: id
        }, function(response) {
            var item = response.results[0];

            for (var i = 0; i < data.fields.length; i++) {
                var field_name = data.fields[i].name;
                $('#edit_' + name + '_' + field_name).val(item[field_name]);
                if (data.key == field_name) {
                    $('#edit_' + name + '_' + field_name).data('original', item[field_name]);
                }
            }

            $('#edit_' + name).show();

        }, null, 'Looking up ' + name + '...');
    });
    addEl('input', $tab, 'primary', '', {
        id: 'create_' + name + '_btn',
        value: 'Create ingredient',
        type: 'button'
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
    });

    enableAutocomplete(name, $search_input, '#' + name + '_tab .inputs', SW.AUTOCOMPLETE_LIMIT.EDITOR);

    if (name == 'product') {
        enableAutocomplete('brand', $('#edit_product_brand'), '#edit_product .inputs', SW.AUTOCOMPLETE_LIMIT.EDITOR);
    }

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
            popularity: $('#edit_ingredient_popularity').val(),
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


// PRODUCT

function setupProductSearchCall() {
    enableAutocomplete('product', $('#product_by_id'), '#product_tab .inputs', SW.AUTOCOMPLETE_LIMIT.EDITOR);

    $('#product_by_id_btn').on('click', function() {
        var product_id = $('#product_by_id').data('id');

        postToAPI('/product/byid', {
            id: product_id
        }, function(response) {
            productLoadSuccess(response.results[0]);
        }, null, 'Looking up product...');
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


// FUNCTION


function setupCreateFunctionCall() {
    $('#create_function_btn').on('click', function() {
        functionLoadSuccess({
            id: 'Not assigned yet',
            name: '',
            brand: '',
            line: '',
            description: ''
        });
    });
}

function setupFunctionEditSaveCall() {
    $('#save_function_btn').on('click', function() {
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

function setupCreateBrandCall() {
    $('#create_brand_btn').on('click', function() {
        brandLoadSuccess({
            id: 'Not assigned yet',
            name: '',
            brand: '',
            line: '',
            description: ''
        });
    });
}

function setupBrandEditSaveCall() {
    $('#save_brand_btn').on('click', function() {
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

function setupCreateTypeCall() {
    $('#create_type_btn').on('click', function() {
        typeLoadSuccess({
            id: 'Not assigned yet',
            name: '',
            type: '',
            line: '',
            description: ''
        });
    });
}

function setupTypeEditSaveCall() {
    $('#save_type_btn').on('click', function() {
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

    //setupIngredientSearchCall();
    setupIngredientEditSaveCall();
    setupCreateIngredientCall();

    //setupProductSearchCall();
    setupProductEditSaveCall();
    setupCreateProductCall();

    //setupFunctionSearchCall();
    setupFunctionEditSaveCall();
    setupCreateFunctionCall();

    //setupBrandSearchCall();
    setupBrandEditSaveCall();
    setupCreateBrandCall();

    //setupTypeSearchCall();
    setupTypeEditSaveCall();
    setupCreateTypeCall();

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