package com.example.logging;

import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiElement;
import org.springframework.boot.logging.logback.ColorConverter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class CustomColorConverter extends ColorConverter {

    @Override
    protected String transform(ILoggingEvent event, String in) {
        AnsiElement element = null;
        if (event.getLevel() == Level.INFO) {
            element = AnsiColor.GREEN;
        } else if (event.getLevel() == Level.DEBUG) {
            element = AnsiColor.MAGENTA;
        } else if (event.getLevel() == Level.TRACE) {
            element = AnsiColor.BRIGHT_GREEN;
        }

        if (element == null) {
            return super.transform(event, in);
        }
        else {
            return toAnsiString(in, element);
        }
    }
}
