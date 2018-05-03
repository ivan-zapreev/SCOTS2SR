/*
 * Copyright (C) 2018 Dr. Ivan S. Zapreev <ivan.zapreev@gmail.com>
 *
 *  Visit my Linked-in profile:
 *     https://nl.linkedin.com/in/zapreevis
 *  Visit my GitHub:
 *     https://github.com/ivan-zapreev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.tudelft.dcsc.scots2sr.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

/**
 * Is used for trivial logging of events in the UI. Is thread safe.
 */
public class ConsoleLog {

    //Stores the list view
    private final ListView<String> m_view;

    /**
     * The basic constructor
     *
     * @param view the list view to log data into
     */
    public ConsoleLog(ListView<String> view) {
        m_view = view;
    }

    /**
     * Allows to log a new message to the UI console
     *
     * @param msg the message to be logged
     */
    public synchronized void log(final String msg) {
        final LocalDateTime data_time = LocalDateTime.now();
        final ObservableList<String> list = m_view.getItems();
        final String dts = data_time.format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        ).replaceAll("T", " ");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                list.add(dts + " " + msg);
            }
        });
    }

    /**
     * Allows to log an info message to the UI console
     *
     * @param msg the message to be logged
     */
    public void info(final String msg) {
        log("INFO: " + msg);
    }

    /**
     * Allows to log a warning message to the UI console
     *
     * @param msg the message to be logged
     */
    public void warn(final String msg) {
        log("WARNING: " + msg);
    }

    /**
     * Allows to log an error message to the UI console
     *
     * @param msg the message to be logged
     */
    public void err(final String msg) {
        log("ERROR: " + msg);
    }

}
