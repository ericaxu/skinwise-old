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

function getSelectedBrands() {
    var brands = [];
    $('.brand_filter:checked').each(function() {
        brands.push($(this).data('id'));
    });

    return brands;
}

function getSelectedIngredients() {
    var ingredients = [];
    $('.ingredient_filter:checked').each(function() {
        ingredients.push($(this).data('id'));
    });

    return ingredients;
}

function fetchNextPage() {
    if (!SW.ING_FETCH.LOADING) {
        console.log('fetchNextPage');

        $('#loading_spinner').show();
        SW.ING_FETCH.LOADING = true;

        postToAPI('/product/filter', {
            brands: getSelectedBrands(),
            ingredients: getSelectedIngredients(),
            page: SW.ING_FETCH.CUR_PAGE + 1
        }, function (response) {
            $('#loading_spinner').hide();
            SW.ING_FETCH.LOADING = false;
            SW.ING_FETCH.CUR_PAGE += 1;
            SW.ING_FETCH.RESULT_COUNT = response.count;
            loadFilterResults(response);

            console.log("Loaded: " + SW.ING_FETCH.LOADED_COUNT);
            console.log("Total: " + SW.ING_FETCH.RESULT_COUNT);
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

    console.log('refetch');

    postToAPI('/product/filter', {
        brands: getSelectedBrands(),
        ingredients: getSelectedIngredients(),
        page: 0
    }, function (response) {
        $('#loading_spinner').hide();
        SW.ING_FETCH.LOADING = false;
        SW.ING_FETCH.CUR_PAGE = 0;
        SW.ING_FETCH.RESULT_COUNT = response.count;
        loadFilterResults(response);


        console.log("Loaded: " + SW.ING_FETCH.LOADED_COUNT);
        console.log("Total: " + SW.ING_FETCH.RESULT_COUNT);
    });
}


$(document).on('ready', function() {
    new Spinner(SW.SPINNER_CONFIG).spin(document.getElementById("loading_spinner"));

    var original_offset = $('.filter_area').offset().top;

    fetchNextPage();

    $('.brand_filter, .ingredient_filter').on('change', function() {
        refetch();
    });

    $(window).on('scroll', function() {
        // Check if we are at bottom of page
        if ($(window).scrollTop() + $(window).height() > $(document).height() - $('nav').height() &&
            SW.ING_FETCH.LOADED_COUNT <= SW.ING_FETCH.RESULT_COUNT) {
            fetchNextPage();
        }

        if ($(window).scrollTop() >= original_offset - $('nav').height()) {
            $('.filter_area').addClass('sticky');
        } else {
            $('.filter_area').removeClass('sticky');
        }
    });
});