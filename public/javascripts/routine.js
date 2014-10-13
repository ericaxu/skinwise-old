$(document).on('ready', function() {
    $('.edit_routine').on('click', function() {
        if (!$(this).parent().hasClass('editing')) {
            $(this).parent().find('.routines').sortable('enable');
            var $cancel_btn = $('<input/>', {
                type: 'button',
                value: 'Cancel',
                class: 'primary'
            }).on('click', function() {
                $(this).parent().removeClass('editing').find('.edit_routine').val('Edit routine');
                $(this).parent().find('.routines').sortable('disable');
                $(this).remove();
            });
            $(this).parent().addClass('editing').append($cancel_btn);
            $(this).val('Save routine');
        }
    });
    $('.routines').sortable({
        placeholder: 'routine_placeholder'
    });
});