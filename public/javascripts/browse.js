function ingredientResultHTML(ing) {
    var $list_item = $('<li/>', {class: 'ingredient_item'});
    $list_item.append('<h2 class="name"><a href="/ingredient/' + ing.id + '">' + ing.name + '</a></h2>');
    var functions = $('<p/>', {class: 'functions'});

    for (var j = 0; j < ing.functions.length; j++) {
        var id = ing.functions[j];
        if (SW.FUNC[id]) {
            var name = fullyCapitalize(SW.FUNC[id].name);
            functions.append($('<span/>', {class: 'function neutral'}).text(fullyCapitalize(SW.FUNC[id].name)).data('id', id));
        }
    }

    $list_item.append(functions);

    if (ing.description) {
        $list_item.append('<p class="ingredient_description">' + ing.description + '</p>');
    }

    return $list_item;
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
            postToAPI('/ingredient/filter', {
                functions: getChebkexIds('function'),
                page: SW.ING_FETCH.CUR_PAGE + 1
            }, fetch_callback);
        } else if (type === 'product') {
            postToAPI('/product/filter', {
                types: getChebkexIds('type'),
                brands: getChebkexIds('brand'),
                ingredients: getChebkexIds('ingredient'),
                page: SW.ING_FETCH.CUR_PAGE + 1
            }, fetch_callback);
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
        postToAPI('/product/filter', {
            types: getChebkexIds('type'),
            brands: getChebkexIds('brand'),
            ingredients: getChebkexIds('ingredient'),
            page: 0
        }, refetch_callback);
    } else if (type === 'ingredient') {
        $('.ingredients_list ul').empty();
        postToAPI('/ingredient/filter', {
            functions: getChebkexIds('function'),
            page: 0
        }, refetch_callback);
    }
}

function loadFilters(type) {
    var filter_types = SW.FILTER_TYPES[type] || [];
    for (var i = 0; i < filter_types.length; i++) {
        var filter_type = filter_types[i];
        var saved_filters = getSavedFilters(type, filter_type);

        var $filters = $('.' + filter_type + '_filters');
        $filters.empty();
        for (var j = 0; j < saved_filters.length; j++) {
            var filter = saved_filters[j];
            $filters.append(getFilterHTML(filter, filter_type));
        }
    }
}

function setupDeleteButtons() {
    $(document).on('mouseenter', '.filter_option', function () {
        $(this).find('.delete_btn').css('visibility', 'visible');
    }).on('mouseleave', '.filter_option', function () {
        $(this).find('.delete_btn').css('visibility', 'hidden');
    });

    $(document).on('click', '.delete_btn', function () {
        confirmAction('delete filter "' + $(this).parent().find('label').text() + '"', $.proxy(function () {
            removeProductFilter($(this).data('type'), $(this).parent().find('input[type="checkbox"]').data('id'));
            loadFilters(type);
        }, this));
    });
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
        });

        $('#add_filter_btn').on('click', function () {
            cleanupErrors();

            var id = $add_filter.data('id');
            var name = $add_filter.val();

            if (id === undefined || id === '') {
                showAddFilterError('We can\'t recognize this filter :(');
                return;
            }

            addProductFilter($(this).data('type'), id, name);
            loadFilters(type);

            // reset
            $add_filter.val('');
            $('.popup').hide();
        });

        setupDeleteButtons();
        loadFilters(type);
        fetchNextPage(type);

        $(document).on('change', '.filter_option input[type="checkbox"]', function() {
            refetch(type);
        });

        $(window).on('scroll', function () {
            // Check if we are at bottom of page
            if ($(window).scrollTop() + $(window).height() > $(document).height() - nav_height &&
                SW.ING_FETCH.LOADED_COUNT < SW.ING_FETCH.RESULT_COUNT) {
                fetchNextPage(type);
            }

            if (type === 'product') {
                var list_height = $('.products_list').height();
            } else if (type === 'ingredient') {
                var list_height = $('.ingredients_list').height();
            }

            if (list_height + $('#logo').height() + nav_height > $(window).height()) {
                if ($(window).scrollTop() >= 100 - nav_height) {
                    $('.filter_area').addClass('sticky');
                } else {
                    $('.filter_area').removeClass('sticky');
                }
            }
        });
    });
}