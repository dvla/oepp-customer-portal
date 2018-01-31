$('#paymentFrame').on('load', function() {
    window.scrollTo(0,0);
    var iframe = $(this)[0];
    iframe.focus();
});

