function refreshCompare() {
    var $table = $('.compare_products');
    $table.empty();

    var $image_row = addEl('tr', $table);
    var $name_row = addEl('tr', $table);
    var $brand_row = addEl('tr', $table);
    var $price_row = addEl('tr', $table);
    var $size_row = addEl('tr', $table);

    for (var i = 0; i < 2; i++) {
        var current = SW.PRODUCTS_FOR_COMPARE[i];
        var $image_td = addEl('td', $image_row);
        //addEl('img', $image_td, '', '', {
        //    alt: current.name,
        //    src: current.image
        //});
        addProductImage(current, $image_td);
        var $name_td = addEl('td', $name_row, '');
        addEl('a', $name_td, '', current.name, { href: '/product/' + current.id });
        var $brand_td = addEl('td', $brand_row);
        addEl('a', $brand_td, '', SW.BRANDS[current.brand].name, { href: '/brand/' + current.brand });
        addEl('td', $price_row, '', current.price);
        addEl('td', $size_row, '', current.size + ' ' + current.size_unit);
    }
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
            refreshCompare();
        });
    });
    $('.compare_input_right').on('autocompleteselect', function(event, ui) {
        postToAPI('/product/byid', {
            id: ui.item.value
        }, function(response) {
            SW.PRODUCTS_FOR_COMPARE[1] = response.results[0];
            refreshCompare();
        });
    });
});