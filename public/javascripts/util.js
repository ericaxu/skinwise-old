function isInteger(str) {
    return /^\+?(0|[1-9]\d*)$/.test(str);
}

function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function postToAPI(url, params, successCallback, errorCallback, message) {
    console.log('Post to ' + url, params);

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
        success: function(response, status, jqxhr) {

            console.log(response);

            if (message) {
                $message_box.fadeOut(SW.CONFIG.NOTICE_FADE_OUT);
            }

            _.forEach(response.messages, showMessage);

            if (response.code == "Ok") {
                successCallback && successCallback(response);
            } else if (errorCallback) {
                console.log("Error communicating with API: " + response.code);
                errorCallback(response);
            }
        },

        error: function(jqxhr, status, err) {
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
    setTimeout(function() {
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
    console.log(filters);
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
    if (!filters) return;
    for (var i = 0; i < filters.length; i++) {
        if (id == filters[i].id) {
            filters.splice(i, 1);
        }
    }
    localStorage.setItem(key, filters);
}