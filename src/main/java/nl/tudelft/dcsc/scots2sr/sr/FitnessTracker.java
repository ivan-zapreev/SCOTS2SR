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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import nl.tudelft.dcsc.sr2jlib.fitness.Fitness;
import nl.tudelft.dcsc.sr2jlib.grammar.Grammar;
import nl.tudelft.dcsc.sr2jlib.grammar.expr.Expression;
import nl.tudelft.dcsc.sr2jlib.grammar.expr.FunctExpr;
import nl.tudelft.dcsc.sr2jlib.grammar.expr.DConstExpr;
import nl.tudelft.dcsc.sr2jlib.grid.Individual;
import nl.tudelft.dcsc.sr2jlib.grid.GridObserver;

/**
 * This is a fitness tracker implementation of the grid observer class, is
 * responsible for monitoring the current SR grid state.
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public abstract class FitnessTracker implements GridObserver {

    private static final Logger LOGGER = Logger.getLogger(FitnessTracker.class.getName());

    private static final int MIN_DATA_SIZE = 3;

    private final Individual[][] m_pop_grid;

    private final int m_size_x;
    private final int m_size_y;

    private double m_req_mean;
    private double m_req_dev;
    private double m_req_max;

    private double m_ex_mean;
    private double m_ex_dev;
    private double m_act_max;

    /**
     * The basic constructor
     *
     * @param size_x the population grid size in x
     * @param size_y the population grid size in y
     */
    public FitnessTracker(final int size_x, final int size_y) {
        this.m_size_x = size_x;
        this.m_size_y = size_y;

        this.m_pop_grid = new Individual[size_x][];
        IntStream.range(0, size_x).forEachOrdered(idx -> {
            this.m_pop_grid[idx] = new Individual[size_y];
        });

        this.m_req_mean = 0.0;
        this.m_req_dev = 0.0;
        this.m_req_max = Double.NEGATIVE_INFINITY;

        this.m_ex_mean = 0.0;
        this.m_ex_dev = 0.0;
        this.m_act_max = Double.NEGATIVE_INFINITY;
    }

    /**
     * Allows to get the fitness mean and deviation
     *
     * @return the fitness mean, deviation and maximum fitness
     */
    public synchronized double[] get_req_fitness() {
        return new double[]{m_req_mean, m_req_dev, m_req_max};
    }

    /**
     * Allows to get the fitness mean and deviation
     *
     * @return the fitness mean, deviation and maximum fitness
     */
    public synchronized double[] get_ex_fitness() {
        return new double[]{m_ex_mean, m_ex_dev, m_act_max};
    }

    @Override
    public synchronized void set(final Individual new_ind) {
        //First re-work the scale and shift from the fitness into the individual.
        final Fitness ftn = new_ind.get_fitness();
        if (ftn instanceof ScaledFitness) {
            final ScaledFitness sftn = (ScaledFitness) ftn;
            new_ind.update_exprs((Expression expr, final int idx) -> {
                //Add the shifting and scaling if any
                //If there is a scaling factro then use it
                if (sftn.is_scale(idx)) {
                    final double scale = sftn.get_scale(idx);
                    expr = FunctExpr.make_binary(Grammar.NUM_ENTRY_TYPE_STR,
                            expr, Grammar.NUM_ENTRY_TYPE_STR, "*",
                            DConstExpr.make_const(Grammar.NUM_ENTRY_TYPE_STR, scale),
                            DConstExpr.ENTRY_CDOUBLE_STR);
                }
                //If there is shifting then use it
                if (sftn.is_shift(idx)) {
                    final double shift = sftn.get_shift(idx);
                    expr = FunctExpr.make_binary(Grammar.NUM_ENTRY_TYPE_STR,
                            expr, Grammar.NUM_ENTRY_TYPE_STR, "+",
                            DConstExpr.make_const(Grammar.NUM_ENTRY_TYPE_STR, shift),
                            DConstExpr.ENTRY_CDOUBLE_STR);
                }
                return expr;
            });
        }

        final Individual old_ind = m_pop_grid[new_ind.get_pos_x()][new_ind.get_pos_y()];
        LOGGER.log(Level.FINE, "Settling {0} in place of {1}", new Object[]{new_ind, old_ind});
        m_pop_grid[new_ind.get_pos_x()][new_ind.get_pos_y()] = new_ind;
    }

    @Override
    public synchronized void remove(final Individual old_ind) {
        //Remove an old individual from the grid
        m_pop_grid[old_ind.get_pos_x()][old_ind.get_pos_y()] = null;
    }

    /**
     * Allows to extract the actual fitness from the individual
     *
     * @param ind the individual, not null
     * @return its actual fitness if instance of ExtendedFirness or 0.0
     * otherwise
     */
    private double get_actual_fitness(final Individual ind) {
        if ((ind.get_fitness() instanceof ExtendedFitness)) {
            return ((ExtendedFitness) ind.get_fitness()).get_act_ftn();
        } else {
            return 0.0;
        }
    }

    @Override
    public synchronized List<Individual> get_best_fit_ind() {
        //Return the list of best fit individuals
        double max_ftn = 0.0;
        final List<Individual> best_fit = new ArrayList<>();
        for (int pos_x = 0; pos_x < m_size_x; ++pos_x) {
            for (int pos_y = 0; pos_y < m_size_y; ++pos_y) {
                final Individual ind = get_individual(pos_x, pos_y);
                if (ind != null) {
                    if (best_fit.isEmpty()) {
                        best_fit.add(ind);
                        max_ftn = get_actual_fitness(ind);
                    } else {
                        final double new_ftn = get_actual_fitness(ind);
                        if (new_ftn == max_ftn) {
                            best_fit.add(ind);
                            LOGGER.log(Level.FINE,
                                    "The number of {0} fit individuals is {1}",
                                    new Object[]{max_ftn, best_fit.size()});
                        } else {
                            if (new_ftn > max_ftn) {
                                best_fit.clear();
                                max_ftn = new_ftn;
                                best_fit.add(ind);
                            } else {
                                //The new individual is less fit, ignore it
                            }
                        }
                    }
                } else {
                    //The individual is not there or could not be compiled
                }
            }
        }
        return best_fit;
    }

    /**
     * Allows to get an individual at the given position
     *
     * @param pos_x position in x
     * @param pos_y position in y
     * @return and individual in the given position, or null if none
     */
    private synchronized Individual get_individual(final int pos_x, final int pos_y) {
        return m_pop_grid[pos_x][pos_y];
    }

    /**
     * Allows to re-compute the fitness values This method is not fully thread
     * safe. There must be one thread calling this method at the same time!
     *
     * @return true if there is data to be plotted, otherwise false
     */
    protected boolean re_compute_fitness() {
        double num_ind = 0.0;

        m_req_max = Double.NEGATIVE_INFINITY;
        m_act_max = Double.NEGATIVE_INFINITY;
        BigDecimal req_sum_bd = new BigDecimal(0.0);
        BigDecimal req_sum_sq_bd = new BigDecimal(0.0);
        BigDecimal act_sum_bd = new BigDecimal(0.0);
        BigDecimal act_sum_sq_bd = new BigDecimal(0.0);
        for (int pos_x = 0; pos_x < m_size_x; ++pos_x) {
            for (int pos_y = 0; pos_y < m_size_y; ++pos_y) {
                final Individual ind = get_individual(pos_x, pos_y);
                if (ind != null) {
                    final Fitness ftn = ind.get_fitness();
                    final double ext_ftn = ftn.get_fitness();
                    final BigDecimal req_ftn_bd = new BigDecimal(ext_ftn);
                    req_sum_bd = req_sum_bd.add(req_ftn_bd);
                    req_sum_sq_bd = req_sum_sq_bd.add(req_ftn_bd.pow(2));
                    m_req_max = Math.max(m_req_max, ext_ftn);

                    final double act_ftn;
                    if (ftn instanceof ExtendedFitness) {
                        act_ftn = ((ExtendedFitness) ftn).get_act_ftn();
                    } else {
                        act_ftn = 0;
                    }
                    final BigDecimal act_ftn_bd = new BigDecimal(act_ftn);
                    act_sum_bd = act_sum_bd.add(act_ftn_bd);
                    act_sum_sq_bd = act_sum_sq_bd.add(act_ftn_bd.pow(2));
                    m_act_max = Math.max(m_act_max, act_ftn);
                    ++num_ind;
                }
            }
        }

        //If the sample mean and variance are computable then schedule and update
        if (num_ind >= MIN_DATA_SIZE) {
            final BigDecimal num_ind_bd = new BigDecimal(num_ind - 1.0);

            final BigDecimal req_mean_bd = req_sum_bd.divide(num_ind_bd, 10, RoundingMode.HALF_UP);
            m_req_mean = req_mean_bd.doubleValue();
            final BigDecimal req_sq_mean_bd = req_mean_bd.pow(2);
            final BigDecimal req_mean_sq_bd = req_sum_sq_bd.divide(num_ind_bd, 10, RoundingMode.HALF_UP);
            m_req_dev = (req_mean_sq_bd.doubleValue() > req_sq_mean_bd.doubleValue())
                    ? Math.sqrt(req_mean_sq_bd.doubleValue() - req_sq_mean_bd.doubleValue()) : 0.0;

            final BigDecimal ex_mean_bd = act_sum_bd.divide(num_ind_bd, 10, RoundingMode.HALF_UP);
            m_ex_mean = ex_mean_bd.doubleValue();
            final BigDecimal ex_sq_mean_bd = ex_mean_bd.pow(2);
            final BigDecimal ex_mean_sq_bd = act_sum_sq_bd.divide(num_ind_bd, 10, RoundingMode.HALF_UP);
            m_ex_dev = (ex_mean_sq_bd.doubleValue() > ex_sq_mean_bd.doubleValue())
                    ? Math.sqrt(ex_mean_sq_bd.doubleValue() - ex_sq_mean_bd.doubleValue()) : 0.0;
            return true;
        } else {
            return false;
        }
    }

}
