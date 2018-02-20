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

/**
 *
 * Represents the fitness with scaling
 *
 * @author Dr. Ivan S. Zapreev
 */
public class ScaledFitness extends ExtendedFitness {

    private static final double NO_SCALE = 1.0;
    private static final double NO_SHIFT = 0.0;

    //Stores the scale used, 1.0 if none
    private final double m_scale;
    //Stores the shift used, 0.0 if none
    private final double m_shift;

    /**
     * The constructor.
     *
     * @param ex_ftn the exact fitness
     * @param req_ftn the requested fitness
     * @param scale the scaling factor
     * @param shift the shifting factor
     */
    public ScaledFitness(final double ex_ftn, final double req_ftn,
            final double scale, final double shift) {
        super(ex_ftn, req_ftn);
        m_scale = scale;
        m_shift = shift;
    }

    /**
     * The constructor, assumes no scaling and shifting.
     *
     * @param ex_ftn the exact fitness
     * @param req_ftn the requested fitness
     */
    public ScaledFitness(final double ex_ftn, final double req_ftn) {
        this(ex_ftn, req_ftn, NO_SCALE, NO_SHIFT);
    }

    /**
     * Get the scaling factor, if any
     *
     * @return the scaling factor, or NO_SCALE if none
     */
    public double get_scale() {
        return m_scale;
    }

    /**
     * Get the shift factor, if any
     *
     * @return the shifting factor, or NO_SHIFT if none
     */
    public double get_shift() {
        return m_shift;
    }

    /**
     * Checks if scaling is set
     *
     * @return true if scaling is set
     */
    public boolean is_scale() {
        return (m_scale != NO_SCALE);
    }

    /**
     * Checks if shifting is set
     *
     * @return true if shifting is set
     */
    public boolean is_shift() {
        return (m_shift != NO_SHIFT);
    }

    @Override
    public String toString() {
        return "[" + super.toString() + ", sc: "
                + m_scale + ", sh: " + m_shift + "]";
    }

}
