function setupIngredientInfobox() {
    var $ingredient = $('.ingredient');

    $ingredient.on('mouseenter', function(e) {
        var id = $(this).data('id');
        clearTimeout(SW.ING_BOX.DISMISS_TIMEOUT_ID);
        clearTimeout(SW.ING_BOX.TIMEOUT_ID);
        SW.ING_BOX.TIMEOUT_ID = setTimeout(function() {
            if (SW.ING[id]) {
                var ingredient_data = SW.ING[id];
                var ingredient_info = $('<div/>').on('click', function(e) {
                    e.stopPropagation();
                });
                var close_button = $('<span/>', {class: 'close_btn'}).on('click', function() {
                    $('.ingredient_infobox').remove();
                })
                ingredient_info.append(close_button);
                ingredient_info.append($('<h2/>', {text: ingredient_data.name}));
                var functions = $('<p/>', {class: 'functions'});
                for (var i = 0; i < ingredient_data.functions.length; i++) {
                    var func_id = ingredient_data.functions[i];
                    if (SW.FUNC[func_id]) {
                        functions.append($('<a/>', {
                            class: 'function neutral',
                            text: fullyCapitalize(SW.FUNC[func_id].name),
                            href: '/function/' + func_id
                        }).data('id', func_id));
                    }
                }
                ingredient_info.append(functions);
                ingredient_info.appendTo('body');
                ingredient_info.append($('<p/>', {text: ingredient_data.description}));
                $('.ingredient_infobox').remove();
                var left_offset = Math.min($(document).width() - 470, e.pageX + 10);
                ingredient_info.addClass('ingredient_infobox').show().offset({ top: e.pageY + 10, left: left_offset });
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
                var func_info = $('<div/>').on('click', function(e) {
                    e.stopPropagation();
                });
                var close_button = $('<span/>', {class: 'close_btn'}).on('click', function() {
                    $('.function_infobox').remove();
                })
                func_info.append(close_button);
                func_info.append($('<h2/>', {text: func_data.name}));
                func_info.appendTo('body');
                func_info.append($('<p/>', {text: func_data.description}));
                $('.function_infobox').remove();

                var left_offset = Math.min($(document).width() - 470, e.pageX + 10);
                func_info.addClass('function_infobox').show().offset({ top: e.pageY + 10, left: left_offset });
            }
        }, SW.FUNC_BOX.TIMEOUT);
    }).on('mouseleave', '.function', function() {
        clearTimeout(SW.FUNC_BOX.TIMEOUT_ID);
        SW.FUNC_BOX.DISMISS_TIMEOUT_ID = setTimeout(function() {
            $('.function_infobox').remove();
        }, SW.FUNC_BOX.DISMISS_TIMEOUT);
    }).on('click', '.function', function(e) {
        e.stopPropagation();
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

function attachScrollHandler () {
    var $to_top = $('#back_to_top');
    $(window).on('scroll', function() {
        if ($(document).scrollTop() > SW.BACK_TO_TOP_THRESHOLD) {
            $to_top.fadeIn();
        } else {
            $to_top.fadeOut(200);
        }
    });
    $to_top.on('click', function (e) {
        e.preventDefault();
        $('body').animate({ scrollTop: 0 }, 100);
    });
}

function setupNavSearchAutocomplete() {
    var $search_select = $('#search_category_select');
    var $nav_search = $('#nav_searchbar');

    $search_select.val(getLastSearchedCatgory());

    enableAutocomplete($search_select.val(), $('#nav_searchbar'), '.search_container', SW.AUTOCOMPLETE_LIMIT.NAV_SEARCH, $('#nav_searchbar_not_found'));

    $search_select.on('change', function() {
        setLastSearchedCatgory($(this).val());
        enableAutocomplete($search_select.val(), $('#nav_searchbar'), '.search_container', SW.AUTOCOMPLETE_LIMIT.NAV_SEARCH, $('#nav_searchbar_not_found'));
        $('#nav_searchbar').val('');
    });

    $('#nav_searchbar_btn').on('click', function() {
        var id = $nav_search.data('id');
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
    });

    // Open autocomplete menu when focus on input
    $nav_search.on('focus', function() {
        $(this).autocomplete('search');
    });

    $(document).on('keyup', function(e) {
        // 13 is ENTER
        if (e.which === 13 && $nav_search.is(':focus')) {
            $('#nav_searchbar_btn').trigger('click');
        }
    });
}

$(document).ready(function() {
    setupIngredientInfobox();
    setupFunctionInfobox();
    attachScrollHandler();
    setupNavSearchAutocomplete();

    checkLocalStorage();

    postToAPI('/function/all', {}, getFunctionsSuccess);

    $('.profile_add_input').on('focus', function() {
        $(this).next().show();
    }).on('blur', function() {
        if ($(this).val() === '') {
            $(this).next().hide();
        }
    });

    $('.expand_subnav').on('mouseenter', function() {
        $('.sub_navbar').fadeIn(SW.CONFIG.SUBNAV_FADE_IN);
    }).on('mouseleave', function() {
        $('.sub_navbar').fadeOut(SW.CONFIG.SUBNAV_FADE_OUT);
    });
});