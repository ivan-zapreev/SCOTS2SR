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
package nl.tudelft.dcsc.scots2sr;

import java.io.BufferedWriter;
import nl.tudelft.dcsc.scots2sr.jni.ScotsFacade;
import nl.tudelft.dcsc.scots2sr.ui.DofVisualizer;
import nl.tudelft.dcsc.scots2sr.ui.FitnessChart;
import nl.tudelft.dcsc.scots2sr.ui.GridView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.IntStream;

import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import nl.tudelft.dcsc.scots2jni.FConfig;
import nl.tudelft.dcsc.sr2jlib.fitness.FitnessType;
import nl.tudelft.dcsc.sr2jlib.grid.Individual;
import nl.tudelft.dcsc.sr2jlib.ProcessManagerConfig;
import nl.tudelft.dcsc.sr2jlib.ProcessManager;
import nl.tudelft.dcsc.sr2jlib.SelectionType;
import nl.tudelft.dcsc.sr2jlib.fitness.FitnessManager;
import nl.tudelft.dcsc.sr2jlib.grammar.GrammarConfig;
import nl.tudelft.dcsc.sr2jlib.grammar.Grammar;

public class FXMLController implements Initializable {

    private static class Pair<kT, vT> {

        public final kT m_first;
        public final vT m_second;

        public Pair(final kT first, final vT second) {
            m_first = first;
            m_second = second;
        }
    }

    //Stores the library file property name
    private static final String LIB_FILE_NAME_PROP = "Native Library Name";
    //Stores the symbolic controller file name extension
    private static final String SYM_FILE_NAME_EXT = "sym";
    //Stores the symbolic controller file name template
    private static final String SYM_FILE_TEMPL = "*." + SYM_FILE_NAME_EXT;

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(FXMLController.class.getName());

    //Defines the number of UI threads to be used 
    private static final int NUM_UI_WORK_THREADS = 3;

    @FXML
    private Button m_load_btn;
    @FXML
    private Button m_run_btn;
    @FXML
    private Button m_stop_btn;
    @FXML
    private Button m_save_btn;

    @FXML
    private TextArea m_ctrl_name_txt;
    @FXML
    private ComboBox<String> m_dims_cmb;
    @FXML
    private TextField m_max_pop_size_txt;
    @FXML
    private TextField m_max_mut_txt;
    @FXML
    private TextField m_max_tree_size_txt;
    @FXML
    private TextField m_workers_dof_txt;
    @FXML
    private TextField m_ftn_scale_txt;
    @FXML
    private TextField m_ch_sp_x_txt;
    @FXML
    private TextField m_ch_sp_y_txt;
    @FXML
    private Slider m_ch_vs_rep_sld;
    @FXML
    private Slider m_tm_vs_tnm_sld;
    @FXML
    private Slider m_init_pop_sld;
    @FXML
    private TextArea m_grammar_txt;
    @FXML
    private TextField m_min_ngf_txt;
    @FXML
    private TextField m_max_ngf_txt;
    @FXML
    private TextField m_attract_txt;
    @FXML
    private ComboBox<SelectionType> m_tour_cmb;
    @FXML
    private ComboBox<FitnessType> m_fit_cmb;
    @FXML
    private CheckBox m_is_stop_cbx;
    @FXML
    private CheckBox m_is_iter_cbx;
    @FXML
    private CheckBox m_is_prop_pn_cbx;
    @FXML
    private CheckBox m_is_scale_cbx;
    @FXML
    private CheckBox m_is_compl_cbx;
    @FXML
    private CheckBox m_is_child_lim_cbx;
    @FXML
    private CheckBox m_is_avoid_equal_cbx;
    @FXML
    private TextField m_min_ch_cnt_txt;
    @FXML
    private TextField m_max_ch_cnt_txt;
    @FXML
    private TextField m_max_gd_txt;

