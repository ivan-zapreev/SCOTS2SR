/*
 * Copyright (C) 2018 Your Organisation
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.tudelft.dcsc.scots2sr.sr;

import nl.tudelft.dcsc.sr2jlib.IndividualFilter;
import nl.tudelft.dcsc.sr2jlib.fitness.Fitness;
import nl.tudelft.dcsc.sr2jlib.grid.Individual;

/**
 * Realizes a filter for individuals
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class AvgFilter implements IndividualFilter {

    //Stores the triggering bound for the filter
    private final double m_bound;
    //Stores the flag indicating whether the filtering is now being performed
    private boolean m_is_active;
    //Stores the fitness bound to filter upon
    private double m_ftn_bound;
    //Stores the interval in milli-seconds between filter actions
    private final long m_interval;
    //Stores the previous filtering time-stamp
    private long m_prev_flt;

    /**
     * The basic constructor
     *
     * @param bound the bound for triggering the filtering
     * @param interval the time interval in milliseconds between filtering
     */
    public AvgFilter(final double bound, final long interval) {
        this.m_bound = bound;
        this.m_is_active = false;
        this.m_ftn_bound = 0.0;
        this.m_interval = interval;
        this.m_prev_flt = System.currentTimeMillis();
    }

    /**
     * Allows to retrieve the triggering bound
     *
     * @return the triggering bound
     */
    public double get_bound() {
        return m_bound;
    }

    /**
     * Allows to check if the filtering needs to be triggered
     *
     * @param value the value to be checked to be below the bound
     *
     * @return true if the filtering is to be done
     */
    public synchronized boolean is_trigger(final double value) {
        final long curr_time = System.currentTimeMillis();
        return (!m_is_active)
                && ((curr_time - m_prev_flt) >= m_interval)
                && (value < m_bound);
    }

    /**
     * Must be called when the filtering is starting
     */
    public synchronized void start_filtering() {
        m_is_active = true;
    }

    /**
     * Must be called when the filtering is stopped
     */
    public synchronized void stop_filtering() {
        m_is_active = false;
        m_prev_flt = System.currentTimeMillis();
    }

    /**
     * Allows to set the fitness bound.
     *
     * @param ftn_bound the fitness bound for filtering
     */
    public void set_ftn_bound(final double ftn_bound) {
        m_ftn_bound = ftn_bound;
    }

    @Override
    public boolean evaluate(Individual ind) {
        final Fitness ftn = ind.get_fitness();
        if (ftn instanceof ExtendedFitness) {
            //Filter out all individuals with the exact fitness less than given
            return (((ExtendedFitness) ftn).get_actual_fitness() < m_ftn_bound);
        } else {
            //If it is not an extended fitness then the individual's compilation was failed
            return true;
        }
    }

}
