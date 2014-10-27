function ingredientResultHTML(ing) {
    var $list_item = $('<li/>', {class: 'ingredient_item'});
    $list_item.append('<h2 class="name"><a href="/ingredient/' + ing.id + '">' + ing.name + '</a></h2>');
    var functions = $('<p/>', {class: 'functions'});

    for (var j = 0; j < ing.functions.length; j++) {
        var id = ing.functions[j];
        if (SW.FUNC[id]) {
            var name = fullyCapitalize(SW.FUNC[id].name);
            functions.append($('<a/>', {
                class: 'function neutral',
                href: '/function/' + id,
                text: fullyCapitalize(SW.FUNC[id].name)
            }).data('id', id));
        }
    }

    $list_item.append(functions);

    if (ing.description) {
        $list_item.append('<p class="ingredient_description">' + ing.description + '</p>');
    }

    return $list_item;
}

// Generate the HTML for each filter item, given filter obj and type
function getFilterHTML(filter_obj, filter_key, type) {
    var $option = $('<div/>', { class: 'filter_option' })
        .text(filter_obj.name).data('id', filter_obj.id);
    $option.append($('<span/>', { class: 'delete_btn' })
        .on('click', function(e) {
            e.stopPropagation();
            confirmAction('delete ' + $(this).data('type') + ' filter "' + $(this).parent().text() + '"', $.proxy(function () {
                removeFilter(type, filter_key, filter_obj.id);
                $option.remove();
                if ($option.hasClass('selected')) {
                    refetch(type);
                }
            }, this));
        }));

    return $option;
}

function loadFilterResults(response, type) {
    switch (response.count) {
        case 0:
            $('.result_summary').text('No results found.');
            break;
        case 1:
            $('.result_summary').text('Found 1 result.');
            break;
        default:
            $('.result_summary').text('Found ' + formatNumber(response.count) + ' results.');
    }

    for (var i = 0; i < response.results.length; i++) {
        if (type === 'ingredient') {
            $('.ingredients_list ul').append(ingredientResultHTML(response.results[i]));
        } else if (type === 'product') {
            $('.products_list ul').append(productResultHTML(response.results[i]));
        }
    }

    SW.ING_FETCH.LOADED_COUNT += response.results.length;

    if (SW.ING_FETCH.LOADED_COUNT >= SW.ING_FETCH.RESULT_COUNT) {
        $('.end_of_results').show();
    }
}

function fetchProducts(page, callback) {
    postToAPI('/product/filter', {
        types: getSelectedFilters('type'),
        brands: SW.CUR_BRAND ? [ SW.CUR_BRAND ] : getSelectedFilters('brand'),
        ingredients: SW.CUR_INGREDIENT ? [ SW.CUR_INGREDIENT ] : getSelectedFilters('ingredient'),
        neg_ingredients: [1],
        page: page
    }, callback);
}

function fetchIngredients(page, callback) {
    postToAPI('/ingredient/filter', {
        functions: SW.CUR_FUNCTION ? [ SW.CUR_FUNCTION ] : getSelectedFilters('function'),
        page: page
    }, callback);
}

function fetchNextPage(type) {
    if (!SW.ING_FETCH.LOADING) {
        $('#loading_spinner').show();
        SW.ING_FETCH.LOADING = true;

        var fetch_callback = function (response) {
            $('#loading_spinner').hide();
            SW.ING_FETCH.LOADING = false;
            SW.ING_FETCH.CUR_PAGE += 1;
            SW.ING_FETCH.RESULT_COUNT = response.count;
            loadFilterResults(response, type);
        };

        if (type === 'ingredient') {
            fetchIngredients(SW.ING_FETCH.CUR_PAGE + 1, fetch_callback);
        } else if (type === 'product') {
            fetchProducts(SW.ING_FETCH.CUR_PAGE + 1, fetch_callback);
        }
    }
}