    @FXML
    private AnchorPane m_req_ftn_pane;
    @FXML
    private AnchorPane m_ex_ftn_pane;
    @FXML
    private StackedAreaChart m_req_ftn_crt;
    @FXML
    private StackedAreaChart m_ex_ftn_crt;
    @FXML
    private TabPane m_dof_tab;
    @FXML
    private ProgressBar m_main_prog_ind;
    @FXML
    private HBox m_progress_box;

    //Stores the cached number of iterations
    private String m_max_mut_val;
    //Stores the number of loaded controller dofs
    private int m_num_dofs;

    //Stores the properties file
    private final File m_props_file;
    //Stores the properties
    private final Properties m_props;

    public FXMLController() {
        m_max_mut_val = "300000";
        m_num_dofs = 0;
        m_props_file = new File("config.properties");
        m_props = new Properties();
    }

    /**
     * Allows to load the properties from file
     */
    private void load_properties() {
        try {
            try (FileReader reader = new FileReader(m_props_file)) {
                m_props.load(reader);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Unable to load config file: {0}",
                    m_props_file.getName());
        }
    }

    /**
     * Allows to store properties into file
     */
    private void save_properties() {
        try {
            try (FileWriter writer = new FileWriter(m_props_file)) {
                m_props.store(writer, "Settings");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Unable to save config file: "
                    + m_props_file.getName(), ex);
        }
    }

    /**
     * Is to be called when the application is being stopped
     */
    public void finish() {
        //Stop the processes
        m_executor.shutdownNow();
        enable_ctrls_load(false, true);
        enable_ctrls_run(false, true, true);
        //Store properties
        save_properties();
        //Close the handlers
        stop_logging();
    }

    //The executor to handle parallel tasks
    private final ExecutorService m_executor = Executors.newFixedThreadPool(NUM_UI_WORK_THREADS);

    private void enable_ctrls_safe(final boolean is_start) {
        m_load_btn.setDisable(is_start);
        m_run_btn.setDisable(is_start);
        m_main_prog_ind.setVisible(is_start);
        m_save_btn.setDisable(is_start);
        m_stop_btn.setDisable(true);

        enable_non_btn_ctrls(is_start);

        m_dims_cmb.setDisable(is_start);
    }

    private void enable_ctrls_load(final boolean is_start, final boolean is_ok) {
        m_load_btn.setDisable(is_start);
        m_run_btn.setDisable(is_start || !is_ok);
        m_main_prog_ind.setVisible(is_start);
        m_save_btn.setDisable(true);
        m_stop_btn.setDisable(true);

        enable_non_btn_ctrls(is_start);

        m_dims_cmb.setDisable(is_start || !is_ok);
    }

    private void stop_logging() {
        Logger global_logger = Logger.getLogger("");
        Handler[] handlers = global_logger.getHandlers();
        for (Handler handler : handlers) {
            handler.close();
            global_logger.removeHandler(handler);
        }
    }

    private void start_logging(final String file_name) {
        final String full_file_name = file_name + ".gp.log";
        try {
            FileHandler fh = new FileHandler(full_file_name);
            Logger global_logger = Logger.getLogger("");
            LogManager.getLogManager().reset();
            Handler[] handlers = global_logger.getHandlers();
            for (Handler handler : handlers) {
                handler.close();
                global_logger.removeHandler(handler);
            }
            SimpleFormatter fmt = new SimpleFormatter();
            fh.setFormatter(fmt);
            global_logger.addHandler(fh);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not open log file: " + full_file_name, ex);
        }
    }

