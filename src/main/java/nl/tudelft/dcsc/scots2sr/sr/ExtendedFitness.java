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
package nl.tudelft.dcsc.scots2sr.sr;

import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tudelft.dcsc.sr2jlib.fitness.Fitness;

/**
 *
 * The extended fitness class
 *
 * @author Dr. Ivan S. Zapreev
 */
public class ExtendedFitness extends Fitness {

    private static final Logger LOGGER = Logger.getLogger(ExtendedFitness.class.getName());

    //Stores the exact fitness
    private final double m_act_ftn;

    /**
     * The constructor.
     *
     * @param act_ftn the actual fitness - the exact fitness we want to get 1.0
     * value of
     * @param req_ftn the requested fitness to guide the GP process, is an
     * overestimation of the actual fitness
     */
    public ExtendedFitness(final double act_ftn, final double req_ftn) {
        super(req_ftn);
        m_act_ftn = act_ftn;
    }

    /**
     * Get the exact fitness
     *
     * @return the exact fitness
     */
    public double get_actual_fitness() {
        return m_act_ftn;
    }

    @Override
    public boolean is_one() {
        return (m_act_ftn == 1.0);
    }

    @Override
    public String toString() {
        return "[req: " + m_ftn + ", ex: " + m_act_ftn + "]";
    }

    @Override
    public boolean is_equal(final Fitness other) {
        if (other != null) {
            if (super.is_equal(other)) {
                if ((other instanceof ExtendedFitness)) {
                    final ExtendedFitness e_other = ((ExtendedFitness) other);
                    return (this.m_act_ftn == e_other.m_act_ftn);
                }
            }
            return false;
        } else {
            throw new IllegalArgumentException("Attempting to compare withness with null!");
        }
    }
}
