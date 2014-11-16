function setupIngredientInfobox() {
    var $ingredient = $('.ingredient');

    $ingredient.on('mouseenter', function(e) {
        var id = $(this).data('id');
        clearTimeout(SW.ING_BOX.DISMISS_TIMEOUT_ID);
        clearTimeout(SW.ING_BOX.TIMEOUT_ID);
        SW.ING_BOX.TIMEOUT_ID = setTimeout($.proxy(function() {
            if (SW.ING[id]) {
                var ingredient_data = SW.ING[id];
                var $ingredient_info = addEl('div').on('click', function(e) {
                    e.stopPropagation();
                });
                addEl('span', $ingredient_info, 'close_btn').on('click', function() {
                    $('.ingredient_infobox').remove();
                });
                addEl('h2', $ingredient_info, '', ingredient_data.name);
                var $functions = addEl('p', $ingredient_info, 'functions');
                for (var i = 0; i < ingredient_data.functions.length; i++) {
                    var func_id = ingredient_data.functions[i];
                    if (SW.FUNC[func_id]) {
                        addEl('a', $functions, 'function neutral', fullyCapitalize(SW.FUNC[func_id].name), {
                            href: '/function/' + func_id
                        }).data('id', func_id);

                    }
                }
                $ingredient_info.appendTo('body');
                addEl('p', $ingredient_info, '', ingredient_data.description);
                $('.ingredient_infobox').remove();
                var left_offset = Math.min($(document).width() - 470, $(this).find('.infobox_anchor').offset().left);
                var top_offset = $(this).find('.infobox_anchor').offset().top + 10;
                $ingredient_info.addClass('ingredient_infobox').show().offset({ top: top_offset, left: left_offset});
            }
        }, this), SW.ING_BOX.TIMEOUT);
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

    $(document).on('click', function() {
        $('.ingredient_infobox').remove();
    });
}

function setupFunctionInfobox() {
    $(document).on('mouseenter', '.function', function(e) {
        var id = $(this).data('id');
        clearTimeout(SW.FUNC_BOX.DISMISS_TIMEOUT_ID);
        SW.FUNC_BOX.TIMEOUT_ID = setTimeout(function() {
            if (SW.FUNC[id]) {
                var func_data = SW.FUNC[id];
                var $func_info = addEl('div').on('click', function(e) {
                    e.stopPropagation();
                });
                addEl('span', $func_info, 'close_btn').on('click', function() {
                    $('.function_infobox').remove();
                });
                addEl('h2', $func_info, '', func_data.name);
                $func_info.appendTo('body');
                addEl('p', $func_info, '', func_data.description);
                $('.function_infobox').remove();

                var left_offset = Math.min($(document).width() - 470, e.pageX + 10);
                $func_info.addClass('function_infobox').show().offset({top: e.pageY + 10, left: left_offset});
            }
        }, SW.FUNC_BOX.TIMEOUT);
    }).on('mouseleave', '.function', function() {
        clearTimeout(SW.FUNC_BOX.TIMEOUT_ID);
        SW.FUNC_BOX.DISMISS_TIMEOUT_ID = setTimeout(function() {
            $('.function_infobox').remove();
        }, SW.FUNC_BOX.DISMISS_TIMEOUT);
    }).on('click', '.function', function(e) {
        e.stopPropagation();
        var id = $(this).data('id');
        location.href = '/function/' + id;
    });

    // Dismiss infobox after leaving it for a while
    $(document).on('mouseenter', '.function_infobox', function() {
        clearTimeout(SW.FUNC_BOX.DISMISS_TIMEOUT_ID);
    }).on('mouseleave', '.function_infobox', function() {
        SW.FUNC_BOX.DISMISS_TIMEOUT_ID = setTimeout(function() {
            $('.function_infobox').remove();
        }, SW.FUNC_BOX.DISMISS_TIMEOUT);
    });

    $(document).on('click', function() {
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

function setupBackToTop() {
    var $to_top = $('#back_to_top');
    $(window).on('scroll', function() {
        if ($(document).scrollTop() > SW.BACK_TO_TOP_THRESHOLD) {
            $to_top.fadeIn();
        } else {
            $to_top.fadeOut(200);
        }
    });
    $to_top.on('click', function(e) {
        e.preventDefault();
        $('body').animate({scrollTop: 0}, 100);
    });
}

function redirectToSelectedItem(provided_id) {
    var $nav_search = $('#nav_searchbar');
    var $search_select = $('#search_category_select');

    if (provided_id !== undefined) {
        var id = provided_id;
    } else {
        var id = $nav_search.data('id');
    }
    // Go to page if id is present
    if (id !== undefined && id !== '') {
        location.href = '/' + $search_select.val() + '/' + id;
    }
    // If autocomplete is showing results, use the first one
    else if ($('.search_container .ui-autocomplete').is(':visible')) {
        $('.search_container .ui-autocomplete .ui-menu-item:first-child').trigger('click');
        id = $nav_search.data('id');
        location.href = '/' + $search_select.val() + '/' + id;
    }
    // Last attempt: ask server if there's any match
    else {
        var query = $nav_search.val();
        postToAPI('/autocomplete', {
            type: $search_select.val(),
            query: query
        }, function(response) {
            if (response.results.length > 0) {
                var id = response.results[0].id;
                location.href = '/' + $search_select.val() + '/' + id;
            }
        });
    }
}

function setupNavSearchAutocomplete() {
    var $search_select = $('#search_category_select');
    var $nav_search = $('#nav_searchbar');

    enableAutocomplete($search_select.val(), $nav_search, '.search_container', SW.AUTOCOMPLETE_LIMIT.NAV_SEARCH, $('#nav_searchbar_not_found'));
    $nav_search.on('autocompleteselect', function(event, ui) {
        redirectToSelectedItem(ui.item.value);
    });

    $search_select.on('change', function() {
        enableAutocomplete($search_select.val(), $nav_search, '.search_container', SW.AUTOCOMPLETE_LIMIT.NAV_SEARCH, $('#nav_searchbar_not_found'));
        $nav_search.val('');
    });

    $('#nav_searchbar_btn').on('click', redirectToSelectedItem);
}

function handleEnterKey() {
    $(document).on('keyup', function(e) {
        // 13 is ENTER
        if (e.which === 13) {
            e.preventDefault();
            if ($('#nav_searchbar').is(':focus')) {
                $('#nav_searchbar_btn').trigger('click');
            } else if ($('#add_filter').is(':focus') && $('#add_filter').val() !== '') {
                $('#add_filter_btn').trigger('click');
            }
        }
    });
}

function getBrandsSuccess(response, callback) {
    for (var i = 0; i < response.results.length; i++) {
        var brand = response.results[i];
        SW.BRANDS[brand.id] = {
            name: brand.name
        };
    }
    callback && callback();
}

function loadSimilarProducts(response) {
    var num_shown = Math.min(response.results.length, SW.NUM_SIMILAR_PRODUCTS);
    for (var i = 0; i < num_shown; i++) {
        $('.products_list ul').append(productResultHTML(response.results[i]));
    }
}

function setNavsearchCategory(category) {
    if (category) {
        $('#search_category_select').val(category).trigger('change');
    }
}

$(document).on('ready', function() {
    setupIngredientInfobox();
    setupFunctionInfobox();
    setupBackToTop();
    setupNavSearchAutocomplete();
    handleEnterKey();

    checkLocalStorage();

    postToAPI('/function/all', {}, getFunctionsSuccess);

    $('.expand_subnav').on('mouseenter', function() {
        $('.sub_navbar').fadeIn(SW.CONFIG.SUBNAV_FADE_IN);
    }).on('mouseleave', function() {
        $('.sub_navbar').fadeOut(SW.CONFIG.SUBNAV_FADE_OUT);
    });
});