function ingredientResultHTML(ing) {
    var $list_item = addEl('li', null, 'ingredient_item');
    var $header = addEl('h2', $list_item, 'name');
    addEl('a', $header, '', ing.display_name, {href: '/ingredient/' + ing.id});
    var $functions = addEl('p', $list_item, 'functions');

    for (var j = 0; j < ing.functions.length; j++) {
        var id = ing.functions[j];
        if (SW.FUNC[id]) {
            addEl('a', $functions, 'function neutral', fullyCapitalize(SW.FUNC[id].name), {
                href: '/function/' + id
            }).data('id', id);
        }
    }

    if (ing.description) {
        addEl('p', $list_item, 'ingredient_description', ing.description);
    }

    return $list_item;
}

// Generate the HTML for each filter item, given filter obj and type
function getFilterHTML(filter_obj, filter_key) {
    var $option = addEl('div', null, 'filter_option', '', {
        id: filter_key + '_' + filter_obj.id + '_filter_option'
    }).data('id', filter_obj.id);
    addEl('span', $option, 'filter_option_text', filter_obj.name);
    if (filter_key !== 'benefit' || SW.BROWSE_TYPE === 'ingredient') {
        $option.append(' (' + filter_obj.count + ')');
    }
    if (filter_obj.selected) {
        $option.addClass('selected');
    }
    addEl('span', $option, 'delete_btn').on('click', function(e) {
        e.stopPropagation();
        var action = 'delete ' + filter_key + ' filter "' + filter_obj.name + '"';
        var delete_callback = function() {
            removeFilter(SW.BROWSE_TYPE, filter_key, filter_obj.id);
            $option.remove();
            if ($option.hasClass('selected')) {
                refetch(SW.BROWSE_TYPE);
            }
            setupEmptyFilterBlankSlate();
        };
        confirmAction(action, delete_callback, 'delete_filter');
    });

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

function fetchProducts(page, callback, query) {
    var ingredients = getSelectedFilters('ingredient');
    if (SW.CUR_INGREDIENT && (ingredients.indexOf(SW.CUR_INGREDIENT) === -1 || ingredients.length === 0)) {
        ingredients.push(SW.CUR_INGREDIENT);
    }

    var properties = {};

    var price_filter = $('#price_filter').slider('values');
    if (!$('#price_filter').parent().hasClass('disabled')) {
        properties['price'] = {
            min: price_filter[0] * 100,
            max: price_filter[1] * 100
        };
    }

    var price_per_size_filter = $('#pricepersize_filter').slider('values');
    if (!$('#pricepersize_filter').parent().hasClass('disabled')) {
        properties['pricepersize'] = {
            min: price_per_size_filter[0] * 100 / SW.CONVERSION.ML_IN_OZ,
            max: price_per_size_filter[1] * 100 / SW.CONVERSION.ML_IN_OZ
        };
    }

    var spf_filter = $('#sunscreen_spf_filter').slider('values');
    if (!$('#sunscreen_spf_filter').parent().hasClass('disabled')) {
        properties['sunscreen.spf'] = {
            min: spf_filter[0],
            max: spf_filter[1]
        };
    }


    var query = {
        types: SW.CUR_TYPE ? [SW.CUR_TYPE] : getSelectedFilters('type'),
        brands: SW.CUR_BRAND ? [SW.CUR_BRAND] : getSelectedFilters('brand'),
        neg_brands: getSelectedFilters('neg_brand'),
        ingredients: ingredients,
        neg_ingredients: getSelectedFilters('neg_ingredient'),
        benefits: getSelectedFilters('benefit'),
        number_properties: properties,
        page: page
    };

    postToAPI('/product/filter', query, function(response) {
        callback(response);
        if (!SW.CUR_INGREDIENT && !SW.CUR_TYPE && !SW.CUR_BRAND) {
            changeHash(query);
        }
    });
}

function fetchIngredients(page, callback) {
    var query = {
        functions: SW.CUR_FUNCTION ? [SW.CUR_FUNCTION] : getSelectedFilters('function'),
        benefits: SW.CUR_BENEFIT ? [SW.CUR_BENEFIT] : getSelectedFilters('benefit'),
        page: page
    };
    postToAPI('/ingredient/filter', query, function(response) {
        callback(response);
        if (!SW.CUR_FUNCTION) {
            changeHash(query);
        }
    });
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

function fetchFilterInfo(filter_key, id, callback) {
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
            var url = '/type/byid';
            break;
        case 'function':
            var url = '/function/byid';
            break;
        case 'benefit':
            var url = '/benefit/byid';
            break;
        default:
            showError('Unrecognized filter key ' + filter_key);
    }

    postToAPI(url, {id: id}, callback);
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

        var result = checkIfFilterAlreadyExists(filter_key, id, name);

        if (result.found) {
            showAddFilterError(result.error);
            return;
        }

        fetchFilterInfo(filter_key, id, function(response) {
            var new_filter = {
                id: id,
                name: name,
                count: SW.FILTER_TYPES.ingredient.indexOf(filter_key) !== -1 ? response.results[0].ingredient_count : response.results[0].product_count
            };

            addFilter(SW.BROWSE_TYPE, filter_key, new_filter);

            var $filters = $('.' + filter_key + '_filters');
            $filters.find('.filter_blank_slate').remove();
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

function checkIfFilterAlreadyExists(filter_key, id, name) {
    var result = {
        found: false,
        error: ''
    };

    $('.' + filter_key + '_filters .filter_option').each(function() {
        if ($(this).data('id') === id) {
            var filter_label = $(this).find('.filter_option_text').text();
            if (filter_label === name) {
                result.error = 'Already added this filter.';
                result.found = true;
            } else {
                result.error = filter_label + ' is the same thing as ' + name + ' and it\'s already added.';
                result.found = true;
            }
        }
    });

    return result;
}

function setupAddFilterPopup() {
    var $add_filter = $('#add_filter');
    $(document).on('click', '.open_add_filter_popup', function() {
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

function showExtraFiltersFromUrl() {
    if (location.hash.length > 0) {
        var reverse_mapping = {};
        var query = {};

        for (var filter_type in SW.FILTER_ABBR_MAPPING) {
            if (SW.FILTER_ABBR_MAPPING.hasOwnProperty(filter_type)) {
                var abbr = SW.FILTER_ABBR_MAPPING[filter_type];
                reverse_mapping[abbr] = filter_type;

                // Default empty filter types to empty array
                query[filter_type] = [];
            }
        }

        var hash = location.hash.slice(1, location.hash.length);
        var filters_by_type = hash.split('&');

        for (var i = 0; i < filters_by_type.length; i++) {
            var parts = filters_by_type[i].split('=');
            var filter_name = reverse_mapping[parts[0]];
            query[filter_name] = parts[1].split(',');
        }

        for (var filter_key in query) {
            if (query.hasOwnProperty(filter_key)) {
                var filters = query[filter_key];

                for (var i = 0; i < filters.length; i++) {
                    var value = filters[i];
                    showExtraFilter(filter_key, value, i);
                }
            }
        }
    }
}

function showExtraFilter(filter_key, value, index) {
    if (SW.ADVANCED_PRODUCT_FILTERS.indexOf(filter_key) !== -1) {
        toggleAdvancedFilters(true);
    }
    if (SW.FILTER_PARSING_MAPPING[filter_key] === 'range') {
        var escaped_filter_key = escapeForSelector(filter_key);
        $('#' + escaped_filter_key + '_filter').slider('values', index, parseInt(value)).trigger('change');
    } else if (SW.FILTER_PARSING_MAPPING[filter_key] === 'array') {
        var escaped_filter_key = escapeForSelector(filter_key.slice(0, filter_key.length - 1));
        var result = $('#' + escaped_filter_key + '_' + value + '_filter_option');
        if (result.length > 0) {
            result.addClass('selected');
        } else {
            filter_key = SW.ARRAY_TO_FILTER_KEY[filter_key];
            fetchFilterInfo(filter_key, value, function(response) {
                var new_filter = {
                    id: value,
                    name: response.results[0].name,
                    count:  ['function', 'benefit'].indexOf(filter_key) !== -1 ? response.results[0].ingredient_count : response.results[0].product_count,
                    selected: true
                };

                var $filters = $('.' + filter_key + '_filters');
                $filters.find('.filter_blank_slate').remove();
                $filters.append(getFilterHTML(new_filter, filter_key));
            });
        }
    }
}

function changeHash(query) {
    var hash = '';
    var need_divide = false;

    for (var filter_type in SW.FILTER_ABBR_MAPPING) {
        if (query[filter_type] || (query.number_properties && query.number_properties[filter_type])) {
            if (SW.FILTER_PARSING_MAPPING[filter_type] === 'array' && query[filter_type].length > 0) {
                if (need_divide) {
                    hash += '&';
                }
                need_divide = true;

                hash += (SW.FILTER_ABBR_MAPPING[filter_type] + '=');
                for (var i = 0; i < query[filter_type].length - 1; i++) {
                    hash += query[filter_type][i] + ',';
                }
                hash += query[filter_type][query[filter_type].length - 1];
            } else if (SW.FILTER_PARSING_MAPPING[filter_type] === 'range' && query.number_properties[filter_type]) {
                if (need_divide) {
                    hash += '&';
                }
                need_divide = true;

                var min = query.number_properties[filter_type].min * SW.FILTER_RANGE_CONVERSION[filter_type];
                var max = query.number_properties[filter_type].max * SW.FILTER_RANGE_CONVERSION[filter_type];
                hash += (SW.FILTER_ABBR_MAPPING[filter_type] + '=' + Math.round(min) + ',' +  Math.round(max));
            }
        }
    }

    location.hash = hash;
}

function onPriceSliderChange(event, ui) {
    if (ui) {
        var values = ui.values;
    } else {
        var values = $('#price_filter').slider('values');
    }
    $('#price_label').text('$' + values[0] + ' - $' + values[1]);
    $('#price_filter').parent().removeClass('disabled');
}

function onPricePerSizeSliderChange(event, ui) {
    if (ui) {
        var values = ui.values;
    } else {
        var values = $('#pricepersize_filter').slider('values');
    }
    $('#price_per_size_label').text('$' + values[0] + ' - $' + values[1]);
    $('#pricepersize_filter').parent().removeClass('disabled');
}

function onSpfSliderChange(event, ui) {
    if (ui) {
        var values = ui.values;
    } else {
        var values = $('#sunscreen_spf_filter').slider('values');
    }
    $('#sunscreen_spf_label').text('SPF ' + values[0] + ' - ' + values[1]);
    $('#sunscreen_spf_filter').parent().removeClass('disabled');
}

function setupEmptyFilterBlankSlate() {
    var filter_keys = SW.FILTER_TYPES[SW.BROWSE_TYPE] || [];
    for (var i = 0; i < filter_keys.length; i++) {
        var filter_key = filter_keys[i];
        var $filters = $('.' + filter_key + '_filters');
        if ($filters.html() === '') {
            var $add_filter_button = $filters.parent().find('.open_add_filter_popup');
            var autocomplete_type = $add_filter_button.data('type');
            var filter_key = $add_filter_button.data('filterKey');
            addEl('div', $filters, 'filter_blank_slate open_add_filter_popup', 'Add a filter').data({
                type: autocomplete_type,
                filterKey: filter_key
            });
        }
    }
}

function toggleAdvancedFilters(show) {
    show = (show === undefined) ? false : show;
    var $advanced_filters = $('.advanced_filter_container');
    if ($advanced_filters.is(':visible') && !show) {
        $advanced_filters.hide();
        $('.toggle_advanced_filters').text('Show Advanced Filters');
    } else {
        $advanced_filters.show();
        $('.toggle_advanced_filters').text('Hide Advanced Filters');
    }
}

function setupFilterSlides() {
    $('#price_filter').slider({
        range: true,
        min: SW.SLIDER_RANGE.PRICE_MIN,
        max: SW.SLIDER_RANGE.PRICE_MAX,
        values: [SW.SLIDER_RANGE.PRICE_MIN, SW.SLIDER_RANGE.PRICE_MAX],
        slide: onPriceSliderChange,
        stop: function(event, ui) {
            onPriceSliderChange(event, ui);
            refetch();
        }
    }).on('change', onPriceSliderChange);

    $('#pricepersize_filter').slider({
        range: true,
        min: SW.SLIDER_RANGE.PRICE_PER_OZ_MIN,
        max: SW.SLIDER_RANGE.PRICE_PER_OZ_MAX,
        values: [SW.SLIDER_RANGE.PRICE_PER_OZ_MIN, SW.SLIDER_RANGE.PRICE_PER_OZ_MAX],
        slide: onPricePerSizeSliderChange,
        stop: function(event, ui) {
            onPricePerSizeSliderChange(event, ui);
            refetch();
        }
    }).on('change', onPricePerSizeSliderChange);

    $('#sunscreen_spf_filter').slider({
        range: true,
        min: SW.SLIDER_RANGE.SPF_MIN,
        max: SW.SLIDER_RANGE.SPF_MAX,
        values: [SW.SLIDER_RANGE.SPF_MIN, SW.SLIDER_RANGE.SPF_MAX],
        slide: onSpfSliderChange,
        stop: function(event, ui) {
            onSpfSliderChange(event, ui);
            refetch();
        }
    }).on('change', onSpfSliderChange);
}

function initBrowse(type) {
    $(document).on('ready', function() {
        new Spinner(SW.SPINNER_CONFIG).spin(document.getElementById('loading_spinner'));

        setupFilterSlides();

        loadFilters();
        showExtraFiltersFromUrl();

        postToAPI('/brand/all', {}, function(response) {
            getBrandsSuccess(response, fetchNextPage);
        });
        handleAddFilter();
        handleBrowseScroll();
        setupAddFilterPopup();

        $(document).on('click', '.filter_option', function() {
            $(this).toggleClass('selected');
            refetch();
        });

        if (type) {
            $('#search_category_select').val(type).trigger('change');
        }

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

        $('.clear_slider').on('click', function(e) {
            e.preventDefault();
            var $filter_container = $(this).parent().parent();
            var $slider = $filter_container.find('.slider_filter');
            var min = $slider.slider('option', 'min');
            var max = $slider.slider('option', 'max');
            $slider.slider('values', 0, min).slider('values', 1, max).trigger('change');
            $filter_container.find('.slider_label').text('No filter applied');
            $filter_container.addClass('disabled');
            refetch();
        });

        setupEmptyFilterBlankSlate();


        $('.toggle_advanced_filters').on('click', function(e) {
            e.preventDefault();
            toggleAdvancedFilters();
        })
    });
}