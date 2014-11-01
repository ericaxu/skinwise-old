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
function getFilterHTML(filter_obj, filter_key) {
    var $name = $('<span/>', { class: 'filter_option_text', text: filter_obj.name });
    var $option = $('<div/>', {class: 'filter_option'})
        .append($name).append(' (' + filter_obj.count + ')').data('id', filter_obj.id);
    $option.append($('<span/>', {class: 'delete_btn'})
        .on('click', function(e) {
            e.stopPropagation();
            var action = 'delete ' + filter_key + ' filter "' + filter_obj.name + '"';
            var delete_callback = function() {
                removeFilter(SW.BROWSE_TYPE, filter_key, filter_obj.id);
                $option.remove();
                if ($option.hasClass('selected')) {
                    refetch(SW.BROWSE_TYPE);
                }
            };
            confirmAction(action, delete_callback, 'delete_filter');
        }));

    return $option;
}

function loadFilterResults(response) {
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
        if (SW.BROWSE_TYPE === 'ingredient') {
            $('.ingredients_list ul').append(ingredientResultHTML(response.results[i]));
        } else if (SW.BROWSE_TYPE === 'product') {
            $('.products_list ul').append(productResultHTML(response.results[i]));
        }
    }

    SW.ING_FETCH.LOADED_COUNT += response.results.length;

    if (SW.ING_FETCH.LOADED_COUNT >= SW.ING_FETCH.RESULT_COUNT) {
        if (response.count === 0) {
            $('.end_of_results').text('No results.').show();
        } else {
            $('.end_of_results').text('No more results.').show();
        }
    }
}

function fetchProducts(page, callback) {
    var ingredients = getSelectedFilters('ingredient');
    if (SW.CUR_INGREDIENT && (ingredients.indexOf(SW.CUR_INGREDIENT) !== -1 || ingredients.length === 0)) {
        ingredients.push(SW.CUR_INGREDIENT);
    }

    postToAPI('/product/filter', {
        types: getSelectedFilters('type'),
        brands: SW.CUR_BRAND ? [SW.CUR_BRAND] : getSelectedFilters('brand'),
        neg_brands: getSelectedFilters('neg_brand'),
        ingredients: ingredients,
        neg_ingredients: getSelectedFilters('neg_ingredient'),
        page: page
    }, callback);
}

function fetchIngredients(page, callback) {
    postToAPI('/ingredient/filter', {
        functions: SW.CUR_FUNCTION ? [SW.CUR_FUNCTION] : getSelectedFilters('function'),
        page: page
    }, callback);
}

function fetchNextPage() {
    if (!SW.ING_FETCH.LOADING) {
        $('#loading_spinner').show();
        SW.ING_FETCH.LOADING = true;

        var fetch_callback = function(response) {
            $('#loading_spinner').hide();
            SW.ING_FETCH.LOADING = false;
            SW.ING_FETCH.CUR_PAGE += 1;
            SW.ING_FETCH.RESULT_COUNT = response.count;
            loadFilterResults(response);
        };

        if (SW.BROWSE_TYPE === 'ingredient') {
            fetchIngredients(SW.ING_FETCH.CUR_PAGE + 1, fetch_callback);
        } else if (SW.BROWSE_TYPE === 'product') {
            fetchProducts(SW.ING_FETCH.CUR_PAGE + 1, fetch_callback);
        }
    }
}

function refetch() {
    $('.end_of_results').hide();
    $('.result_summary').text('Fetching results...');

    $('#loading_spinner').show();

    var refetch_callback = function(response) {
        $('#loading_spinner').hide();
        SW.ING_FETCH.LOADING = false;
        SW.ING_FETCH.CUR_PAGE = 0;
        SW.ING_FETCH.RESULT_COUNT = response.count;
        loadFilterResults(response);
    };

    SW.ING_FETCH.LOADED_COUNT = 0;
    SW.ING_FETCH.LOADING = true;

    if (SW.BROWSE_TYPE === 'product') {
        $('.products_list ul').empty();
        fetchProducts(0, refetch_callback);
    } else if (SW.BROWSE_TYPE === 'ingredient') {
        $('.ingredients_list ul').empty();
        fetchIngredients(0, refetch_callback);
    }
}

