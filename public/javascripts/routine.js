$(document).on('ready', function() {
    $(document).on('click', '.edit_routine', function() {
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

    $('.create_routine').on('click', createNewRoutine);
});

function createNewRoutine() {
    var new_routine = {
        name: 'Click to rename me',
        products: [{
            brand: 'Kiehl\'s',
            name: 'Blue Herbal Spot Treatment'
        }]
    };
    $('.user_routine').append(routineHTML(new_routine, true));

}

function fetchRoutine(user_id) {
    postToAPI('/user/routine/get', {}, loadRoutines);
}

function routineItemHTML(product) {
    var $li = $('<li/>');
    $li.append($('<a/>', { href: '#', class: 'product_brand', text: product.brand }));
    $li.append(' ');
    $li.append($('<a/>', { href: '#', class: 'product_name', text: product.name }));
    $li.append('<div class="edit_btns"><input type="button" class="edit_product" value="Edit"/><input type="button" ' +
        'class="delete_product" value="Delete"/></div>');

    return $li;
}

function routineHTML(routine, is_new) {

    console.log(routine);
    var $div = $('<div/>', { class: is_new ? 'column editing' : 'column' });
    var $routine = $('<div/>', { class: 'section' });
    $routine.append($('<h2/>', { text: routine.name }));
    var $routine_list = $('<ol/>', { class: 'routines' }).sortable({
        placeholder: 'routine_placeholder'
    });

    for (var i = 0; i < routine.products.length; i++) {
        $routine_list.append(routineItemHTML(routine.products[i]));
    }

    $routine.append($routine_list);
    $div.append($routine);
    if (is_new) {
        $div.append('<label>Add product: </label><input type="text" class="add_product">');
        $div.append('<input type="button" class="primary edit_routine" value="Save routine">');

    } else {
        $div.append('<input type="button" class="primary edit_routine" value="Edit routine">');
    }

    return $div;
}

function loadRoutines(response) {
    for (var i = 0; i < response.results.length; i++) {
        $('.user_routine').append(routineHTML(response.results[i]));
    }
}