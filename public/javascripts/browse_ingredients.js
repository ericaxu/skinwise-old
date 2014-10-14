function ingredientResultHTML(ing) {
    var $list_item = $('<li/>', { class: 'ingredient_item' });
    $list_item.append('<h2 class="name"><a href="/ingredient/' + ing.id + '">' + ing.name + '</a></h2>');
    var functions = $('<p/>', { class: 'functions' });

    for (var j = 0; j < ing.functions.length; j++) {
        functions.append('<span class="function neutral">' + ing.functions[j] + '</span>');
    }

    $list_item.append(functions);

    if (ing.description) {
        $list_item.append('<p class="ingredient_description">' + ing.description + '</p>');
    }

    return $list_item;
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
        $('.ingredients_list ul').append(ingredientResultHTML(response.results[i]));
    }
}

function getSelectedFunctions() {
    var functions = [];
    $('.function_filter:checked').each(function() {
        functions.push($(this).data('id'));
    });

    return functions;
}

function fetchNextPage() {
    if (!SW.ING_FETCH.LOADING) {

        SW.ING_FETCH.LOADING = true;
        SW.ING_FETCH.SPINNER = new Spinner(SW.SPINNER_CONFIG).spin(document.getElementById("loading_spinner"));

        postToAPI('/ingredient/filter', {
            functions: getSelectedFunctions(),
            page: SW.ING_FETCH.CUR_PAGE + 1
        }, function (response) {
            SW.ING_FETCH.SPINNER.stop();
            SW.ING_FETCH.LOADING = false;
            SW.ING_FETCH.CUR_PAGE += 1;
            loadFilterResults(response);
        });
    }
}

function refetch() {
    $('.ingredients_list ul').empty();

    SW.ING_FETCH.LOADING = true;
    SW.ING_FETCH.SPINNER = new Spinner(SW.SPINNER_CONFIG).spin(document.getElementById("loading_spinner"));

    postToAPI('/ingredient/filter', {
        functions: getSelectedFunctions(),
        page: 0
    }, function (response) {
        SW.ING_FETCH.SPINNER.stop();
        SW.ING_FETCH.LOADING = false;
        SW.ING_FETCH.CUR_PAGE = 0;
        loadFilterResults(response);
    });
}


$(document).on('ready', function() {
    var original_offset = $('.filter_area').offset().top;

    fetchNextPage();

    $('.function_filter').on('change', function() {
        refetch();
    });

    $(window).on('scroll', function() {
        // Check if we are at bottom of page
        if ($(window).scrollTop() + $(window).height() > $(document).height() - $('nav').height()) {
            fetchNextPage();
        }

        if ($(window).scrollTop() >= original_offset - $('nav').height()) {
            $('.filter_area').addClass('sticky');
        } else {
            $('.filter_area').removeClass('sticky');
        }
    });
});