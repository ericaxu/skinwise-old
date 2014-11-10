var SW = {};
SW.CONFIG = {};

SW.CONFIG.API_ROOT = '/api';
SW.CONFIG.PERMISSION_DELIMITER = ', ';

/* Animation configs */
SW.CONFIG.NOTICE_FADE_IN = 500;
SW.CONFIG.NOTICE_FADE_OUT = 500;
SW.CONFIG.SUBNAV_FADE_IN = 100;
SW.CONFIG.SUBNAV_FADE_OUT = 100;
SW.CONFIG.DEFAULT_NOTICE_TIMEOUT = 5000;

SW.CONFIG.CONTAINING_PRODUCT_NUM = 3;

SW.REFETCH_DISTANCE_THRESHOLD = 500;

/* Templates */
SW.TEMPALTES = {};
SW.TEMPALTES.NOTICE = _.template('<div class="<%= type %>"><span class="close_btn"></span><p class="message"><%= message %></p></div>')

/* Product ingredient data */
SW.ING = {};
SW.FUNC = {};
SW.BRANDS = {};

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

SW.ING_BOX = {
    TIMEOUT_ID: null,
    TIMEOUT: 300,
    DISMISS_TIMEOUT_ID: null,
    DISMISS_TIMEOUT: 600
}

SW.FUNC_BOX = {
    TIMEOUT_ID: null,
    TIMEOUT: 300,
    DISMISS_TIMEOUT_ID: null,
    DISMISS_TIMEOUT: 600
}

SW.DEBUG = true;

SW.AUTOCOMPLETE_LIMIT = {
    NAV_SEARCH: 10,
    ADD_FILTER: 4,
    EDITOR: 5
}

SW.FEEDBACK = {
    question: _.template('<%= user %> needs your help!'),
    content: _.template('<%= user %> spotted a mistake that we made. How embarrassing!'),
    missing: _.template('<%= user %> points out we should add something to our database.'),
    bug: _.template('Bug report from our volunteer QA <%= user %>'),
    suggestion: _.template('<%= user %> thinks we should build this.'),
    other: _.template('<%= user %> has feedback for us.')
}

SW.FILTER_TYPES = {
    product: ['type', 'brand', 'neg_brand', 'ingredient', 'neg_ingredient'],
    ingredient: ['function']
}

SW.BACK_TO_TOP_THRESHOLD = 300;

SW.LOCAL_STORAGE_SETTINGS = {
    VERSION: 3,
    REQUIRE_CLEAR: true
};

SW.PREFERENCES = {
    INGREDIENT: [
        'ingredient_working',
        'ingredient_not_working',
        'ingredient_bad_reaction'
    ]
};

SW.FILTER_ABBR_MAPPING = {
    types: 't',
    brands: 'b',
    neg_brands: 'nb',
    ingredients: 'i',
    neg_ingredients: 'ni'
};

SW.PRICE_FILTER_RANGE = {
    MIN: 0,
    MAX: 201,
    INFINITY: Math.pow(2, 53)
};

SW.EDITOR = {
    product: {
        key: 'name',
        fields: [
            {
                name: 'id',
                label: 'ID',
                type: 'short_text'
            },
            {
                name: 'name',
                label: 'Name',
                type: 'short_text'
            },
            {
                name: 'brand',
                label: 'Brand',
                type: 'short_text'
            },
            {
                name: 'line',
                label: 'Line',
                type: 'short_text'
            },
            {
                name: 'size',
                label: 'Size',
                type: 'short_text'
            },
            {
                name: 'size_unit',
                label: 'Size unit',
                type: 'short_text'
            },
            {
                name: 'image',
                label: 'Image URL',
                type: 'short_text'
            },
            {
                name: 'popularity',
                label: 'Popularity',
                type: 'short_text'
            },
            {
                name: 'description',
                label: 'Description',
                type: 'long_text'
            }
        ]
    },

    ingredient: {
        key: 'name',
        fields: [
            {
                name: 'id',
                label: 'ID',
                type: 'short_text'
            },
            {
                name: 'name',
                label: 'Name',
                type: 'short_text'
            },
            {
                name: 'cas_number',
                label: 'CAS number',
                type: 'short_text'
            },
            {
                name: 'popularity',
                label: 'Popularity',
                type: 'short_text'
            },
            {
                name: 'description',
                label: 'Description',
                type: 'long_text'
            },
            {
                name: 'functions',
                label: 'Functions',
                type: 'long_text'
            }
        ]
    },

    function: {
        key: 'name',
        fields: [
            {
                name: 'id',
                label: 'ID',
                type: 'short_text'
            },
            {
                name: 'name',
                label: 'Name',
                type: 'short_text'
            },
            {
                name: 'description',
                label: 'Description',
                type: 'long_text'
            }
        ]
    },

    brand: {
        key: 'name',
        fields: [
            {
                name: 'id',
                label: 'ID',
                type: 'short_text'
            },
            {
                name: 'name',
                label: 'Name',
                type: 'short_text'
            },
            {
                name: 'description',
                label: 'Description',
                type: 'long_text'
            }
        ]
    },

    type: {
        key: 'name',
        fields: [
            {
                name: 'id',
                label: 'ID',
                type: 'short_text'
            },
            {
                name: 'name',
                label: 'Name',
                type: 'short_text'
            },
            {
                name: 'description',
                label: 'Description',
                type: 'long_text'
            }
        ]
    }
}