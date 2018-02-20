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

import java.util.stream.IntStream;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.animation.AnimationTimer;
import javafx.scene.control.ScrollPane;

/**
 * This class represents the population grid on which the population is growing
 *
 * @author Dr. Ivan S. Zapreev
 */
public class GridView extends ScrollPane {

    /**
     * The graphics animation that allows to update the canvas with the new
     * elements from the fitness update queue
     */
    private class GridAnimation extends AnimationTimer {

        private static final long MIN_UPD_INTERVAL = (long) (1.0e9 / 24.0);
        private static final double TARGET_RANGE = 0.9;
        private static final double TARGET_RANGE_LFT = (1.0 - TARGET_RANGE) / 2;
        private static final double TARGET_RANGE_RGT = 1 - TARGET_RANGE_LFT;

        private long m_prev = 0;
        private final GraphicsContext m_2d_graph;
        private double m_min_ftn = Double.MAX_VALUE;
        private double m_max_ftn = 0.0;

        public GridAnimation() {
            m_2d_graph = m_canvas.getGraphicsContext2D();
        }

        /**
         * Fills the given cell with the given fitness color
         *
         * @param pos_x x position
         * @param pos_y y position
         * @param fitness the fitness color
         */
        private void fill_with_color(final int pos_x, final int pos_y, final double fitness) {
            m_2d_graph.setFill(get_color(fitness));
            m_2d_graph.fillRect(pos_x * MIN_SIZE + (pos_x + 1) * SPACING,
                    pos_y * MIN_SIZE + (pos_y + 1) * SPACING,
                    MIN_SIZE, MIN_SIZE);
        }

        @Override
        public void handle(long now) {
            if (m_prev != 0) {
                if ((now - m_prev) >= MIN_UPD_INTERVAL) {
                    final double mm_delta = Math.abs(m_max_ftn - m_min_ftn);
                    final double scale = TARGET_RANGE / mm_delta;
                    final double mm_mid = (m_max_ftn + m_min_ftn) / 2.0;
                    final double shift = 0.5 - scale * mm_mid;
                    m_min_ftn = Double.MAX_VALUE;
                    m_max_ftn = 0.0;
                    IntStream.range(0, m_size_x).forEach(pos_x -> {
                        IntStream.range(0, m_size_y).forEach(pos_y -> {
                            double fitness = get_fitness_update(pos_x, pos_y);
                            if (fitness != UNDEF_VALUE_FTN) {
                                if (fitness != EMPTY_CELL_FTN) {
                                    //Draw relative to the minimum fitness value
                                    //so that we always have a vivid picture
                                    if (fitness < 1.0) {
                                        final double adj_ftn = fitness * scale + shift;
                                        if (adj_ftn >= TARGET_RANGE_LFT && adj_ftn <= TARGET_RANGE_RGT) {
                                            fill_with_color(pos_x, pos_y, adj_ftn);
                                        }
                                    } else {
                                        fill_with_color(pos_x, pos_y, fitness);
                                    }
                                    m_min_ftn = Math.min(m_min_ftn, fitness);
                                    m_max_ftn = Math.max(m_max_ftn, fitness);
                                } else {
                                    fill_with_color(pos_x, pos_y, EMPTY_CELL_FTN);
                                }
                            }
                        });
                    });
                    m_prev = now;
                }
            } else {
                m_prev = now;
            }
        }
    }

    private static final int SPACING = 1;
    private static final int MIN_SIZE = 4;
    private static final double UNDEF_VALUE_FTN = -1.0;
    private static final double EMPTY_CELL_FTN = -2.0;

    private final int m_size_x;
    private final int m_size_y;
    private final double[][] m_fit_grid;
    private final Canvas m_canvas;
    private final GridAnimation m_animation;

    public GridView(final int size_x, final int size_y) {
        this.m_size_x = size_x;
        this.m_size_y = size_y;
        this.m_canvas = new Canvas();
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setContent(this.m_canvas);
        this.m_animation = new GridAnimation();
        m_fit_grid = new double[size_x][];
        IntStream.range(0, size_x).forEach(pos_x -> {
            m_fit_grid[pos_x] = new double[size_y];
            IntStream.range(0, size_y).forEach(pos_y -> {
                m_fit_grid[pos_x][pos_y] = UNDEF_VALUE_FTN;
            });
        });
        draw_initial(size_x, size_y);
    }

