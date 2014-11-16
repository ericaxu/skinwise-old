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
        if (current) {
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
            var price = current.properties.price ? current.properties.price.text_value : 'Unknown';
            addEl('span', $price_td, '', price);
            var $size_td = addEl('td', $size_row, 'short');
            addEl('span', $size_td, 'emphasis', 'Size: ');
            var size = current.properties.size ? current.properties.size.number_value + ' ' + current.properties.size.text_value : 'Unknown';
            addEl('span', $size_td, '', size);
        } else {
            var $image_td = addEl('td', $image_row, 'short');
            var $name_td = addEl('td', $name_row, 'short');
            var $brand_td = addEl('td', $brand_row, 'short');
            var $price_td = addEl('td', $price_row, 'short');
            var $size_td = addEl('td', $size_row, 'short');
        }
    }

    if (common_ingredients.length > 0) {
        var $common_ingredients_tr = addEl('tr', $table);
        var $common_ingredients_td = addEl('td', $common_ingredients_tr, '', '', {colspan: "2"});
        addEl('span', $common_ingredients_td, 'emphasis', 'Common ingredients: ');
        for (var i = 0; i < common_ingredients.length - 1; i++) {
            $common_ingredients_td.append(ingredientLinkHTML(common_ingredients[i]));
            $common_ingredients_td.append(', ');
        }
        $common_ingredients_td.append(ingredientLinkHTML(common_ingredients.pop()));
    }

    var $ingredient_row = addEl('tr', $table);

    if (common_ingredients.length !== left_ingredients.length || common_ingredients.length !== right_ingredients.length) {
        for (var i = 0; i < 2; i++) {
            var $ingredient_td = addEl('td', $ingredient_row);
            var ingredient_list = (i === 0) ? left_ingredients : right_ingredients;
            if (ingredient_list.length > 0) {
                var unique_ingredients = ingredient_list.filter(function(ingredient) {
                    return common_ingredient_ids.indexOf(ingredient.id) === -1;
                });

                if (unique_ingredients.length > 0) {
                    addEl('span', $ingredient_td, 'emphasis', 'Unique ingredients: ');
                    for (var j = 0; j < unique_ingredients.length; j++) {
                        $ingredient_td.append(ingredientLinkHTML(unique_ingredients[j]));
                        $ingredient_td.append(', ');
                    }
                    $ingredient_td.append(ingredientLinkHTML(unique_ingredients.pop()));
                }

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

function updateHash() {
    var hash = '';

    var left_id = SW.PRODUCTS_FOR_COMPARE[0] && SW.PRODUCTS_FOR_COMPARE[0].id;
    var right_id = SW.PRODUCTS_FOR_COMPARE[1] && SW.PRODUCTS_FOR_COMPARE[1].id;

    if (left_id) {
        hash += 'l=' + left_id;
        if (right_id) {
            hash += ';'
        }
    }
    if (right_id) {
        hash += 'r=' + right_id;
    }

    location.hash = hash;
}

function loadHash() {
    if (location.hash.length > 0) {
        var hash = location.hash.slice(1, location.hash.length);
        var products = hash.split(';');

        var left_id = products[0].split('=')[1];
        fetchProductForComparison(left_id, 0);

        if (products.length > 1) {
            var right_id = products[1].split('=')[1];
            fetchProductForComparison(right_id, 1);
        }
    }
}

function fetchProductForComparison(id, index) {
    postToAPI('/product/byid', {
        id: id
    }, function(response) {
        SW.PRODUCTS_FOR_COMPARE[index] = response.results[0];
        postToAPI('/product/ingredientinfo', {
            id: id
        }, function(response) {
            SW.PRODUCTS_FOR_COMPARE[index].ingredients = response.results;
            getIngredientInfoSuccess(response);
            updateHash();
            refreshCompare();
        });
    });
}

$(document).on('ready', function() {
    postToAPI('/brand/all', {}, function(response) {
        getBrandsSuccess(response, loadHash);
    });
    enableAutocomplete('product', $('.compare_input_left'), '.compare_products_container', SW.AUTOCOMPLETE_LIMIT.NAV_SEARCH);
    enableAutocomplete('product', $('.compare_input_right'), '.compare_products_container', SW.AUTOCOMPLETE_LIMIT.NAV_SEARCH);
    $('.compare_input_left').on('autocompleteselect', function(event, ui) {
        fetchProductForComparison(ui.item.value, 0);
    });
    $('.compare_input_right').on('autocompleteselect', function(event, ui) {
        fetchProductForComparison(ui.item.value, 1);
    });
});