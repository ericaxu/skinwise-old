function setupProfileAutocomplete() {
    var profile_fields = [ 'add_ingredient_work', 'add_ingredient_doesnt_work', 'add_ingredient_bad_reaction' ];
    for (var i = 0; i < profile_fields.length; i++) {
        var name = profile_fields[i];
        enableAutocomplete('ingredient',
            $('#' + name),
            '#' + name + '_container',
            SW.AUTOCOMPLETE_LIMIT.ADD_FILTER,
            $('#' + name + '_not_found'));
    }
}

$(document).on('ready', function() {
    $('.profile_add_input').on('focus', function() {
        $(this).next().show();
    }).on('blur', function() {
        if ($(this).val() === '') {
            $(this).next().hide();
        }
    });

    setupProfileAutocomplete();
});