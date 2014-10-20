function fullyCapitalize(str) {
    return str.replace(/\w\S*/g, function (txt) {
        return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
    });
}

function isInteger(str) {
    return /^\+?(0|[1-9]\d*)$/.test(str);
}

function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function postToAPI(url, params, successCallback, errorCallback, message) {
    log('Post to ' + url, params);

    if (message) {
        var $message_box = $(SW.TEMPALTES.NOTICE({
            type: 'info',
            message: message
        }));
        $('.notice_container').append($message_box);
        $message_box.fadeIn(SW.CONFIG.NOTICE_FADE_IN);
    }
    $.ajax(SW.CONFIG.API_ROOT + url, {
        contentType: 'text/plain',
        type: 'POST',
        data: JSON.stringify(params),
        dataType: 'json',
        success: function (response, status, jqxhr) {

            log(response);

            if (message) {
                $message_box.fadeOut(SW.CONFIG.NOTICE_FADE_OUT);
            }

            _.forEach(response.messages, showMessage);

            if (response.code == "Ok") {
                successCallback && successCallback(response);
            } else if (errorCallback) {
                log("Error communicating with API: " + response.code);
                errorCallback(response);
            }
        },

        error: function (jqxhr, status, err) {
            console.error("Network error occurred when trying to post to " + url + ": " + status);

            if (message) {
                $message_box.fadeOut(SW.CONFIG.NOTICE_FADE_OUT);
            }

            errorCallback && errorCallback(response);
        }
    });
}

function showMessage(message) {
    var $notice_box = $(SW.TEMPALTES.NOTICE({
        type: message.type,
        message: message.message
    }));
    $('.notice_container').append($notice_box);
    $notice_box.fadeIn(SW.CONFIG.NOTICE_FADE_IN);
    setTimeout(function () {
        $notice_box.fadeOut(200);
    }, message.timeout || SW.CONFIG.DEFAULT_NOTICE_TIMEOUT);
}

function showError(message) {
    showMessage({ type: 'error', message: message });
}

function showInfo(message) {
    showMessage({ type: 'info', message: message });
}

function getRandomImage() {
    var images = [
        '/assets/images/11620108701.jpg',
        '/assets/images/rby-philosophy-microdelivery-triple-acid-brightening-peel-de-36384246.jpg',
        '/assets/images/43640.jpg',
        '/assets/images/COOLA_Tinted Matte_SPF_30_for_Face.jpg',
        '/assets/images/2_percent_BHA_liquid.png',
        '/assets/images/drbrandt_microdermabrasion_900x900.jpg'
    ]

    var chosen = _.random(images.length - 1);

    return '<img class="product_pic" src="' + images[chosen] + '" alt="Product image"/>';
}


function productResultHTML(product) {
    var $list_item = $('<li/>', { class: 'product' });
    var $link = $('<a/>', { href: '/product/' + product.id });
    $link.append(getRandomImage());
    $link.append($('<div/>', { class: 'product_brand', text: product.brand }));
    $link.append($('<div/>', { class: 'product_name', text: product.name }));

    $list_item.append($link);

    return $list_item;
}

function addProductFilter(filter_type, id, name) {
    addFilter('product_' + filter_type, {
        id: id,
        name: name
    });
}

function addIngredientFilter(filter_type, id, name) {
    addFilter('ingredient_' + filter_type, {
        id: id,
        name: name
    });
}

function removeProductFilter(filter_type, id) {
    removeFilter('product_' + filter_type, id);
}

function removeIngredientFilter(filter_type, id) {
    removeFilter('ingredient_' + filter_type, id);
}

function addFilter(key, item) {
    var filters = JSON.parse(localStorage.getItem(key) || '[]');
    log(filters);
    filters.push(item);
    localStorage.setItem(key, JSON.stringify(filters));
}

function getProductFilters(key) {
    return JSON.parse(localStorage.getItem('product_' + key) || '[]');
}


function getIngredientFilters(key) {
    return JSON.parse(localStorage.getItem('ingredient_' + key) || '[]');
}

function removeFilter(key, id) {
    var filters = JSON.parse(localStorage.getItem(key) || '[]');
    log(filters);
    if (!filters) return;
    for (var i = 0; i < filters.length; i++) {
        if (id == filters[i].id) {
            filters.splice(i, 1);
        }
    }
    localStorage.setItem(key, JSON.stringify(filters));
}

function getLastSearchedCatgory() {
    return localStorage.getItem('last_searched_category') || 'product';
}

function setLastSearchedCatgory(category) {
    localStorage.setItem('last_searched_category', category);
}

function getChebkexIds(filter_type) {
    var results = [];
    $('.' + filter_type + '_filters input[type="checkbox"]:checked').each(function () {
        results.push($(this).data('id'));
    });

    return results;
}

function getFilterHTML(filter, type) {
    var $option = $('<div/>', { class: 'filter_option' });
    $option.append($('<input/>', {
        type: 'checkbox',
        id: filter.name
    }).data('id', filter.id));
    $option.append($('<label/>', { for: filter.name }).text(filter.name));
    $option.append($('<span/>', { class: 'delete_btn' }).data('type', type));

    return $option;
}

function log() {
    if (SW.DEBUG) {
        console.log.apply(console, arguments);
    }
}

function enableAutocomplete(type, selector, append_to, limit) {
    log('enable autocomplete');
    if ($(selector).hasClass('ui-autocomplete-input')) {
        $(selector).autocomplete('destroy');
    }
    $(selector).autocomplete({
        appendTo: append_to,

        select: function (event, ui) {
            log('select');
            event.preventDefault();
            $(selector).val(ui.item.label).data('id', ui.item.value);
            // To fix add filter auto complete
            enableAutocomplete(type, selector, append_to, limit);
        },

        source: function (request, response) {
            var query = request.term;
            postToAPI('/autocomplete', {
                type: type,
                query: query
            }, function (api_response) {
                var data = [];
                var length = Math.min(api_response.results.length, limit || Number.MAX_VALUE)
                for (var i = 0; i < length; i++) {
                    var item = api_response.results[i];
                    data.push({
                        label: fullyCapitalize(item.name),
                        value: item.id
                    });
                }
                response(data);
            });
        }
    });
}