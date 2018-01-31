$(function () {
    var textarea = $('#feedback-form').find('textarea[maxLength]');

    if (textarea.length > 0) {
        var countdown = $('.character-countdown');
        var limit = textarea.attr('maxLength');

        function updateCountdown() {
            function crossPlatformLength(text) {
                return text.replace(/\r(?!\n)|\n(?!\r)/g, '\r\n').length;
            }

            countdown.text(limit - crossPlatformLength(textarea.val()));
        }

        updateCountdown();
        textarea.on('change keyup', updateCountdown);
    }
});