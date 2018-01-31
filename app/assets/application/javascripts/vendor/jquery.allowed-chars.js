/**
 * WARNING: THIS IS IN-HOUSE MODIFIED VERSION OF THE FOLLOWING JQUERY PLUGIN.
 * IT INCLUDES FIX FOR THE ISSUE OF BLOCKING SPECIAL KEYS IN FIREFOX BROWSERS.
 *
 * jQuery Allowed Chars simple plugin version: 1.0.4, date: 27/05/2014
 *
 * jQuery plugin to restrict users for typing only allowed chars (regExp) for specified element
 *
 * Copyright (c) 2014 Pavlo Voznenko (p.voznenko@gmail.com) and other contributors
 * Distributed under the MIT License
 *
 * GitHub: https://github.com/fosco-maestro/jquery-allowed-chars-simple-plugin
 * jQuery Plugin: http://plugins.jquery.com/jquery.allowed-chars/
 *
 * Usage Example: @see github: https://github.com/fosco-maestro/jquery-allowed-chars-simple-plugin
 *
 * Demo available on: http://jsfiddle.net/fosco/55XLd/
 */
;(function($, undefined) {
    "use strict";

    if (!Array.prototype.indexOf) {
        Array.prototype.indexOf = function(value) {
            return jQuery.inArray(value, this);
        };
    }

    /**
     * By default plugin allow only int chars: 0123456789
     * You can change chars by passing string with chars as parameter
     *
     * @param {String|RegExp|Object} options
     */
    $.fn.allowedChars = function(options) {
        var settings = {
                allowed       : "0123456789",
                caseSensitive : true
            },
            $this = $(this);

        var methods = {

            /**
             * Method for init options
             *
             * @param {String|RegExp|Object} options
             * @returns {boolean}
             */
            init : function (options) {
                if (undefined !== options) {
                    var optionsType = Object.prototype.toString.call(options);
                    switch (optionsType) {
                        case "[object RegExp]":
                        case "[object String]":
                            settings.allowed = options;
                            break;
                        case "[object Object]":
                            $.extend(settings, options);
                            break;
                        default:
                            methods.errorHandler("Unexpected 'options' type: " + optionsType +
                                "; Supported types: RegExp, String, Object");

                            return false;
                        }
                }
                return true;
            },

            /**
             * Error handle, writing error to console through console.log if console.log defined
             *
             * @param {String} msg error message
             */
            errorHandler : function(msg) {
                if (undefined !== console && undefined !== console.log) {
                    console.log('jquery.allowedChars error: ' + msg);
                }
            }
        };

        if (methods.init(options)) {
            $this.keypress(function(event) {
                var key = window.event ? window.event.keyCode : (event.which || undefined),
                    chars = settings.allowed,
                    isRegExp = "[object RegExp]" === Object.prototype.toString.call(chars),
                    keyChar;

                if (key === undefined) {
                    return true;
                }

                keyChar = String.fromCharCode(key);

                if (isRegExp) {
                    if (chars.test(keyChar)) {
                        return true;
                    }

                    preventUntilSpecialKeyIsPressed(key);
                } else {
                    if (!settings.caseSensitive) {
                        chars = chars.toLowerCase();
                        keyChar = keyChar.toLowerCase();
                    }

                    if (chars.indexOf(keyChar) !== -1) {
                        return true;
                    }

                    preventUntilSpecialKeyIsPressed(key);
                }

                function preventUntilSpecialKeyIsPressed(key) {
                    var specialKeys = [
                        0,  // null
                        8,  // backspace
                        9,  // horizontal tab
                        13, // carriage return
                        27  // escape
                    ];

                    if (key != null && specialKeys.indexOf(key) === -1) {
                        event.preventDefault();
                    }
                }

                return true;
            });
        }
    };
}(jQuery));