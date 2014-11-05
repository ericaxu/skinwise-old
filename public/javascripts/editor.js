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


// INGREDIENTS

function setupIngredientSearchCall() {
    enableAutocomplete('ingredient', $('#ingredient_by_id'), '#ingredient_tab .inputs', SW.AUTOCOMPLETE_LIMIT.EDITOR);

    $('#ingredient_by_id_btn').on('click', function() {
        var ingredient_id = $('#ingredient_by_id').data('id');

        postToAPI('/ingredient/byid', {
            id: ingredient_id
        }, function(response) {
            ingredientLoadSuccess(response.results[0]);
        }, null, 'Looking up ingredient...');
    });
}

function ingredientLoadSuccess(ingredient) {
    $('#edit_ingredient_id').val(ingredient.id);
    $('#edit_ingredient_name').val(ingredient.name).data('original', ingredient.name);
    $('#edit_ingredient_cas_number').val(ingredient.cas_number);
    $('#edit_ingredient_popularity').val(ingredient.popularity);
    $('#edit_ingredient_description').val(ingredient.description);
    $('#edit_ingredient_functions').val(ingredient.functions.join(SW.CONFIG.PERMISSION_DELIMITER));
    $('#edit_ingredient').show();
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
    enableAutocomplete('brand', $('#edit_product_brand'), '#edit_product .inputs', SW.AUTOCOMPLETE_LIMIT.EDITOR);

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

function setupFunctionSearchCall() {
    enableAutocomplete('function', $('#function_by_id'), '#function_tab .inputs', SW.AUTOCOMPLETE_LIMIT.EDITOR);

    $('#function_by_id_btn').on('click', function() {
        var function_id = $('#function_by_id').data('id');

        postToAPI('/function/byid', {
            id: function_id
        }, function(response) {
            functionLoadSuccess(response.results[0]);
        }, null, 'Looking up function...');
    });
}

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

function functionLoadSuccess(function_obj) {
    $('#edit_function_id').val(function_obj.id);
    $('#edit_function_name').val(function_obj.name).data('original', function_obj.name);
    $('#edit_function_description').val(function_obj.description);
    $('#edit_function').show();
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

function setupBrandSearchCall() {
    enableAutocomplete('brand', $('#brand_by_id'), '#brand_tab .inputs', SW.AUTOCOMPLETE_LIMIT.EDITOR);

    $('#brand_by_id_btn').on('click', function() {
        var brand_id = $('#brand_by_id').data('id');

        postToAPI('/brand/byid', {
            id: brand_id
        }, function(response) {
            brandLoadSuccess(response.results[0]);
        }, null, 'Looking up brand...');
    });
}

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

function brandLoadSuccess(brand) {
    $('#edit_brand_id').val(brand.id);
    $('#edit_brand_name').val(brand.name).data('original', brand.name);
    $('#edit_brand_brand').val(brand.brand);
    $('#edit_brand_line').val(brand.line);
    $('#edit_brand_image').val(brand.image);
    $('#edit_brand_description').val(brand.description);
    $('#edit_brand').show();
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

function setupTypeSearchCall() {
    enableAutocomplete('type', $('#type_by_id'), '#type_tab .inputs', SW.AUTOCOMPLETE_LIMIT.EDITOR);

    $('#type_by_id_btn').on('click', function() {
        var type_id = $('#type_by_id').data('id');

        postToAPI('/producttype/byid', {
            id: type_id
        }, function(response) {
            typeLoadSuccess(response.results[0]);
        }, null, 'Looking up type...');
    });
}

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

function typeLoadSuccess(type) {
    $('#edit_type_id').val(type.id);
    $('#edit_type_name').val(type.name).data('original', type.name);
    $('#edit_type_description').val(type.description);
    $('#edit_type').show();
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
    var $div = $('<div/>', {class: 'unmatched_alias_item'});
    $div.append($('<h2/>').text(alias.name));
    $div.append($('<p/>').text(alias.description || ''));
    var $input_container = $('<div/>', {id: 'unmatched_' + alias.id});
    var $ingredient_search = $('<input/>');
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

    setupIngredientSearchCall();
    setupIngredientEditSaveCall();
    setupCreateIngredientCall();

    setupProductSearchCall();
    setupProductEditSaveCall();
    setupCreateProductCall();

    setupFunctionSearchCall();
    setupFunctionEditSaveCall();
    setupCreateFunctionCall();

    setupBrandSearchCall();
    setupBrandEditSaveCall();
    setupCreateBrandCall();

    setupTypeSearchCall();
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