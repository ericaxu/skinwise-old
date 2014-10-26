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
        }, ingredientLoadSuccess, null, 'Looking up ingredient...');
    });
}

function ingredientLoadSuccess(response) {
    $('#edit_ingredient_id').val(response.id);
    $('#edit_ingredient_name').val(response.name).data('original', response.name);
    $('#edit_ingredient_cas_number').val(response.cas_number);
    $('#edit_ingredient_popularity').val(response.popularity);
    $('#edit_ingredient_description').val(response.description);
    $('#edit_ingredient_functions').val(response.functions.join(SW.CONFIG.PERMISSION_DELIMITER));
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

    $('#product_by_id_btn').on('click', function() {
        var product_id = $('#product_by_id').data('id');

        postToAPI('/product/byid', {
            id: product_id
        }, productLoadSuccess, null, 'Looking up product...');
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

function productLoadSuccess(response) {
    log(response);
    $('#edit_product_id').val(response.id);
    $('#edit_product_name').val(response.name).data('original', response.name);
    $('#edit_product_brand').val(response.brand);
    $('#edit_product_line').val(response.line);
    $('#edit_product_image').val(response.image);
    $('#edit_product_popularity').val(response.popularity);
    $('#edit_product_description').val(response.description);
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
            brand: $('#edit_product_brand').val(),
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

        postToAPI('/ingredient/function/byid', {
            id: function_id
        }, functionLoadSuccess, null, 'Looking up function...');
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

function functionLoadSuccess(response) {
    $('#edit_function_id').val(response.id);
    $('#edit_function_name').val(response.name).data('original', response.name);
    $('#edit_function_description').val(response.description);
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

        postToAPI('/product/brand/byid', {
            id: brand_id
        }, brandLoadSuccess, null, 'Looking up brand...');
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

function brandLoadSuccess(response) {
    log(response);
    $('#edit_brand_id').val(response.id);
    $('#edit_brand_name').val(response.name).data('original', response.name);
    $('#edit_brand_brand').val(response.brand);
    $('#edit_brand_line').val(response.line);
    $('#edit_brand_image').val(response.image);
    $('#edit_brand_description').val(response.description);
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

        postToAPI('/product/type/byid', {
            id: type_id
        }, typeLoadSuccess, null, 'Looking up type...');
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

function typeLoadSuccess(response) {
    $('#edit_type_id').val(response.id);
    $('#edit_type_name').val(response.name).data('original', response.name);
    $('#edit_type_description').val(response.description);
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
    postToAPI('/ingredient/unmatched', {
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

    listenForEnter();
});