function setupIngredientInfobox() {
    var $ingredient = $('.ingredient');

    $ingredient.on('click', function(e) {
        var ingredient_name = $(this).text()
        $('.ingredient_infobox').remove();
        if (SW.ING[ingredient_name]) {

            var ingredient_data = SW.ING[ingredient_name];
            var ingredient_info = $('<div/>', { class: 'ingredient_infobox' }).on('click', function(e) {
                e.stopPropagation();
            });
            var close_button = $('<span/>', { class: 'close_btn' }).on('click', function() {
                $('.ingredient_infobox').remove();
            })
            ingredient_info.append(close_button);
            ingredient_info.append($('<h2/>', { text: ingredient_name }));
            var functions = $('<p/>', { class: 'functions' });
            for (var i = 0; i < ingredient_data.functions.length; i++) {
                functions.append($('<span/>', {
                    class: 'function neutral',
                    text: ingredient_data.functions[i]
                }));
            }
            ingredient_info.append(functions);
            ingredient_info.appendTo('body');
            ingredient_info.append($('<p/>', { text: ingredient_data.short_desc }));
            ingredient_info.append('<p><a class="explicit" href="/ingredient/' + ingredient_data.id +  '">More details</a></p>');
            ingredient_info.show().offset({ top: e.pageY, left: e.pageX });
        }
    });

    $(document).on('click', function() {
        $('.ingredient_infobox').remove();
    });

    $ingredient.on('click', function(e) {
        e.stopPropagation();
    });
}

function getIngredientInfoSuccess(response) {
    for (var i = 0; i < response.ingredient_info.length; i++) {
        var ingredient = response.ingredient_info[i];
        SW.ING[ingredient.name] = {
            id: ingredient.id,
            description: ingredient.description,
            functions: ingredient.functions
        };
    }
}

$(document).ready(function() {
    setupIngredientInfobox();

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