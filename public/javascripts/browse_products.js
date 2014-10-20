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
        $('.products_list ul').append(productResultHTML(response.results[i]));
    }

    SW.ING_FETCH.LOADED_COUNT += response.results.length;

    if (SW.ING_FETCH.LOADED_COUNT >= SW.ING_FETCH.RESULT_COUNT) {
        $('.end_of_results').show();
    }
}

function fetchNextPage() {
    if (!SW.ING_FETCH.LOADING) {
        $('#loading_spinner').show();
        SW.ING_FETCH.LOADING = true;

        postToAPI('/product/filter', {
            types: getChebkexIds('type'),
            brands: getChebkexIds('brand'),
            ingredients: getChebkexIds('ingredient'),
            page: SW.ING_FETCH.CUR_PAGE + 1
        }, function (response) {
            $('#loading_spinner').hide();
            SW.ING_FETCH.LOADING = false;
            SW.ING_FETCH.CUR_PAGE += 1;
            SW.ING_FETCH.RESULT_COUNT = response.count;
            loadFilterResults(response);
        });
    }
}

function refetch() {
    $('.products_list ul').empty();
    $('.end_of_results').hide();
    $('.result_summary').text('Fetching results...');

    $('#loading_spinner').show();

    SW.ING_FETCH.LOADED_COUNT = 0;
    SW.ING_FETCH.LOADING = true;

    postToAPI('/product/filter', {
        types: getChebkexIds('type'),
        brands: getChebkexIds('brand'),
        ingredients: getChebkexIds('ingredient'),
        page: 0
    }, function (response) {
        $('#loading_spinner').hide();
        SW.ING_FETCH.LOADING = false;
        SW.ING_FETCH.CUR_PAGE = 0;
        SW.ING_FETCH.RESULT_COUNT = response.count;
        loadFilterResults(response);
    });
}

function loadFilters() {
    var filter_types = ['type', 'brand', 'ingredient'];
    for (var i = 0; i < filter_types.length; i++) {
        var filter_type = filter_types[i];
        var saved_filters = getProductFilters(filter_type);

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
            loadFilters();
        }, this));
    });
}

$(document).on('ready', function () {
    new Spinner(SW.SPINNER_CONFIG).spin(document.getElementById("loading_spinner"));
    var original_offset = $('.filter_area').offset().top;

    $('.open_add_filter_popup').on('click', function () {
        $('#add_filter_btn').data('type', $(this).data('type'));
        enableAutocomplete($(this).data('type'), '#add_filter', '#add_filter_form .inputs', SW.AUTOCOMPLETE_LIMIT.ADD_FILTER);
        $('.add_filter.popup').show();
    });

    $('#add_filter_btn').on('click', function () {
        // TODO: check if the id is valid
        var id = $('#add_filter').data('id');
        var name = $('#add_filter').val();
        addProductFilter($(this).data('type'), id, name);
        loadFilters();

        // reset
        $('#add_filter').val('');
        $('.popup').hide();
    });

    setupDeleteButtons();
    loadFilters();

    fetchNextPage();

    $(document).on('change', '.filter_option input[type="checkbox"]', refetch);

    $(window).on('scroll', function () {
        // Check if we are at bottom of page
        if ($(window).scrollTop() + $(window).height() > $(document).height() - $('nav').height() &&
            SW.ING_FETCH.LOADED_COUNT < SW.ING_FETCH.RESULT_COUNT) {
            fetchNextPage();
        }


        if ($('.products_list').height() + $('#logo').height() + $('nav').height() > $(window).height()) {
            if ($(window).scrollTop() >= original_offset - $('nav').height()) {
                $('.filter_area').addClass('sticky');
            } else {
                $('.filter_area').removeClass('sticky');
            }
        }
    });
});