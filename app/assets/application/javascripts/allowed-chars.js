$(function() {
    var inputs = $('input[data-allowed-chars]');

    inputs.each(function() {
        var element = $(this);

        var allowedChars = element.data('allowed-chars');
        element.allowedChars(new RegExp(allowedChars));
    });
});
