var SW = {};
SW.CONFIG = {};

SW.CONFIG.API_ROOT = '/api';
SW.CONFIG.PERMISSION_DELIMITER = ', ';

/* Animation configs */
SW.CONFIG.NOTICE_FADE_IN = 500;
SW.CONFIG.NOTICE_FADE_OUT = 500;
SW.CONFIG.SUBNAV_FADE_IN = 100;
SW.CONFIG.SUBNAV_FADE_OUT = 100;
SW.CONFIG.SEARCHBAR_EXPAND_TIMEOUT = 400;
SW.CONFIG.DEFAULT_NOTICE_TIMEOUT = 5000;

SW.CONFIG.CONTAINING_PRODUCT_NUM = 3;

/* Timeout objects */
SW.SEARCHBAR_EXPAND_TIMEOUT = null;

/* Templates */
SW.TEMPALTES = {};
SW.TEMPALTES.NOTICE = _.template('<div class="<%= type %>"><span class="close_btn"></span><p class="message"><%= message %></p></div>')

/* Product ingredient data */
SW.ING = {};

SW.FUNC = {};

SW.SPINNER_CONFIG = {
    lines: 11,
    width: 4,
    length: 14,
    radius: 21,
    color: "#222",
    trail: 42,
    shadow: false,
    hwaccel: false,
    top: "100",
    left: "auto"
};

/* Ingredient filter fetching */
SW.ING_FETCH = {
    LOADING: false,
    CUR_PAGE: -1,
    LOADED_COUNT: 0,
    RESULT_COUNT: 0
};

SW.INFOBOX = {
    TIMEOUT_ID: null,
    TIMEOUT: 300,
    DISMISS_TIMEOUT_ID: null,
    DISMISS_TIMEOUT: 600
}

SW.DEBUG = true;