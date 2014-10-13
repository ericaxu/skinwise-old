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

/* Timeout objects */
SW.SEARCHBAR_EXPAND_TIMEOUT = null;

/* Templates */
SW.TEMPALTES = {};
SW.TEMPALTES.NOTICE = _.template('<div class="<%= type %>"><span class="close_btn"></span><p class="message"><%= message %></p></div>')

/* Product ingredient data */
SW.ING = {};