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
    showMessage({type: 'error', message: message});
}

function showInfo(message) {
    showMessage({type: 'info', message: message});
}

function addFilter(type, filter_key, item) {
    var key = type + '_' + filter_key;
    var filters = JSON.parse(localStorage.getItem(key) || '[]');
    var id = item.id;
    for (var i = 0; i < filters.length; i++) {
        if (filters[i].id === id) {
            return;
        }
    }
    filters.push(item);
    localStorage.setItem(key, JSON.stringify(filters));
}

function getSavedFilters(type, key) {
    return JSON.parse(localStorage.getItem(type + '_' + key) || '[]');
}

function removeFilter(type, filter_key, id) {
    var key = type + '_' + filter_key;
    var filters = JSON.parse(localStorage.getItem(key) || '[]');
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

function getFeedbackEmail() {
    return localStorage.getItem('feedback_email') || '';
}

function setFeedbackEmail(email) {
    localStorage.setItem('feedback_email', email);
}

function getAskAgain(key) {
    return localStorage.getItem('ask_again_' + key);
}

function setAskAgain(key, value) {
    localStorage.setItem('ask_again_' + key, value);
}

// Get an array of checked ids for a filter type
function getSelectedFilters(filter_type) {
    var results = [];
    $('.' + filter_type + '_filters .filter_option.selected').each(function () {
        results.push($(this).data('id'));
    });

    return results;
}

// Get an array of checked ids for a filter type
function getFiltersByType(filter_type) {
    var results = [];
    $('.' + filter_type + '_filters .filter_option').each(function () {
        results.push($(this).data('id'));
    });

    return results;
}

function log() {
    if (SW.DEBUG) {
        console.log.apply(console, arguments);
    }
}

// From http://stackoverflow.com/questions/273789/is-there-a-version-of-javascripts-string-indexof-that-allows-for-regular-expr
String.prototype.regexIndexOf = function(regex, startpos) {
    var indexOf = this.substring(startpos || 0).search(regex);
    return (indexOf >= 0) ? (indexOf + (startpos || 0)) : indexOf;
}

function enableAutocomplete(type, $selector, append_to, limit, $no_result_el) {
    // If already intialized autocomplete on it, destroy prev instance and create a new one
    if ($selector.hasClass('ui-autocomplete-input')) {
        $selector.autocomplete('destroy');
    }
    $selector.autocomplete({
        appendTo: append_to,

        select: function (event, ui) {
            event.preventDefault();
            $selector.val(ui.item.label).data('id', ui.item.value);
            // To fix add filter auto complete
            enableAutocomplete(type, $selector, append_to, limit, $no_result_el);
        },

        // Default behavior is to replace input with mouseover value; we don't want that
        focus: function (event, ui) {
            event.preventDefault();
        },

        source: function (request, response) {
            var query = request.term;
            postToAPI('/autocomplete', {
                type: type,
                query: query
            }, function (api_response) {
                if (api_response.results.length > 0) {
                    var data = [];
                    var length = Math.min(api_response.results.length, limit || Number.MAX_VALUE);
                    for (var i = 0; i < length; i++) {
                        var item = api_response.results[i];
                        data.push({
                            label: type === 'function' ? fullyCapitalize(item.name) : item.name,
                            value: item.id
                        });
                    }
                    $no_result_el && $no_result_el.hide();
                    response(data);
                } else {
                    response([]);
                    $no_result_el && $no_result_el.show();
                }
            });
        }
    }).autocomplete('instance')._renderItem = function (ul, item) {
        var query = $selector.val().toLowerCase();

        var words = query.split(/[^a-zA-Z0-9]/);

        // Because "a   b".split(" ") returns ["a", "", "", "", "b"] ... sigh
        var nonemptyWords = []
        for (var i = 0; i < words.length; i++) {
            if (words[i].length > 0)
                nonemptyWords.push(words[i])
        }

        // Add a space to the front and end to avoid having a special case
        // for the first and last word during regex search (not displayed).
        // Also convert to lowercase to avoid dealing with case.
        var paddedItem = " " + item.label.toLowerCase() + " ";

        // Whether the character should be bolded.
        var flags = new Array(item.label.length);
        for (var i = 0; i < flags.length; i++)
            flags[i] = false;

        for (var i = 0; i < nonemptyWords.length; i++) {
            var word = nonemptyWords[i];
            var regex;
            if (i == nonemptyWords.length - 1) {
                // Prefix search.
                regex = "[^a-zA-Z0-9]" + word;
            } else {
                // Full word search.
                regex = "[^a-zA-Z0-9]" + word + "[^a-zA-Z0-9]";
            }

            var index = paddedItem.regexIndexOf(regex);
            while (index >= 0) {
                for (var j = 0; j < word.length; j++) {
                    // We should do (index + j + 1) because of the leading
                    // padded whitespace, but it is cancelled by the
                    // leading non-alphanumerical in the regex.
                    flags[index + j] = true;
                }
                index = paddedItem.regexIndexOf(regex, index + 1);
            }
        }

        // Get runs of true flags.
        var html = [];
        for (var i = 0; i < item.label.length; i++) {
            if (flags[i]) {
                var start = i;
                i++;
                while (flags[i]) {
                    i++;
                }
                html.push('<span class="autocomplete_found_part">' + item.label.slice(start, i) + '</span>');
            }
            html.push(item.label[i]);
        }
        html = html.join("");

        return $("<li>")
            .append(html)
            .appendTo(ul);
    };

    $selector.data('old_val', $selector.val());

    // Update data id on any input change
    $selector.off('propertychange keyup input paste').on('propertychange keyup input paste', function() {
        if ($(this).data('old_val') !== $(this).val()) {
            // Updated stored value
            $(this).data('old_val', $(this).val());
            $(this).data('id', '');

            // Don't show "No results found" when user deletes her query
            if ($(this).val() === '') {
                $no_result_el && $no_result_el.hide();
            }

        }
    });

    $selector.off('blur').on('blur', function() {
        $no_result_el.hide();
    });
}

// Show placeholder image if image is unavailable
function checkImage($element, url) {
    $('<img/>').attr('src', url)
        .load(function() {
            if (!this.complete || typeof this.naturalWidth == "undefined" || this.naturalWidth == 0) {
                $element.attr({
                    src: SW_PLACEHOLDER_URL,
                    alt: 'No Image Available'
                });
            } else {
                $element.attr('src', url);
            }
        })
        .error(function() {
            $element.attr({
                src: SW_PLACEHOLDER_URL,
                alt: 'No Image Available'
            });
        });
}

// Check if there's a new version that requires clearing local storage
function checkLocalStorage() {
    var version = localStorage.getItem('local_storage_ver');
    if (SW.LOCAL_STORAGE_SETTINGS.REQUIRE_CLEAR && (version === null || version < SW.LOCAL_STORAGE_SETTINGS.VERSION)) {
        localStorage.clear();
    }
    localStorage.setItem('local_storage_ver', SW.LOCAL_STORAGE_SETTINGS.VERSION);
}

function addEl(tag, parent, className, text, attr) {
    var $el = $('<' + tag + '/>');

    if (className) {
        $el.attr('class', className)
    };

    if (text) {
        $el.text(text)
    }

    if (attr) {
        $el.attr(attr);
    }

    if (parent) {
        parent.append($el);
    }

    return $el;
}

function addProductImage(product, $parent) {
    var $img = addEl('img', $parent, 'product_img', '', {
        src: product.image || SW_PLACEHOLDER_URL,
        alt: product.brand + ' ' + product.name
    });
    // check if image is ok
    if (product.image) {
        checkImage($img, product.image);
    }
}

function productResultHTML(product) {
    var $list_item = addEl('li', null, 'product');
    var $link = addEl('a', $list_item, '', '', { href: '/product/' + product.id });
    var $img_container = addEl('div', $link, 'product_img_container');
    addProductImage(product, $img_container);
    addEl('div', $link, 'product_brand', SW.BRANDS[product.brand].name);
    addEl('div', $link, 'product_name', product.name);
    addEl('div', $link, '', formatPrice(product));

    return $list_item;
}


function formatPrice(product) {
    if (product.price && product.size && product.size_unit) {
        return product.price + ' / ' + product.size + ' ' + product.size_unit;
    } else if (product.price) {
        return product.price;
    } else {
        return '';
    }
}
