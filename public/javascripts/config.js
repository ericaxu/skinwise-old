var SW = {};
SW.CONFIG = {};

/* Animation configs */
SW.CONFIG.NOTICE_FADE_IN = 200;
SW.CONFIG.NOTICE_FADE_OUT = 200;
SW.CONFIG.SEARCHBAR_EXPAND_TIMEOUT = 400;

/* Timeout objects */
SW.SEARCHBAR_EXPAND_TIMEOUT = null;

/* Templates */
SW.TEMPALTES = {};
SW.TEMPALTES.NOTICE = _.template('<div class="<%= type %>"><span class="close_btn"></span><p class="message"><%= message %></p></div>')

/* Test data */
SW.TEST_ING_LIST = {
    'zinc oxide': {
        functions: [ 'Sunscreen', 'Anti-inflammatory' ],
        short_desc: 'Zinc oxide is a mineral that is primarily used as a sunscreen ingredient, providing physical protection ' +
            'against both UVA and UVB rays. It also has antibacterial and anti-inflammatory properties, often making an appearance ' +
            'in acne treatments, calamine lotion and diaper rash creams.'
    }
};