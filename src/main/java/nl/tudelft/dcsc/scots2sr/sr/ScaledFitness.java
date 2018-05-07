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

import java.util.Arrays;

/**
 *
 * Represents the fitness with scaling
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class ScaledFitness extends ExtendedFitness {

    private static final double NO_SCALE = 1.0;
    private static final double NO_SHIFT = 0.0;

    //Stores the scale used, 1.0 if none
    private final double m_scales[];
    //Stores the shift used, 0.0 if none
    private final double m_shifts[];

    /**
     * The constructor.
     *
     * @param ex_ftn the exact fitness
     * @param req_ftn the requested fitness
     * @param scales the vector of scaling factors per input dof
     * @param shifts the vector of shifting factor per input dof
     */
    public ScaledFitness(final double ex_ftn, final double req_ftn,
            final double scales[], final double shifts[]) {
        super(ex_ftn, req_ftn);
        m_scales = scales;
        m_shifts = shifts;
    }
    
    /**
     * Get the scaling factor, if any
     *
     * @param idx the input dof index (starts from 0)
     * @return the scaling factors, or NO_SCALE if none
     */
    public double get_scale(final int idx) {
        return m_scales[idx];
    }

    /**
     * Get the shift factor, if any
     *
     * @param idx the input dof index (starts from 0)
     * @return the shifting factors, or NO_SHIFT if none
     */
    public double get_shift(final int idx) {
        return m_shifts[idx];
    }

    /**
     * Checks if scaling is set
     *
     * @param idx the input dof index (starts from 0)
     * @return true if scaling is set
     */
    public boolean is_scale(final int idx) {
        return (m_scales[idx] != NO_SCALE);
    }

    /**
     * Checks if shifting is set
     *
     * @param idx the input dof index (starts from 0)
     * @return true if shifting is set
     */
    public boolean is_shift(final int idx) {
        return (m_shifts[idx] != NO_SHIFT);
    }

    @Override
    public String toString() {
        return "[" + super.toString() + ", sc: "
                + Arrays.toString(m_scales) + ", sh: "
                + Arrays.toString(m_shifts) + "]";
    }

}
