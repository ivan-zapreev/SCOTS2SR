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

import javafx.animation.AnimationTimer;
import nl.tudelft.dcsc.scots2sr.sr.ExtendedFitness;
import nl.tudelft.dcsc.scots2sr.sr.FitnessTracker;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import nl.tudelft.dcsc.sr2jlib.fitness.Fitness;
import nl.tudelft.dcsc.sr2jlib.grid.Individual;

/**
 * This class represents the visualizer for the dof symbolic regression process
 *
 * @author Dr. Ivan S. Zapreev
 */
public class DofVisualizer extends FitnessTracker {

    /**
     * This functional interface is needed for fitness change call-backs
     */
    @FunctionalInterface
    public interface FitnessChange {

        /**
         * The Fitness change notification interface
         *
         * @param req_ftn the requested fitness values
         * @param ex_ftn the exact fitness values
         */
        public void change(final double[] req_ftn, final double[] ex_ftn);
    }

    /**
     * The animation class that is used to update the fitness value
     */
    private class ChartUpdater extends AnimationTimer {

        private static final long MIN_UPD_INTERVAL = (long) (1.0e9 / 24.0);
        private long m_prev = 0;

        @Override
        public void handle(long now) {
            if (m_prev != 0) {
                if ((now - m_prev) >= MIN_UPD_INTERVAL) {
                    //Update the fitness values
                    DofVisualizer.this.update_fitness();
                    //Remember the new previos time
                    m_prev = now;
                }
            } else {
                //Remember the new previos time
                m_prev = now;
            }
        }
    };

    private boolean m_is_update;
    private final ChartUpdater m_chart_upd;
    private final ProgressIndicator m_run_prg;
    private final GridView m_grid_view;
    private final FitnessChart m_req_chart;
    private final FitnessChart m_ex_chart;
    private FitnessChange m_ftn_change;

    /**
     * The basic constructor
     *
     * @param size_x the number of x elements in the grid
     * @param size_y the number of y elements in the grid
     * @param cont the progress indicator container
     * @param dof_title the dof title
     * @param ph the preferred indicator height
     * @param pw the preferred indicator width
     * @param grid_view the grid view for visualization
     * @param req_chart the requested fitness chart
     * @param ex_chart the exact fitness chart
     */
    public DofVisualizer(final int size_x,
            final int size_y,
            final HBox cont,
            final String dof_title,
            final double ph, final double pw,
            final GridView grid_view,
            final FitnessChart req_chart,
            final FitnessChart ex_chart) {
        super(size_x, size_y);

        this.m_is_update = false;
        this.m_chart_upd = new ChartUpdater();
        this.m_run_prg = new ProgressIndicator();
        this.m_run_prg.setProgress(-1.0);
        this.m_run_prg.setPrefHeight(ph);
        this.m_run_prg.setPrefWidth(pw);
        this.m_run_prg.setVisible(false);
        cont.getChildren().add(new Label(dof_title + ": "));
        cont.getChildren().add(m_run_prg);
        cont.getChildren().add(new Separator(Orientation.VERTICAL));

        this.m_grid_view = grid_view;
        this.m_req_chart = req_chart;
        this.m_ex_chart = ex_chart;
        this.m_ftn_change = null;
    }

    @Override
    public void start_observing() {
        //Start the grid animation
        m_grid_view.start();
        //Start the chart animations
        m_req_chart.start();
        m_ex_chart.start();
        //Start the progress update
        m_run_prg.setVisible(true);
        //Start the chart updater
        m_chart_upd.start();
    }

    @Override
    public synchronized void add_individual(final Individual ind) {
        //Mark the change in the fitness tracker
        super.add_individual(ind);

        //Update the Grid, take care of the case when the fitness ould not be computed
        final Fitness ftn = ind.get_fitness();
        final double act_ftn;
        if (ftn instanceof ExtendedFitness) {
            act_ftn = ((ExtendedFitness) ftn).get_actual_fitness();
        } else {
            act_ftn = 0.0;
        }
        m_grid_view.set_fitness(ind.get_pos_x(), ind.get_pos_y(), act_ftn);

        //Mart that we need an update
        m_is_update = true;
    }

    @Override
    public synchronized void kill_individual(final Individual ind) {
        //Mark the change in the fitness tracker
        super.kill_individual(ind);

        //Update the Grid
        m_grid_view.clear_fitness(ind.get_pos_x(), ind.get_pos_y());

        //Mart that we need an update
        m_is_update = true;
    }

    /**
     * Allows to set a new fitness change listener
     *
     * @param ftn_change the new fitness change listener
     * @return the previously set fitness change listener
     */
    public FitnessChange set_ftn_change_listener(final FitnessChange ftn_change) {
        final FitnessChange tmp_ftn_change = m_ftn_change;
        m_ftn_change = ftn_change;
        return tmp_ftn_change;
    }

    /**
     * Update the fitness in the user interface
     */
    private synchronized void update_fitness() {
        //Re-compute fitness and plot data
        if (m_is_update && re_compute_fitness()) {
            //Get the fitness values
            final double[] req_ftn = get_req_fitness();
            final double[] ex_ftn = get_ex_fitness();
            //Fitness update
            if (m_ftn_change != null) {
                m_ftn_change.change(req_ftn, ex_ftn);
            }
            //Schedule the chart updates
            m_req_chart.schedule_update(req_ftn);
            m_ex_chart.schedule_update(ex_ftn);
            //Mark the update as done
            m_is_update = false;
        }
    }

    @Override
    public void stop_observing() {
        //Stop the chart updater
        m_chart_upd.stop();
        //Stop the grid animation
        m_grid_view.stop();
        //Stop the grichartd animation
        m_req_chart.stop();
        m_ex_chart.stop();
        //Stop the progress update
        m_run_prg.setVisible(false);
    }

    /**
     * Shall be called when the manager is to submit data to the
     *
     * @param is_active true if the manager is set active, otherwise false
     */
    public void set_active(final boolean is_active) {
        if (is_active) {
            m_req_chart.set_active();
            m_ex_chart.set_active();
        }
    }
}
