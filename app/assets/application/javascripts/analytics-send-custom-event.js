$(function () {
    function sendEvent(eventCategory, eventAction, eventLabel) {
        if (eventLabel && eventLabel.length > 0) {
            ga('send', 'event', eventCategory, eventAction, eventLabel);
        }
    }

    // Send a google analytics event when an element is loaded that has the 'analytics-load-event-trigger' class.
    // An example of html that would use this is <div class="analytics-load-event-trigger" data-event-label="Your GA label"></div>
    var triggerElement = $('.analytics-load-event-trigger');
    if (triggerElement.length > 0) {
        var label = triggerElement.data('eventLabel');
        sendEvent('Data', 'Loaded', label);
    }

    // Send a google analytics event when an element that has the 'analytics-click-event-trigger' class is clicked.
    // An example of html that would use this is <a href="http://somelink" class="analytics-click-event-trigger" data-event-label="Your GA label">Some text</a>
    $('.analytics-click-event-trigger').on('click', function () {
        var label = $(this).data('eventLabel');
        sendEvent('Navigation', 'Clicked', label);
    });
});
