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

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;

/**
 * The population fitness chart diagram
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class FitnessChart extends LineChart<Number, Number> {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(FitnessChart.class.getName());

    private static final int NODE_REMOVE_IDX = 0;
    private static final int MAX_DATA_SIZE = 1000;
    private static final String MEAN_STR = "Avg: ";
    private static final String MDEV_STR = "-Dev: ";
    private static final String PDEV_STR = "+Dev: ";
    private static final String MAX_STR = "Max: ";

    /**
     * The graphics animation that allows to update the canvas with the new
     * elements from the fitness update queue
     */
    private class ChartAnimation extends AnimationTimer {

        public static final long MIN_UPD_INTERVAL = (long) (1.0e9 / 2.0);

        private long m_prev = 0;

        @Override
        public void handle(long now) {
            synchronized (FitnessChart.this) {
                if ((m_prev != 0)) {
                    if (m_is_data && ((now - m_prev) >= MIN_UPD_INTERVAL)) {
                        m_mean_ser.getData().add(new XYChart.Data<>(m_mut_num, m_mean));
                        final double req_mmd = m_mean - m_dev;
                        m_mdev_ser.getData().add(new XYChart.Data<>(m_mut_num, req_mmd));
                        final double req_mpd = m_mean + m_dev;
                        m_pdev_ser.getData().add(new XYChart.Data<>(m_mut_num, req_mpd));
                        m_max_ser.getData().add(new XYChart.Data<>(m_mut_num, m_max));

                        Platform.runLater(() -> {
                            m_mean_ser.setName(MEAN_STR + Double.toString(m_mean));
                            m_mdev_ser.setName(MDEV_STR + Double.toString(req_mmd));
                            m_pdev_ser.setName(PDEV_STR + Double.toString(req_mpd));
                            m_max_ser.setName(MAX_STR + Double.toString(m_max));
                        });

                        m_is_data = false;
                        m_prev = now;
                    }
                } else {
                    m_prev = now;
                }

                //Trancate the data if there is too much
                if (m_mean_ser.getData().size() > MAX_DATA_SIZE) {
                    m_mean_ser.getData().remove(NODE_REMOVE_IDX);
                    m_mdev_ser.getData().remove(NODE_REMOVE_IDX);
                    m_pdev_ser.getData().remove(NODE_REMOVE_IDX);
                    m_max_ser.getData().remove(NODE_REMOVE_IDX);
                }

                //PopulationChart.this.updateBounds();
                FitnessChart.this.updateAxisRange();
            }
        }
    }

    private final AnchorPane m_anchor_pane;
    private final String m_name;
    private final XYChart.Series<Number, Number> m_mean_ser;
    private final XYChart.Series<Number, Number> m_mdev_ser;
    private final XYChart.Series<Number, Number> m_pdev_ser;
    private final XYChart.Series<Number, Number> m_max_ser;
    private final ChartAnimation m_animation;

    private boolean m_is_data;
    private double m_mean;
    private double m_dev;
    private double m_max;
    private long m_mut_num;

    public FitnessChart(final AnchorPane anchor_pane, final String name) {
        super(new NumberAxis("mutants count", 0.0, 1.0, 1.0),
                new NumberAxis("fitness", -0.2, 1.2, 0.1));
        this.m_is_data = false;
        this.m_mean = 0.0;
        this.m_dev = 0.0;
        this.m_mut_num = 0;
        this.m_max = 0.0;

        this.m_anchor_pane = anchor_pane;
        this.m_name = name;
        this.m_animation = new ChartAnimation();

        this.setTitle("Fitness " + this.m_name);

        this.m_mean_ser = new XYChart.Series<>();
        this.m_mdev_ser = new XYChart.Series<>();
        this.m_pdev_ser = new XYChart.Series<>();
        this.m_max_ser = new XYChart.Series<>();

        this.m_mean_ser.setName(MEAN_STR);
        this.m_mdev_ser.setName(MDEV_STR);
        this.m_pdev_ser.setName(PDEV_STR);
        this.m_max_ser.setName(MAX_STR);

        this.setCreateSymbols(false);
        this.setAnimated(false);
        this.getData().add(m_mean_ser);
        this.getData().add(m_mdev_ser);
        this.getData().add(m_pdev_ser);
        this.getData().add(m_max_ser);

        this.getXAxis().setAnimated(false);
        this.getXAxis().setAutoRanging(true);
        this.getYAxis().setAnimated(false);
        this.getYAxis().setAutoRanging(true);
        ((NumberAxis) this.getXAxis()).forceZeroInRangeProperty().set(false);
        ((NumberAxis) this.getYAxis()).forceZeroInRangeProperty().set(false);
    }

    /**
     * Allows to schedule the population chart update
     *
     * @param data the array storing mean, deviation and maximum fitness
     */
    public synchronized void schedule_update(final double[] data) {
        LOGGER.log(Level.FINE, "mean={0}, dev={1}, max={2}",
                new Object[]{data[0], data[1], data[2]});

        this.m_mut_num += 1;
        this.m_is_data = true;

        this.m_mean = data[0];
        this.m_dev = data[1];
        this.m_max = data[2];
    }

    /**
     * Starts drawing the chart updates
     */
    public void start() {
        m_animation.start();
    }

    /**
     * Stops drawing the chart updates
     */
    public void stop() {
        //Stop the times
        m_animation.stop();
        //Handle the last pieces
        m_animation.handle(Long.MAX_VALUE);
    }

    /**
     * Notifies that this chart should be made visible
     */
    public void set_active() {
        m_anchor_pane.getChildren().clear();
        m_anchor_pane.getChildren().add(this);
        AnchorPane.setTopAnchor(this, 0.0);
        AnchorPane.setLeftAnchor(this, 0.0);
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setBottomAnchor(this, 0.0);
    }
}