    /**
     * Allows to update the main progress bar
     *
     * @param value the new value
     */
    private void update_main_progress(final double value) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                m_main_prog_ind.setProgress(value);
            }
        });
    }

    /**
     * Allows to get the smallest individual text from the list of individuals.
     *
     * @param inds the list of individuals
     * @return the pair of individual and its prepared string representation.
     * @throws IllegalStateException in case the list of individuals is empty
     */
    private Pair<Individual, String> get_best_ind(List<Individual> inds) throws IllegalStateException {
        String min_ind_str = "";
        Individual min_ind = null;
        update_main_progress(0.0);
        LOGGER.log(Level.FINE, "Got {0} individuals", inds.size());
        if (inds.isEmpty()) {
            throw new IllegalStateException("The best individuals list is emty!");
        } else {
            for (int ind_idx = 0; ind_idx < inds.size(); ++ind_idx) {
                //Get the individual
                final Individual ind = inds.get(ind_idx);

                //Optimize the individual
                LOGGER.log(Level.INFO, "Optimizing candidage individual {0}/{1}: {2}",
                        new Object[]{(ind_idx + 1), inds.size(),
                            ind.get_expr_list().get(0).to_text()});
                ind.optimize();

                //Update progress indicator
                update_main_progress(((double) (ind_idx + 1)) / ((double) inds.size()));

                //Get the textual representatio
                final String ind_str = ind.get_expr_list().get(0).to_text();
                //Choose the shortest representation
                if (min_ind_str.isEmpty()) {
                    min_ind_str = ind_str;
                    min_ind = ind;
                } else {
                    final String ind_str_trm = ind_str.trim();
                    if (min_ind_str.length() > ind_str_trm.length()) {
                        min_ind_str = ind_str_trm;
                        min_ind = ind;
                    }
                }
            }
        }
        update_main_progress(-1.0);
        return new Pair<>(min_ind, min_ind_str);
    }

    private void start_saving(final String ctrl_file_name) {
        enable_ctrls_safe(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    List<Individual> inds = new ArrayList<>();
                    //Go through the Managers, get the best fit controllers
                    //save the symbolic controllers into a text file
                    Path file_path = Paths.get(ctrl_file_name);
                    try (BufferedWriter writer = Files.newBufferedWriter(file_path)) {
                        for (ProcessManager mgr : m_managers) {
                            LOGGER.log(Level.FINE, "Getting the dof's best fit individuals");
                            final Pair<Individual, String> result = get_best_ind(mgr.get_best_fit_ind());
                            final String candidate = result.m_second;
                            LOGGER.log(Level.FINE, "The shortest one is {0}", candidate);
                            writer.write(candidate + "\n");
                            LOGGER.log(Level.FINE, "The individual is stored");
                            writer.flush();
                            //Store the individual for unfit points extraction
                            inds.add(result.m_first);
                        }
                    }
                    //Get the symbolic controller file name without extension
                    final String bad_file_name = ctrl_file_name.replaceAll(
                            "\\." + SYM_FILE_NAME_EXT, "");
                    //Store the unsafe points as a BDD.
                    ((ScotsFacade) FitnessManager.inst()).store_unfit_points(bad_file_name, inds);
                    //Re-enable the buttons
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            enable_ctrls_safe(false);
                        }
                    });
                } catch (FileNotFoundException | IllegalStateException ex) {
                    final String msg = ex.getMessage();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert alert = new Alert(AlertType.ERROR,
                                    "Failed string the controller: " + msg);
                            alert.show();
                            enable_ctrls_safe(false);
                        }
                    });
                }
                return null;
            }
        };
        m_executor.submit(task);
    }

    private void start_loading(final String full_file_name) {
        enable_ctrls_load(true, false);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final String file_name = full_file_name.replaceFirst("[.][^.]+$", "");
                    start_logging(file_name);
                    m_num_dofs = ScotsFacade.INSTANCE.load(file_name);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            m_ctrl_name_txt.setText(full_file_name);
                            m_dims_cmb.getItems().clear();
                            m_dof_tab.getTabs().clear();
                            m_req_ftn_pane.getChildren().clear();
                            m_req_ftn_pane.getChildren().add(m_req_ftn_crt);
                            m_ex_ftn_pane.getChildren().clear();
                            m_ex_ftn_pane.getChildren().add(m_ex_ftn_crt);
                            IntStream.range(1, m_num_dofs).forEachOrdered(
                                    nbr -> m_dims_cmb.getItems().add(nbr - 1, Integer.toString(nbr)));
                            enable_ctrls_load(false, true);
                        }
                    });
                } catch (FileNotFoundException | IllegalStateException ex) {
                    final String msg = ex.getMessage();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert alert = new Alert(AlertType.ERROR,
                                    "Failed loading the controller: " + msg);
                            alert.show();
                            enable_ctrls_load(false, false);
                        }
                    });
                }
                return null;
            }
        };
        m_executor.submit(task);
    }

    //Stores the list of managers
    private final List<ProcessManager> m_managers = new ArrayList<>();
    //Stores the list of active managers
    private final List<ProcessManager> m_managers_act = new ArrayList<>();

    private void enable_non_btn_ctrls(final boolean is_start) {
        //m_ctrl_name_txt.setDisable(is_start);
        m_dims_cmb.setDisable(is_start);
        if (!m_is_iter_cbx.isSelected()) {
            m_max_mut_txt.setDisable(is_start);
        }
        m_max_tree_size_txt.setDisable(is_start);
        m_max_pop_size_txt.setDisable(is_start);
        m_grammar_txt.setDisable(is_start);
        m_ch_vs_rep_sld.setDisable(is_start);
        m_tm_vs_tnm_sld.setDisable(is_start);
        m_init_pop_sld.setDisable(is_start);
        m_workers_dof_txt.setDisable(is_start);
        m_tour_cmb.setDisable(is_start);
        m_is_stop_cbx.setDisable(is_start);
        m_is_iter_cbx.setDisable(is_start);
        m_min_ngf_txt.setDisable(is_start);
        m_max_ngf_txt.setDisable(is_start);
        if (m_is_compl_cbx.isSelected()) {
            m_fit_cmb.setDisable(is_start);
            m_attract_txt.setDisable(is_start);
            m_ftn_scale_txt.setDisable(is_start);
        }
        m_is_prop_pn_cbx.setDisable(is_start);
        m_is_scale_cbx.setDisable(is_start);
        m_is_compl_cbx.setDisable(is_start);
        m_is_child_lim_cbx.setDisable(is_start);
        m_is_avoid_equal_cbx.setDisable(is_start);
        if (m_is_child_lim_cbx.isSelected()) {
            m_min_ch_cnt_txt.setDisable(is_start);
            m_max_ch_cnt_txt.setDisable(is_start);
        }
        m_max_gd_txt.setDisable(is_start);
        m_ch_sp_x_txt.setDisable(is_start);
        m_ch_sp_y_txt.setDisable(is_start);
    }

    /**
     * Disables the interface when started running GP
     */
    private void enable_ctrls_run(final boolean is_start, final boolean is_ok, final boolean is_stop_mgrs) {
        m_load_btn.setDisable(is_start);
        m_run_btn.setDisable(is_start);
        m_stop_btn.setDisable(!is_start);
        m_save_btn.setDisable(is_start || !is_ok);

        enable_non_btn_ctrls(is_start);

        if (!is_start && is_stop_mgrs) {
            m_managers.forEach((manager) -> {
                manager.stop(true);
            });
        }
    }

    @FXML
    public void stopRunning(ActionEvent event) {
        enable_ctrls_run(false, true, true);
    }

    /**
     * Counts the number of finished dofs identified by population managers
     *
     * @param mgr
     */
    private synchronized void count_finished_dofs(final ProcessManager mgr) {
        m_managers_act.remove(mgr);
        if (m_managers_act.isEmpty()) {
            enable_ctrls_run(false, true, false);
        }
    }

    /**
     * Configures system parameters
     *
     * @return the instantiated grammar
     * @throws Exception
     */
    private Grammar configure_parameters()
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Grammar grammar = null;
        final Object value = m_dims_cmb.getValue();
        if (value != null) {
            //Set up the parameters and the grammar
            m_main_prog_ind.setVisible(true);
            m_stop_btn.setDisable(true);
            try {
                //Get the parameter values
                final int num_ss_dofs = Integer.parseInt(value.toString());
                final int fitness_type = ((FitnessType) m_fit_cmb.getValue()).get_uid();
                final double attr_size = Double.parseDouble(m_attract_txt.getText());
                final double ftn_scale = Double.parseDouble(m_ftn_scale_txt.getText());
                final boolean is_scale = m_is_scale_cbx.isSelected();
                final boolean is_complex = m_is_compl_cbx.isSelected();

                //Talk to Scots via interface
                final FConfig f_cfg = new FConfig(num_ss_dofs, fitness_type,
                        attr_size, ftn_scale, is_scale, is_complex);
                ScotsFacade.INSTANCE.configure(f_cfg);

                //Set the grammar
                final GrammarConfig g_cfg = new GrammarConfig(m_grammar_txt.getText(),
                        Integer.parseInt(m_max_tree_size_txt.getText()),
                        m_ch_vs_rep_sld.getValue(), num_ss_dofs,
                        Double.parseDouble(m_min_ngf_txt.getText()),
                        Double.parseDouble(m_max_ngf_txt.getText()),
                        m_is_prop_pn_cbx.isSelected(),
                        Integer.parseInt(m_max_gd_txt.getText()),
                        m_tm_vs_tnm_sld.getValue());
                grammar = Grammar.create_grammar(g_cfg);
            } finally {
                m_main_prog_ind.setVisible(false);
                m_stop_btn.setDisable(false);
            }
        } else {
            throw new IllegalArgumentException("The number of state-space dimensions is not set!");
        }
        return grammar;
    }

    /**
     * Starts the symbolic regression process
     *
     * @param grammar the grammar to be used for all the managers
     * @throws Exception
     */
    private void start_process(final Grammar grammar) {
        //Extract the data from the interface controls
        final String sizes[] = m_max_pop_size_txt.getText().split("x");
        final Object value = m_dims_cmb.getValue();
        final int num_ss_dofs = Integer.parseInt(value.toString());
        final int size_x = Integer.parseInt(sizes[0]);
        final int size_y = Integer.parseInt(sizes[1]);
        final int ch_sp_x = Integer.parseInt(m_ch_sp_x_txt.getText());
        final int ch_sp_y = Integer.parseInt(m_ch_sp_y_txt.getText());
        final int num_workers = Integer.parseInt(m_workers_dof_txt.getText());
        final long max_mutations = Long.parseLong(m_max_mut_txt.getText());
        final double init_pop_mult = m_init_pop_sld.getValue();
        final boolean is_stop_found = m_is_stop_cbx.isSelected();
        final SelectionType sel_type = (SelectionType) m_tour_cmb.getValue();
        final boolean is_child_limit = m_is_child_lim_cbx.isSelected();
        final boolean is_avoid_equal = m_is_avoid_equal_cbx.isSelected();
        final int min_ch_cnt = Integer.parseInt(m_min_ch_cnt_txt.getText());
        final int max_ch_cnt = Integer.parseInt(m_max_ch_cnt_txt.getText());
        //Prepare grammars
        Grammar.clear_grammars();
        IntStream.range(0, (m_num_dofs - num_ss_dofs)).forEachOrdered(mgr_id -> {
            //Register the grammar for the given manager, the dof index 
            //is always 0 as there is on input signal dimension per manager
            Grammar.register_grammar(mgr_id, 0, grammar);
        });
        Grammar.prepare_grammars();
        //Constructe the interface elements
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                m_managers.clear();
                m_dof_tab.getTabs().clear();
                m_progress_box.getChildren().clear();
                IntStream.range(0, (m_num_dofs - num_ss_dofs)).forEachOrdered(mgr_id -> {
                    //Instantiaet the visualizer
                    final String dof_title = "Dof #" + mgr_id;
                    Tab dofTab = new Tab(dof_title);
                    GridView grid_pane = new GridView(size_x, size_y);
                    FitnessChart req_ftn_chart = new FitnessChart(m_req_ftn_pane, dof_title + "(Complex)");
                    FitnessChart ex_ftn_chart = new FitnessChart(m_ex_ftn_pane, dof_title + "(Exact)");
                    final DofVisualizer visualizer = new DofVisualizer(
                            size_x, size_y, m_progress_box, dof_title,
                            m_main_prog_ind.getPrefHeight(), m_main_prog_ind.getPrefHeight(),
                            grid_pane, req_ftn_chart, ex_ftn_chart) {
                        @Override
                        public synchronized void set(final Individual ind) {
                            //Call the super class method first
                            super.set(ind);
                            //Check if we need to stop
                            if (is_stop_found && (ind.get_fitness().is_one())) {
                                //Stop the manager
                                stop_manager_found(mgr_id);
                            }
                        }
                    };
                    //Instantiate the process manager
                    final ProcessManagerConfig config = new ProcessManagerConfig(
                            mgr_id, init_pop_mult, num_workers, max_mutations,
                            1, size_x, size_y, ch_sp_x, ch_sp_y, sel_type,
                            is_child_limit, is_avoid_equal, min_ch_cnt, max_ch_cnt,
                            visualizer, (mgr) -> {
                                count_finished_dofs(mgr);
                            });
                    final ProcessManager manager = new ProcessManager(config);
                    //Add the population filter: Experimental
                    /*final AvgFilter filter = new AvgFilter(1.0e-4, 300000);
                    visualizer.set_ftn_change_listener(new DofVisualizer.FitnessChange() {
                        @Override
                        public void change(double[] req_ftn, double[] ex_ftn) {
                            m_executor.submit(new Runnable() {
                                @Override
                                public void run() {
                                    if (filter.is_trigger(ex_ftn[1])) {
                                        filter.set_ftn_bound(ex_ftn[0]);
                                        filter.start_filtering();
                                        manager.filter_individuals(filter);
                                        filter.stop_filtering();
                                    }
                                }
                            });
                        }
                    });*/
                    m_managers.add(manager);
                    m_managers_act.add(manager);
                    dofTab.setContent(grid_pane);
                    dofTab.setOnSelectionChanged(ev -> {
                        visualizer.set_active(dofTab.isSelected());
                    });
                    m_dof_tab.getTabs().add(dofTab);
                    //Start the manager
                    manager.start();
                });
            }
        });
    }

    /**
     * Allows to stop the manager at the given index
     *
     * @param mgr_idx the manager to be softly stopped
     */
    private void stop_manager_found(final int mgr_idx) {
        m_managers.get(mgr_idx).stop(true);
    }

    @FXML
    public void startRunning(ActionEvent event) {
        enable_ctrls_run(true, true, false);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String msg = null;
                try {
                    final Grammar grammar = configure_parameters();
                    start_process(grammar);
                } catch (IllegalAccessException | InvocationTargetException
                        | IllegalArgumentException | IllegalStateException ex) {
                    LOGGER.log(Level.SEVERE, "Faled to start execution!", ex);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            enable_ctrls_run(false, false, false);
                            final Alert alert = new Alert(AlertType.ERROR,
                                    "Faled to start execution: " + ex.getMessage());
                            alert.showAndWait();
                        }
                    });
                }
                return null;
            }
        };
        m_executor.submit(task);
    }

    @FXML
    public void saveFileSelection(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Controller File");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Symbolic controller", SYM_FILE_TEMPL);
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(m_save_btn.getScene().getWindow());
        if (file != null) {
            start_saving(file.getPath());
        }
    }

    @FXML
    public void openFileSelection(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Controller File");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SCOTSv2.0 controller", "*.scs");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(m_load_btn.getScene().getWindow());
        if (file != null) {
            start_loading(file.getPath());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Load properties
        load_properties();

        m_main_prog_ind.setProgress(-1.0);
        m_main_prog_ind.setVisible(false);
        m_progress_box.setAlignment(Pos.CENTER_LEFT);

        m_tour_cmb.getItems().add(SelectionType.VALUE.get_idx(),
                SelectionType.VALUE);
        m_tour_cmb.getItems().add(SelectionType.PROB.get_idx(),
                SelectionType.PROB);
        m_tour_cmb.getSelectionModel().selectFirst();

        m_fit_cmb.getItems().add(FitnessType.EXACT);
        m_fit_cmb.getItems().add(FitnessType.ATANG);
        m_fit_cmb.getItems().add(FitnessType.INVER);
        m_fit_cmb.valueProperty().addListener(new ChangeListener<FitnessType>() {
            @Override
            public void changed(ObservableValue ov, FitnessType old_val, FitnessType new_val) {
                final String old_str = m_ftn_scale_txt.getText();
                if ((old_val != null) && !old_str.trim().isEmpty()) {
                    old_val.set_scaling(Double.parseDouble(old_str));
                }
                m_ftn_scale_txt.setText(Double.toString(new_val.get_scaling()));
            }
        });
        m_fit_cmb.getSelectionModel().select(FitnessType.INVER);

        //Add change listener
        m_max_mut_txt.setText(Long.toString(Long.MAX_VALUE));
        m_is_iter_cbx.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (m_is_iter_cbx.isSelected()) {
                    m_max_mut_txt.setDisable(true);
                    m_max_mut_val = m_max_mut_txt.getText();
                    m_max_mut_txt.setText(Long.toString(Long.MAX_VALUE));
                } else {
                    m_max_mut_txt.setDisable(false);
                    m_max_mut_txt.setText(m_max_mut_val);
                }
            }
        });

        //Add listener
        m_is_child_lim_cbx.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (m_is_child_lim_cbx.isSelected()) {
                    m_min_ch_cnt_txt.setDisable(false);
                    m_max_ch_cnt_txt.setDisable(false);
                } else {
                    m_min_ch_cnt_txt.setDisable(true);
                    m_max_ch_cnt_txt.setDisable(true);
                }
            }
        });

        //Add listener
        m_is_compl_cbx.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (m_is_compl_cbx.isSelected()) {
                    m_fit_cmb.setDisable(false);
                    m_attract_txt.setDisable(false);
                } else {
                    m_fit_cmb.setDisable(true);
                    m_attract_txt.setDisable(true);
                }
            }
        });
    }

    /**
     * Allows to perform the initial checks.
     */
    public void after_show() {
        Platform.runLater(new Runnable() {
            private final FileChooser fileChooser = new FileChooser();
            private final FileChooser.ExtensionFilter extFilter
                    = new FileChooser.ExtensionFilter("SCOTS2DLL dynamic library",
                            "*.dylib", "*.dll", "*.so");

            {
                fileChooser.setTitle("Load Native Library");
                fileChooser.getExtensionFilters().add(extFilter);
            }

            @Override
            public void run() {
                //Check if the native library is defined
                String lib_file_name = m_props.getProperty(LIB_FILE_NAME_PROP);
                if (lib_file_name == null) {
                    final File file = fileChooser.showOpenDialog(m_load_btn.getScene().getWindow());
                    if (file != null) {
                        lib_file_name = file.getPath();
                        m_props.setProperty(LIB_FILE_NAME_PROP, lib_file_name);
                    } else {
                        //If the library is not selected then re-try
                        Platform.runLater(this);
                        return;
                    }
                }
                LOGGER.log(Level.INFO, "Loading the SCOTS2DLL "
                        + "dynamic library from {0}", lib_file_name);
                //Attempt loading the library
                if (ScotsFacade.INSTANCE.load_library(lib_file_name)) {
                    m_props.remove(LIB_FILE_NAME_PROP);
                    final Alert alert = new Alert(AlertType.ERROR,
                            "Faled loading the JNI dynamic library: "
                            + lib_file_name + ", please choose another one!");
                    alert.showAndWait();
                    //If loading failed then re-try
                    Platform.runLater(this);
                }
            }
        });
    }
}
