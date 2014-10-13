function loadFilterResults(response) {
    for (var i = 0; i < response.results.length; i++) {
        var ing = response.results[i];
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

        $('.ingredients_list ul').append($list_item);
    }
}


$(document).on('ready', function() {
    postToAPI('/ingredient/filter', {
        functions: [],
        page: 0
    }, loadFilterResults);

    $('.function_filter').on('change', function() {
        $('.ingredients_list ul').empty();

        var functions = [];
        $('.function_filter:checked').each(function() {
           functions.push($(this).data('id'));
        });

        postToAPI('/ingredient/filter', {
            functions: functions,
            page: 0
        }, loadFilterResults);
    });
});