function refetch(type) {
    $('.end_of_results').hide();
    $('.result_summary').text('Fetching results...');

    $('#loading_spinner').show();

    var refetch_callback = function (response) {
        $('#loading_spinner').hide();
        SW.ING_FETCH.LOADING = false;
        SW.ING_FETCH.CUR_PAGE = 0;
        SW.ING_FETCH.RESULT_COUNT = response.count;
        loadFilterResults(response, type);
    };

    SW.ING_FETCH.LOADED_COUNT = 0;
    SW.ING_FETCH.LOADING = true;

    if (type === 'product') {
        $('.products_list ul').empty();
        fetchProducts(0, refetch_callback);
    } else if (type === 'ingredient') {
        $('.ingredients_list ul').empty();
        fetchIngredients(0, refetch_callback);
    }
}

function loadFilters(type) {
    var filter_keys = SW.FILTER_TYPES[type] || [];
    for (var i = 0; i < filter_keys.length; i++) {
        var filter_key = filter_keys[i];
        var saved_filters = getSavedFilters(type, filter_key);

        var $filters = $('.' + filter_key + '_filters');
        $filters.empty();
        for (var j = 0; j < saved_filters.length; j++) {
            var filter = saved_filters[j];
            $filters.append(getFilterHTML(filter, filter_key, type));
        }
    }
}

function initBrowse(type) {
    var $add_filter = $('#add_filter');

    $(document).on('ready', function () {
        new Spinner(SW.SPINNER_CONFIG).spin(document.getElementById("loading_spinner"));
        var nav_height = $('nav').height();

        $('.open_add_filter_popup').on('click', function () {
            $('#add_filter_btn').data('type', $(this).data('type'));
            enableAutocomplete($(this).data('type'), $('#add_filter'), '#add_filter_form .inputs', SW.AUTOCOMPLETE_LIMIT.ADD_FILTER, $('#add_filter_not_found'));
            $('.add_filter.popup').show();
            $('#add_filter').focus();
        });

        $('#add_filter_btn').on('click', function () {
            cleanupErrors();

            var id = $add_filter.data('id');
            var name = $add_filter.val();

            if (id === undefined || id === '') {
                showAddFilterError('We can\'t recognize this filter :(');
                return;
            }

            log(id, name);
            var filter_key = $(this).data('type');
            var new_filter =  {
                id: id,
                name: name
            };
            addFilter(type, filter_key, new_filter);

            var $filters = $('.' + filter_key + '_filters');
            $filters.append(getFilterHTML(new_filter, filter_key, type));

            // reset
            $add_filter.val('');
            $('.popup').hide();
        });

        loadFilters(type);
        fetchNextPage(type);

        $(document).on('click', '.filter_option', function () {
            $(this).toggleClass('selected');
            refetch(type);
        });

        $('.filter_toggle_link').on('click', function() {
            if ($(this).hasClass('open')) {
                $(this).removeClass('open');
                $(this).find('.chevron').removeClass('top').addClass('bottom');
                $('.filter_container').hide();
            } else {
                $(this).addClass('open');
                $(this).find('.chevron').removeClass('bottom').addClass('top');
                $('.filter_container').show();
            }
        });

        $(window).on('scroll', function () {
            // Check if we are at bottom of page
            if ($(window).scrollTop() + $(window).height() + SW.REFETCH_DISTANCE_THRESHOLD > $(document).height() - nav_height &&
                SW.ING_FETCH.LOADED_COUNT < SW.ING_FETCH.RESULT_COUNT) {
                fetchNextPage(type);
            }

            if (type === 'product') {
                var list_height = $('.products_list').height();
            } else if (type === 'ingredient') {
                var list_height = $('.ingredients_list').height();
            }

            //if (list_height + $('#logo').height() + nav_height > $(window).height()) {
            //    if ($(window).scrollTop() >= 100 - nav_height) {
            //        $('.filter_area').addClass('sticky');
            //    } else {
            //        $('.filter_area').removeClass('sticky');
            //    }
            //}
        });
    });
}