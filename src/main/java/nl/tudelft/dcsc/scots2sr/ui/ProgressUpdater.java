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

/**
 * This class is synchronized and allows to update progress bar in the
 * multi-threaded and streamed environment.
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class ProgressUpdater {

    /**
     * The call back interface to update the UI progress
     */
    @FunctionalInterface
    public interface UpdateCallBack {

        public void update_progress(final int curr_cnt, final int total_cnt);
    }

    private final UpdateCallBack m_updater;
    private final int m_total_cnt;
    private int m_current_cnt;

    /**
     * The basic constructor
     *
     * @param updater the call back functional interface to update the UI
     * elements
     * @param total_cnt the maximum count to be registered by the updater
     */
    public ProgressUpdater(
            final UpdateCallBack updater,
            final int total_cnt) {
        m_updater = updater;
        m_total_cnt = total_cnt;
        m_current_cnt = 0;
    }

    /**
     * Allows to increment the internal counter and request progress update
     */
    public synchronized void update() {
        m_current_cnt += 1;
        m_updater.update_progress(m_current_cnt, m_total_cnt);
    }

}
