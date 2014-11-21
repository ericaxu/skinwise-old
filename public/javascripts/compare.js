function extract_ingredient_id(ingredient) {
    return ingredient.id;
}

function refreshCompare() {
    var left_ingredients = (SW.PRODUCTS_FOR_COMPARE[0] && SW.PRODUCTS_FOR_COMPARE[0].ingredients) || [];
    var right_ingredients = (SW.PRODUCTS_FOR_COMPARE[1] && SW.PRODUCTS_FOR_COMPARE[1].ingredients) || [];
    var common_ingredients = findCommonIngredients(left_ingredients, right_ingredients);
    var common_ingredient_ids = common_ingredients.map(extract_ingredient_id);

    var $table = $('.compare_products tbody');
    $table.empty();

    if (SW.PRODUCTS_FOR_COMPARE.length > 0) {
        var $image_row = addEl('tr', $table);
        var $name_row = addEl('tr', $table);
        var $brand_row = addEl('tr', $table);
        var $price_row = addEl('tr', $table);
        var $size_row = addEl('tr', $table);
        var $price_per_size_row = addEl('tr', $table);

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
                var $price_per_size_td = addEl('td', $price_per_size_row, 'short');
                if (current.properties.pricepersize && current.properties.size && current.properties.size.text_value === 'ml') {
                    addEl('span', $price_per_size_td, 'emphasis', 'Price/oz: ');
                    var price_per_size = '$' + (current.properties.pricepersize.number_value * SW.CONVERSION.ML_IN_OZ / 100).toFixed(2);
                    addEl('span', $price_per_size_td, '', price_per_size);
                } else if (current.properties.pricepersize && current.properties.size) {
                    addEl('span', $price_per_size_td, 'emphasis', 'Price/' + current.properties.size.text_value + ': ');
                    var price_per_size = '$' + (current.properties.pricepersize.number_value / 100).toFixed(2);
                    addEl('span', $price_per_size_td, '', price_per_size);
                } else {
                    addEl('span', $price_per_size_td, 'emphasis', 'Price/oz.: ');
                    addEl('span', $price_per_size_td, '', 'Unknown');
                }
            } else {
                var $image_td = addEl('td', $image_row, 'short');
                var $name_td = addEl('td', $name_row, 'short');
                var $brand_td = addEl('td', $brand_row, 'short');
                var $price_td = addEl('td', $price_row, 'short');
                var $size_td = addEl('td', $size_row, 'short');
                var $price_per_size_td = addEl('td', $price_per_size_row, 'short');
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
            $common_ingredients_td.append(ingredientLinkHTML(common_ingredients[common_ingredients.length - 1]));
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
}

function findCommonIngredients(left, right) {
    var left_ids = left.map(extract_ingredient_id);
    var right_ids = right.map(extract_ingredient_id);

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
            hash += '&'
        }
    }
    if (right_id) {
        hash += 'r=' + right_id;
    }

    if (hash !== '') {
        hash = '#' + hash;
    }

    if (hash !== location.hash) {
        history.pushState(null, null, location.pathname + hash);
    }
}

function handleHashChange() {
    // Reset before loading hash
    $('.compare_input_right, .compare_input_left').val('');
    $('.compare_products table').empty();
    SW.PRODUCTS_FOR_COMPARE = [];

    if (location.hash.length > 0) {
        var hash = location.hash.slice(1, location.hash.length);
        var products = hash.split('&');

        for (var i = 0; i < products.length; i++) {
            var side = products[i].split('=')[0];
            var id = products[i].split('=')[1];

            if (side === 'l') {
                fetchProductForComparison(id, 0, '.compare_input_left');
            } else if (side === 'r') {
                fetchProductForComparison(id, 1, '.compare_input_right');
            }
        }
    } else {
        refreshCompare();
    }
}

function fetchProductForComparison(id, index, input_name_selector) {
    postToAPI('/product/byid', {
        id: id
    }, function(response) {
        var product = response.results[0];
        if (input_name_selector !== undefined) {
            var brand = SW.BRANDS[product.brand].name;
            $(input_name_selector).val(brand + ' - ' + product.name);
        }
        SW.PRODUCTS_FOR_COMPARE[index] = product;
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
        getBrandsSuccess(response, handleHashChange);
    });
    enableAutocomplete('product', $('.compare_input_left'), '.compare_products_container', SW.AUTOCOMPLETE_LIMIT.NAV_SEARCH);
    enableAutocomplete('product', $('.compare_input_right'), '.compare_products_container', SW.AUTOCOMPLETE_LIMIT.NAV_SEARCH);
    $('.compare_input_left').on('autocompleteselect', function(event, ui) {
        fetchProductForComparison(ui.item.value, 0);
    });
    $('.compare_input_right').on('autocompleteselect', function(event, ui) {
        fetchProductForComparison(ui.item.value, 1);
    });

    window.onpopstate = handleHashChange;
});