    /**
     * Allows to pick up a fitness update
     *
     * @param pos_x the x position
     * @param pos_y the y position
     * @return the stored fitness update value or UNDEF_FITNESS if none
     */
    private double get_fitness_update(final int pos_x, final int pos_y) {
        synchronized (m_fit_grid[pos_x]) {
            return m_fit_grid[pos_x][pos_y];
        }
    }

    /**
     * Allows to set a new fitness update value
     *
     * @param pos_x the x position
     * @param pos_y the y position
     * @param fitness the new fitness value
     */
    public void schedule_update(final int pos_x, final int pos_y, final double fitness) {
        synchronized (m_fit_grid[pos_x]) {
            m_fit_grid[pos_x][pos_y] = fitness;
        }
    }

    /**
     * Starts drawing the grid updates
     */
    public void start() {
        m_animation.start();
    }

    /**
     * Stops drawing the grid updates
     */
    public void stop() {
        //Stop the times
        m_animation.stop();
        //Handle the last pieces
        m_animation.handle(Long.MAX_VALUE);
    }

    /**
     * Allows to get cell color based on its fitness value.
     *
     * @param fitness the fitness [0,1] or negative if no element
     * @return the color
     */
    private Color get_color(final double fitness) {
        if (fitness < 0.0) {
            return Color.gray(0.99, 1.0);
        } else {
            if (fitness >= 1.0) {
                return Color.color(0.0, 0.0, 1.0);
            } else {
                return Color.color((1 - fitness), fitness, 0.0);
            }
        }
    }

    /**
     * Put a new fitness value into a grid cell
     *
     * @param pos_x the position in x
     * @param pos_y the position in y
     * @param ftn the fitness value, must be &ge; 0.0
     */
    public void set_fitness(final int pos_x, final int pos_y, final double ftn) {
        schedule_update(pos_x, pos_y, ftn);
    }

    /**
     * Remove a fitness value from a grid cell
     *
     * @param pos_x the position in x
     * @param pos_y the position in y
     */
    public void clear_fitness(final int pos_x, final int pos_y) {
        schedule_update(pos_x, pos_y, EMPTY_CELL_FTN);
    }

    /**
     * Allows to draw the initial grid
     *
     * @param size_x the grid x size
     * @param size_y the grid y size
     */
    private void draw_initial(final int size_x, final int size_y) {
        final int right = (int) snappedRightInset();
        final int left = (int) snappedLeftInset();
        final int top = (int) snappedTopInset();
        final int bottom = (int) snappedBottomInset();
        final int w = (int) (getWidth() - left - right);
        final int h = (int) (getHeight() - top - bottom);
        m_canvas.setLayoutX(left);
        m_canvas.setLayoutY(top);

        //Make sure we get enough space for all the elements
        int cellW = (w - (size_x + 1) * SPACING) / size_x;
        cellW = (cellW < MIN_SIZE ? MIN_SIZE : cellW);
        final int reqW = cellW * size_x + (size_x + 1) * SPACING;
        int cellH = (h - (size_y + 1) * SPACING) / size_y;
        cellH = (cellH < MIN_SIZE ? MIN_SIZE : cellH);
        final int reqH = cellH * size_y + (size_y + 1) * SPACING;

        m_canvas.setWidth(w < reqW ? reqW : w);
        m_canvas.setHeight(h < reqH ? reqH : h);
        GraphicsContext g = m_canvas.getGraphicsContext2D();
        g.setFill(Color.gray(0.0));
        g.fillRect(0, 0, reqW, reqH);

        for (int idx_x = 0; idx_x < size_x; idx_x++) {
            for (int idx_y = 0; idx_y < size_y; idx_y++) {
                //Draw an empty cell
                schedule_update(idx_x, idx_y, EMPTY_CELL_FTN);
            }
        }
    }
}