function loadFilters() {
    var filter_keys = SW.FILTER_TYPES[SW.BROWSE_TYPE] || [];
    for (var i = 0; i < filter_keys.length; i++) {
        var filter_key = filter_keys[i];
        var saved_filters = getSavedFilters(SW.BROWSE_TYPE, filter_key);

        var $filters = $('.' + filter_key + '_filters');
        $filters.empty();
        for (var j = 0; j < saved_filters.length; j++) {
            var filter = saved_filters[j];
            $filters.append(getFilterHTML(filter, filter_key));
        }
    }
}

function getBrandsSuccess(response) {
    for (var i = 0; i < response.results.length; i++) {
        var brand = response.results[i];
        SW.BRANDS[brand.id] = {
            name: brand.name
        };
    }
}

function handleAddFilter() {
    var $add_filter = $('#add_filter');

    $('#add_filter_btn').on('click', function() {
        cleanupErrors();

        var id = $add_filter.data('id');
        var name = $add_filter.val();
        var filter_key = $(this).data('filterKey');

        if (id === undefined || id === '') {
            showAddFilterError('We can\'t recognize this filter :(');
            return;
        }

        // Check if this filter already exists
        var found = false;
        $('.' + filter_key + '_filters .filter_option').each(function () {
            if ($(this).data('id') === id) {
                var filter_label = $(this).find('.filter_option_text').text();
                if (filter_label === name) {
                    showAddFilterError('Already added this filter.');
                    found = true;
                } else {
                    showAddFilterError(filter_label + ' is the same thing as ' + name + ' and it\'s already added.');
                    found = true;
                }
            }
        });

        if (found) { return; }

        switch (filter_key) {
            case 'brand':
            case 'neg_brand':
                var url = '/brand/byid';
                break;
            case 'ingredient':
            case 'neg_ingredient':
                var url = '/ingredient/byid';
                break;
            case 'type':
                var url = '/producttype/byid';
                break;
            default:
                showError('Unrecognized filter key ' + filter_key);
        }

        postToAPI(url, { id: id }, function(response) {
            var new_filter = {
                id: id,
                name: name,
                count: response.results[0].product_count
            };

            addFilter(SW.BROWSE_TYPE, filter_key, new_filter);

            var $filters = $('.' + filter_key + '_filters');
            $filters.append(getFilterHTML(new_filter, filter_key));
            $add_filter.val('');
            $('.popup').hide();
        });

    });
}

function handleBrowseScroll() {
    var nav_height = $('nav').height();
    $(window).on('scroll', function() {
        // Check if we are at bottom of page
        if ($(window).scrollTop() + $(window).height() + SW.REFETCH_DISTANCE_THRESHOLD > $(document).height() - nav_height &&
            SW.ING_FETCH.LOADED_COUNT < SW.ING_FETCH.RESULT_COUNT) {
            fetchNextPage();
        }

        if (SW.BROWSE_TYPE === 'product') {
            var list_height = $('.products_list').height();
        } else if (SW.BROWSE_TYPE === 'ingredient') {
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
}

function setupAddFilterPopup() {
    var $add_filter = $('#add_filter');
    $('.open_add_filter_popup').on('click', function() {
        // Reset
        cleanupErrors();
        $add_filter.val('');

        var type = $(this).data('type');
        $('#add_filter_btn').data({
            type: type,
            filterKey: $(this).data('filterKey')
        });
        enableAutocomplete(type, $add_filter, '#add_filter_form .inputs', SW.AUTOCOMPLETE_LIMIT.ADD_FILTER, $('#add_filter_not_found'));
        $add_filter.on('focus', function() {
            $(this).autocomplete('search');
        });
        $('#add_filter_type').text(type);
        $('.add_filter.popup').show();
        $add_filter.focus();
    });
}

function initBrowse() {

    postToAPI('/brand/all', {}, getBrandsSuccess);

    $(document).on('ready', function() {
        new Spinner(SW.SPINNER_CONFIG).spin(document.getElementById("loading_spinner"));

        loadFilters();
        fetchNextPage();
        handleAddFilter();
        handleBrowseScroll();
        setupAddFilterPopup();

        $(document).on('click', '.filter_option', function() {
            $(this).toggleClass('selected');
            refetch();
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

    });
}