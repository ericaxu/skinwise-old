function extra_ingredient(ingredient) {
    return ingredient.id;
}

function refreshCompare() {
    var left_ingredients = (SW.PRODUCTS_FOR_COMPARE[0] && SW.PRODUCTS_FOR_COMPARE[0].ingredients) || [];
    var right_ingredients = (SW.PRODUCTS_FOR_COMPARE[1] && SW.PRODUCTS_FOR_COMPARE[1].ingredients) || [];
    var common_ingredients = findCommonIngredients(left_ingredients, right_ingredients);
    var common_ingredient_ids = common_ingredients.map(extra_ingredient);

    var $table = $('.compare_products tbody');
    $table.empty();

    var $image_row = addEl('tr', $table);
    var $name_row = addEl('tr', $table);
    var $brand_row = addEl('tr', $table);
    var $price_row = addEl('tr', $table);
    var $size_row = addEl('tr', $table);

    for (var i = 0; i < 2; i++) {
        var current = SW.PRODUCTS_FOR_COMPARE[i];
        var $image_td = addEl('td', $image_row, 'short');
        addProductImage(current, $image_td);
        var $name_td = addEl('td', $name_row, 'short');
        addEl('span', $name_td, 'emphasis', 'Name: ');
        addEl('a', $name_td, '', current.name, {href: '/product/' + current.id});
        var $brand_td = addEl('td', $brand_row, 'short');
        addEl('span', $brand_td, 'emphasis', 'Brand: ');
        addEl('a', $brand_td, '', SW.BRANDS[current.brand].name, {href: '/brand/' + current.brand});
        var $price_td = addEl('td', $price_row, 'short');
        addEl('span', $price_td, 'emphasis', 'Price: ');
        addEl('span', $price_td, '', current.price);
        var $size_td = addEl('td', $size_row, 'short');
        addEl('span', $size_td, 'emphasis', 'Size: ');
        addEl('span', $size_td, '', current.size + ' ' + current.size_unit);
    }

    if (common_ingredients.length > 0) {
        var $common_ingredients_tr = addEl('tr', $table);
        var $common_ingredients_td = addEl('td', $common_ingredients_tr, '', '', {colspan: "2"});
        addEl('span', $common_ingredients_td, 'emphasis', 'Common ingredients: ');
        for (var i = 0; i < common_ingredients.length - 1; i++) {
            var ingredient = common_ingredients[i];
            addEl('a', $common_ingredients_td, 'ingredient', ingredient.name, {
                href: '/ingredient/' + ingredient.id
            }).data('id', ingredient.id);
            $common_ingredients_td.append(', ');
        }
        var ingredient = common_ingredients[common_ingredients.length - 1];
        addEl('a', $common_ingredients_td, 'ingredient', ingredient.name, {
            href: '/ingredient/' + ingredient.id
        }).data('id', ingredient.id);
    }

    var $ingredient_row = addEl('tr', $table);

    if (common_ingredients.length !== left_ingredients.length || common_ingredients.length !== right_ingredients.length) {
        for (var i = 0; i < 2; i++) {
            var $ingredient_td = addEl('td', $ingredient_row);
            var unique_ingredients = SW.PRODUCTS_FOR_COMPARE[i].ingredients.filter(function(ingredient) {
                return common_ingredient_ids.indexOf(ingredient.id) === -1;
            });

            if (unique_ingredients.length > 0) {
                addEl('span', $ingredient_td, 'emphasis', 'Unique ingredients: ');
                for (var j = 0; j < unique_ingredients.length; j++) {
                    var ingredient = unique_ingredients[j];
                    addEl('a', $ingredient_td, 'ingredient', ingredient.name, {
                        href: '/ingredient/' + ingredient.id
                    }).data('id', ingredient.id);
                    $ingredient_td.append(', ');
                }
                var ingredient = unique_ingredients[unique_ingredients.length - 1];
                addEl('a', $ingredient_td, 'ingredient', ingredient.name, {
                    href: '/ingredient/' + ingredient.id
                }).data('id', ingredient.id);
            }
        }
    }

    setupIngredientInfobox();
}

function findCommonIngredients(left, right) {
    var left_ids = left.map(extra_ingredient);
    var right_ids = right.map(extra_ingredient);

    var common = [];

    for (var i = 0; i < left_ids.length; i++) {
        var id = left_ids[i];
        if (right_ids.indexOf(id) !== -1) {
            common.push(left[i]);
        }
    }

    return common;
}

$(document).on('ready', function() {
    postToAPI('/brand/all', {}, getBrandsSuccess);
    enableAutocomplete('product', $('.compare_input_left'), '.compare_products_container', SW.AUTOCOMPLETE_LIMIT.NAV_SEARCH);
    enableAutocomplete('product', $('.compare_input_right'), '.compare_products_container', SW.AUTOCOMPLETE_LIMIT.NAV_SEARCH);
    $('.compare_input_left').on('autocompleteselect', function(event, ui) {
        postToAPI('/product/byid', {
            id: ui.item.value
        }, function(response) {
            SW.PRODUCTS_FOR_COMPARE[0] = response.results[0];
            postToAPI('/product/ingredientinfo', {
                id: ui.item.value
            }, function(response) {
                SW.PRODUCTS_FOR_COMPARE[0].ingredients = response.results;
                getIngredientInfoSuccess(response);
                refreshCompare();
            });
        });
    });
    $('.compare_input_right').on('autocompleteselect', function(event, ui) {
        postToAPI('/product/byid', {
            id: ui.item.value
        }, function(response) {
            SW.PRODUCTS_FOR_COMPARE[1] = response.results[0];
            postToAPI('/product/ingredientinfo', {
                id: ui.item.value
            }, function(response) {
                getIngredientInfoSuccess(response);
                SW.PRODUCTS_FOR_COMPARE[1].ingredients = response.results;
                refreshCompare();
            });
        });
    });
});