function setupIngredientInfobox() {
    var $ingredient = $('.ingredient');

    $ingredient.on('mouseenter', function (e) {
        var id = $(this).data('id');
        clearTimeout(SW.ING_BOX.DISMISS_TIMEOUT_ID);
        clearTimeout(SW.ING_BOX.TIMEOUT_ID);
        SW.ING_BOX.TIMEOUT_ID = setTimeout(function() {
            if (SW.ING[id]) {
                var ingredient_data = SW.ING[id];
                var ingredient_info = $('<div/>').on('click', function (e) {
                    e.stopPropagation();
                });
                var close_button = $('<span/>', { class: 'close_btn' }).on('click', function () {
                    $('.ingredient_infobox').remove();
                })
                ingredient_info.append(close_button);
                ingredient_info.append($('<h2/>', { text: ingredient_data.name }));
                var functions = $('<p/>', { class: 'functions' });
                for (var i = 0; i < ingredient_data.functions.length; i++) {
                    var func_id = ingredient_data.functions[i];
                    if (SW.FUNC[func_id]) {
                        functions.append($('<span/>', {
                            class: 'function neutral',
                            text: fullyCapitalize(SW.FUNC[func_id].name)
                        }).data('id', func_id));
                    }
                }
                ingredient_info.append(functions);
                ingredient_info.appendTo('body');
                ingredient_info.append($('<p/>', { text: ingredient_data.description }));
                $('.ingredient_infobox').remove();
                ingredient_info.addClass('ingredient_infobox').show().offset({ top: e.pageY + 10, left: e.pageX + 5 });
            }
        }, SW.ING_BOX.TIMEOUT);
    }).on('mouseleave', function() {
        clearTimeout(SW.FUNC_BOX.TIMEOUT_ID);
        SW.ING_BOX.DISMISS_TIMEOUT_ID = setTimeout(function() {
            $('.ingredient_infobox').remove();
        }, SW.ING_BOX.DISMISS_TIMEOUT);
    });

    // Dismiss infobox after leaving it for a while
    $(document).on('mouseenter', '.ingredient_infobox', function(e) {
        clearTimeout(SW.ING_BOX.TIMEOUT_ID);
        clearTimeout(SW.ING_BOX.DISMISS_TIMEOUT_ID);
    }).on('mouseleave', '.ingredient_infobox', function() {
        SW.ING_BOX.DISMISS_TIMEOUT_ID = setTimeout(function() {
            $('.ingredient_infobox').remove();
        }, SW.ING_BOX.DISMISS_TIMEOUT);
    });

    $(document).on('click', function () {
        $('.ingredient_infobox').remove();
    });
}

function setupFunctionInfobox() {
    $(document).on('mouseenter', '.function', function (e) {
        var id = $(this).data('id');
        clearTimeout(SW.FUNC_BOX.DISMISS_TIMEOUT_ID);
        SW.FUNC_BOX.TIMEOUT_ID = setTimeout(function() {
            if (SW.FUNC[id]) {
                var func_data = SW.FUNC[id];
                var func_info = $('<div/>').on('click', function (e) {
                    e.stopPropagation();
                });
                var close_button = $('<span/>', { class: 'close_btn' }).on('click', function () {
                    $('.function_infobox').remove();
                })
                func_info.append(close_button);
                func_info.append($('<h2/>', { text: func_data.name }));
                func_info.appendTo('body');
                func_info.append($('<p/>', { text: func_data.description }));
                $('.function_infobox').remove();
                func_info.addClass('function_infobox').show().offset({ top: e.pageY + 10, left: e.pageX + 10 });
            }
        }, SW.FUNC_BOX.TIMEOUT);
    }).on('mouseleave', '.function', function() {
        clearTimeout(SW.FUNC_BOX.TIMEOUT_ID);
        SW.FUNC_BOX.DISMISS_TIMEOUT_ID = setTimeout(function() {
            $('.function_infobox').remove();
        }, SW.FUNC_BOX.DISMISS_TIMEOUT);
    });

    // Dismiss infobox after leaving it for a while
    $(document).on('mouseenter', '.function_infobox', function() {
        clearTimeout(SW.FUNC_BOX.DISMISS_TIMEOUT_ID);
    }).on('mouseleave', '.function_infobox', function() {
        SW.FUNC_BOX.DISMISS_TIMEOUT_ID = setTimeout(function() {
            $('.function_infobox').remove();
        }, SW.FUNC_BOX.DISMISS_TIMEOUT);
    });

    $(document).on('click', function () {
        $('.function_infobox').remove();
    });
}

function getIngredientInfoSuccess(response) {
    for (var i = 0; i < response.results.length; i++) {
        var ingredient = response.results[i];
        SW.ING[ingredient.id] = {
            name: ingredient.name,
            description: ingredient.description,
            functions: ingredient.functions
        };
    }
}

function getFunctionsSuccess(response) {
    for (var i = 0; i < response.results.length; i++) {
        var func = response.results[i];
        SW.FUNC[func.id] = {
            name: fullyCapitalize(func.name),
            description: func.description
        };
    }
}

function getContainingProductsSuccess(response) {
    var number_shown = Math.min(response.results.length, SW.CONFIG.CONTAINING_PRODUCT_NUM);

    for (var i = 0; i < number_shown; i++) {
        $('.contained_products ul').append(productResultHTML(response.results[i]));
    }
}

$(document).ready(function () {
    setupIngredientInfobox();
    setupFunctionInfobox();

    enableAutocomplete($('#search_category_select').val(), '#nav_searchbar', '.search_container', SW.AUTOCOMPLETE_LIMIT.NAV_SEARCH);

    $('#search_category_select').on('change', function() {
        enableAutocomplete($('#search_category_select').val(), '#nav_searchbar', '.search_container', SW.AUTOCOMPLETE_LIMIT.NAV_SEARCH);
        $('#nav_searchbar').val('');
    });

    $('#nav_searchbar_btn').on('click', function() {
        // TODO: check if id is valid
        var id = $('#nav_searchbar').data('id');
        location.href = '/' + $('#search_category_select').val() + '/' + id;
    });

    $(document).on('keyup', function(e) {
        // 13 is ENTER
        if (e.which === 13 && $('#nav_searchbar').is(':focus')) {
            $('#nav_searchbar_btn').trigger('click');
        }
    });

    postToAPI('/ingredient/functions', {}, getFunctionsSuccess);

    $('.profile_add_input').on('focus', function () {
        $(this).next().show();
    }).on('blur', function () {
        if ($(this).val() === '') {
            $(this).next().hide();
        }
    });

    $('.expand_subnav').on('mouseenter', function () {
        $('.sub_navbar').fadeIn(SW.CONFIG.SUBNAV_FADE_IN);
    }).on('mouseleave', function () {
        $('.sub_navbar').fadeOut(SW.CONFIG.SUBNAV_FADE_OUT);
    });
});