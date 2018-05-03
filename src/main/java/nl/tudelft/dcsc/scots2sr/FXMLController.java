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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.List;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import nl.tudelft.dcsc.scots2jni.FConfig;
import nl.tudelft.dcsc.scots2sr.ui.ConsoleLog;
import nl.tudelft.dcsc.scots2sr.utils.Pair;
import nl.tudelft.dcsc.sr2jlib.fitness.FitnessType;
import nl.tudelft.dcsc.sr2jlib.grid.Individual;
import nl.tudelft.dcsc.sr2jlib.ProcessManagerConfig;
import nl.tudelft.dcsc.sr2jlib.ProcessManager;
import nl.tudelft.dcsc.sr2jlib.SelectionType;
import nl.tudelft.dcsc.sr2jlib.fitness.Fitness;
import nl.tudelft.dcsc.sr2jlib.fitness.FitnessManager;
import nl.tudelft.dcsc.sr2jlib.grammar.GrammarConfig;
import nl.tudelft.dcsc.sr2jlib.grammar.Grammar;

/**
 * This is the main UI controller implementation
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class FXMLController implements Initializable {

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
    private CheckBox m_is_opt_on_save_cbx;
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

    @FXML
    private CheckBox m_mc_fitness_cbx;
    @FXML
    private CheckBox m_rss_ftn_cbx;
    @FXML
    private TextField m_rel_sam_size_txt;
    @FXML
    private TextField m_act_sam_size_txt;
    @FXML
    private TextField m_min_bis_size_txt;
    @FXML
    private Slider m_rss_bis_ratio_sld;
    @FXML
    private ListView<String> m_log_lst;

    //Stores the cached number of iterations
    private String m_max_mut_val;
    //Stores the number of loaded controller dofs
    private int m_num_dofs;
    //Stores the property manager
    private final PropertyManager m_prop_mgr;
    //Stores the info log
    private ConsoleLog m_log;

    //The executor to handle parallel tasks
    private final ExecutorService m_executor = Executors.newFixedThreadPool(NUM_UI_WORK_THREADS);

    //Stores the list of managers
    private final List<ProcessManager> m_managers = new ArrayList<>();
    //Stores the list of active managers
    private final List<ProcessManager> m_managers_act = new ArrayList<>();

    public FXMLController() {
        m_max_mut_val = "300000";
        m_num_dofs = 0;
        m_prop_mgr = new PropertyManager("config.properties");
        m_log = null;
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
        m_prop_mgr.save_properties();
        //Close the handlers
        stop_logging();
    }

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
        m_log.info("Started controller logging into: " + full_file_name);
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
            m_log.err("Could not open log file: " + full_file_name);
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
    private Pair<Individual, String> get_best_ind(final int mgr_id,
            List<Individual> inds) throws IllegalStateException {
        String min_ind_str = "";
        Individual min_ind = null;
        update_main_progress(-1.0);

        LOGGER.log(Level.FINE, "Got {0} individuals", inds.size());
        m_log.info("Started getting the best individual for dof: " + mgr_id);

        if (inds.isEmpty()) {
            m_log.err("There is no single individual available!");
            throw new IllegalStateException("The best individuals list is emty!");
        } else {
            m_log.info("The number of candidate individuals is: " + inds.size());
            for (int ind_idx = 0; ind_idx < inds.size(); ++ind_idx) {
                //Get the individual
                final Individual ind = inds.get(ind_idx);

                //Optimize the individual
                if (m_is_opt_on_save_cbx.isSelected()) {
                    LOGGER.log(Level.INFO, "Optimizing individual: {0}/{1})",
                            new Object[]{ind_idx + 1, inds.size()});
                    m_log.info("Started optimizing individual size for: "
                            + (ind_idx + 1) + "/" + inds.size());

                    final String ind_orig = ind.get_expr_list().get(0).to_text();
                    ind.optimize();
                    final String ind_opt = ind.get_expr_list().get(0).to_text();

                    LOGGER.log(Level.FINE, "Optimized:\n{0}\n---into---\n{1}",
                            new Object[]{ind_orig, ind_opt});
                    m_log.info("Finished optimizing individual size for: "
                            + (ind_idx + 1) + "/" + inds.size());
                }

                //Update progress indicator, only do it when the percentage is high enough
                double percentage = ((double) (ind_idx + 1)) / ((double) inds.size());
                if (percentage > 0.1) {
                    update_main_progress(percentage);
                }

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

        m_log.info("Finished getting the best individual for dof: " + mgr_id);
        return new Pair<>(min_ind, min_ind_str);
    }

    /**
     * Allows to convert the fitness values per dof to string
     *
     * @param ftn the array of fitness values per dof
     * @return the string representing the fitness values per dof
     */
    private static String fitness_to_string(final Fitness[] ftn) {
        String result = "";
        NumberFormat formatter = new DecimalFormat("#00.00");
        for (int idx = 0; idx < ftn.length; ++idx) {
            result += "Dof #" + idx + ": "
                    + formatter.format(ftn[idx].get_fitness() * 100) + "%"
                    + (idx == (ftn.length - 1) ? "" : "\n");
        }
        return result;
    }

    /**
     * Allows to show the resulting info for the exported dof controllers
     *
     * @param ftn the fitness objects per dof
     */
    private void show_resulting_info(final Fitness[] ftn) {
        //Construct the info message
        final String msg = "The resulting symbolic controller fitness"
                + " values per-dof are: \n" + fitness_to_string(ftn);
        m_log.info(msg);

        //Construct the UI elements
        final TextArea msg_area = new TextArea(msg);
        msg_area.setEditable(false);
        msg_area.setWrapText(true);
        final ScrollPane scr_pane = new ScrollPane(msg_area);
        final GridPane msg_pane = new GridPane();
        msg_pane.setMaxWidth(Double.MAX_VALUE);
        msg_pane.add(scr_pane, 0, 0);

        //Show the alert
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Finished exporting symbolic controllers");
        alert.getDialogPane().setContent(msg_pane);
        alert.initModality(Modality.NONE);
        alert.show();
    }

    /**
     * Allows to get the best fit individual for each dof
     *
     * @param inds the container of individual and its string representations
     */
    private void get_best_fint_inds(final List<Pair<Individual, String>> inds) {
        m_log.info("Started getting the best fit infividuals.");
        LOGGER.log(Level.INFO, "Getting the dof's best fit individuals");
        m_managers.forEach(mgr -> {
            //Store the individual for unfit points extraction
            inds.add(get_best_ind(mgr.get_mgr_id(), mgr.get_best_fit_ind()));
        });
        m_log.info("Finished getting the best fit infividuals.");
    }

    /**
     * Allows to store the symbolic controllers per dof into file along with
     * their fitness scores
     *
     * @param ctrl_file_name the file name to be used
     * @param inds the list of individual string pairs
     * @param ftn the fitness objects per individual
     * @throws IOException in case the file writing fails
     */
    private void store_symbolic_controllers(final String ctrl_file_name,
            final List<Pair<Individual, String>> inds,
            final Fitness[] ftn) throws IOException {
        m_log.info("Started saving symbolic controller: " + ctrl_file_name);
        //Save the symbolic controllers into a text file
        Path file_path = Paths.get(ctrl_file_name);
        try (final BufferedWriter writer = Files.newBufferedWriter(file_path)) {
            NumberFormat formatter = new DecimalFormat("#00.00");
            for (int idx = 0; idx < inds.size(); ++idx) {
                final Pair<Individual, String> pair = inds.get(idx);
                final String candidate = pair.m_second;
                LOGGER.log(Level.FINE, "The shortest one is {0}", candidate);
                writer.write("Dof #" + idx + ", "
                        + formatter.format(ftn[idx].get_fitness() * 100)
                        + "% fit controller: " + candidate + "\n");
                LOGGER.log(Level.FINE, "The individual is stored");
                writer.flush();
            }
        }
        m_log.info("Finished saving symbolic controller: " + ctrl_file_name);
    }

    /**
     * Allows to save the "best fit" symbolic controller per dof and export
     * their unfit points.
     *
     * @param ctrl_file_name the controller file name
     */
    private void start_saving(final String ctrl_file_name) {
        enable_ctrls_safe(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    //Obtain the best fit individuals
                    final List<Pair<Individual, String>> inds = new ArrayList<>();
                    get_best_fint_inds(inds);

                    //Get the symbolic controller file name without extension
                    final String bad_file_name = ctrl_file_name.replaceAll(
                            "\\." + SYM_FILE_NAME_EXT, "");
                    //Store the unsafe points as a BDD.
                    final ScotsFacade facade = (ScotsFacade) FitnessManager.inst();
                    m_log.info("Started storing controller's unfit points into: " + bad_file_name);
                    final Fitness[] ftn = facade.store_unfit_points(bad_file_name, inds);
                    m_log.info("Finished storing controller's unfit points into: " + bad_file_name);

                    //Store the symbolic controllers into files
                    store_symbolic_controllers(ctrl_file_name, inds, ftn);

                    //Show the end info and enable the buttons
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            //Show the resulting info
                            show_resulting_info(ftn);

                            //Enable the controls
                            enable_ctrls_safe(false);
                        }
                    });
                } catch (FileNotFoundException | IllegalStateException ex) {
                    final String msg = ex.getMessage();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            m_log.err("Failed saving symbolic controller: "
                                    + ctrl_file_name + ", error: " + msg);
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
                    m_log.info("Started loading controller: " + full_file_name);
                    final String file_name = full_file_name.replaceFirst("[.][^.]+$", "");
                    start_logging(file_name);
                    m_num_dofs = ScotsFacade.INSTANCE.load(file_name);
                    m_log.info("Finished loading controller: " + full_file_name);
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
                            m_log.err("Failed loading the controller: " + msg);
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

    private void enable_monte_carlo_ctrls(final boolean is_dis) {
        m_mc_fitness_cbx.setDisable(is_dis);
        if (m_mc_fitness_cbx.isSelected()) {
            m_rel_sam_size_txt.setDisable(is_dis);
            m_rss_ftn_cbx.setDisable(is_dis);
            if (m_rss_ftn_cbx.isSelected()) {
                m_min_bis_size_txt.setDisable(is_dis);
                m_rss_bis_ratio_sld.setDisable(is_dis);
            }
        }
    }

    private void enable_child_limit_ctrls(final boolean is_dis) {
        m_is_child_lim_cbx.setDisable(is_dis);
        if (m_is_child_lim_cbx.isSelected()) {
            m_min_ch_cnt_txt.setDisable(is_dis);
            m_max_ch_cnt_txt.setDisable(is_dis);
        }
    }

    private void enable_fitness_ctrls(final boolean is_dis) {
        m_is_compl_cbx.setDisable(is_dis);
        if (m_is_compl_cbx.isSelected()) {
            //If the complex fitness is selected then some of
            //its controls can be enabled, so we disable them
            m_fit_cmb.setDisable(is_dis);
            m_attract_txt.setDisable(is_dis);
            //The exatra condition here is needed to avoid enabling 
            //the scaling control in case the exact fitness is used
            m_ftn_scale_txt.setDisable(is_dis
                    || (m_fit_cmb.getSelectionModel().getSelectedItem() == FitnessType.EXACT));
        }
    }

    private void enable_non_btn_ctrls(final boolean is_dis) {
        //m_ctrl_name_txt.setDisable(is_start);
        m_dims_cmb.setDisable(is_dis);
        if (!m_is_iter_cbx.isSelected()) {
            m_max_mut_txt.setDisable(is_dis);
        }
        m_max_tree_size_txt.setDisable(is_dis);
        m_max_pop_size_txt.setDisable(is_dis);
        m_grammar_txt.setDisable(is_dis);
        m_ch_vs_rep_sld.setDisable(is_dis);
        m_tm_vs_tnm_sld.setDisable(is_dis);
        m_init_pop_sld.setDisable(is_dis);
        m_workers_dof_txt.setDisable(is_dis);
        m_tour_cmb.setDisable(is_dis);
        m_is_stop_cbx.setDisable(is_dis);
        m_is_iter_cbx.setDisable(is_dis);
        m_min_ngf_txt.setDisable(is_dis);
        m_max_ngf_txt.setDisable(is_dis);

        enable_fitness_ctrls(is_dis);
        enable_monte_carlo_ctrls(is_dis);
        enable_child_limit_ctrls(is_dis);

        m_is_prop_pn_cbx.setDisable(is_dis);
        m_is_scale_cbx.setDisable(is_dis);
        m_is_avoid_equal_cbx.setDisable(is_dis);
        m_is_opt_on_save_cbx.setDisable(is_dis);
        m_max_gd_txt.setDisable(is_dis);
        m_ch_sp_x_txt.setDisable(is_dis);
        m_ch_sp_y_txt.setDisable(is_dis);
    }

    /**
     * Disables the interface when started running Symbolic Regression
     *
     * @param is_start true if the process is starting
     * @param is_ok true if the process is finishing and it went without errors
     * @param is_stop_mgrs true if the process managers are to be stopped
     */
    private void enable_ctrls_run(
            final boolean is_start, final boolean is_ok,
            final boolean is_stop_mgrs) {
        m_load_btn.setDisable(is_start);
        m_run_btn.setDisable(is_start);
        m_stop_btn.setDisable(!is_start);
        m_save_btn.setDisable(is_start || !is_ok);

        enable_non_btn_ctrls(is_start);

        if (!is_start && is_stop_mgrs) {
            m_log.info("Started stopping the process managers for all dofs.");
            m_managers.forEach((manager) -> {
                manager.stop(true);
            });
            m_log.info("Finished stopping the process managers for all dofs.");
        }
    }

    @FXML
    public void stopRunning(ActionEvent event) {
        enable_ctrls_run(false, true, true);
    }

    /**
     * Counts the number of finished dofs identified by population managers
     *
     * @param mgr the process manager
     * @return true if the process manager was active, otherwise false
     */
    private synchronized boolean count_finished_dofs(final ProcessManager mgr) {
        final boolean result = m_managers_act.remove(mgr);
        if (m_managers_act.isEmpty()) {
            enable_ctrls_run(false, true, false);
        }
        return result;
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
        m_log.info("Started grammar parameters configuration");
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
                final boolean is_monte_carlo = m_mc_fitness_cbx.isSelected();
                final boolean is_rec_strat_sample = m_rss_ftn_cbx.isSelected();
                final long sample_size = Long.parseLong(m_act_sam_size_txt.getText());
                final long min_bisect_size = Long.parseLong(m_min_bis_size_txt.getText());
                final double sample_bisect_ratio = m_rss_bis_ratio_sld.getValue();

                //Talk to Scots via interface
                m_log.info("Started configuring the SCOTS2DLL backend.");
                final FConfig f_cfg = new FConfig(num_ss_dofs, fitness_type,
                        attr_size, ftn_scale, is_scale, is_complex,
                        is_monte_carlo, is_rec_strat_sample, sample_size,
                        min_bisect_size, sample_bisect_ratio);
                ScotsFacade.INSTANCE.configure(f_cfg);
                m_log.info("Finished configuring the SCOTS2DLL backend.");

                //Set the grammar
                m_log.info("Started creating the new grammar.");
                final GrammarConfig g_cfg = new GrammarConfig(m_grammar_txt.getText(),
                        Integer.parseInt(m_max_tree_size_txt.getText()),
                        m_ch_vs_rep_sld.getValue(), num_ss_dofs,
                        Double.parseDouble(m_min_ngf_txt.getText()),
                        Double.parseDouble(m_max_ngf_txt.getText()),
                        m_is_prop_pn_cbx.isSelected(),
                        Integer.parseInt(m_max_gd_txt.getText()),
                        m_tm_vs_tnm_sld.getValue());
                grammar = Grammar.create_grammar(g_cfg);
                m_log.info("Finished creating the new grammar.");
            } finally {
                m_main_prog_ind.setVisible(false);
                m_stop_btn.setDisable(false);
            }
        } else {
            throw new IllegalArgumentException("The number of state-space dimensions is not set!");
        }
        m_log.info("Finished grammar parameters configuration");
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
        m_log.info("Started preparing grammar for symbolic regression.");
        Grammar.clear_grammars();
        IntStream.range(0, (m_num_dofs - num_ss_dofs)).forEachOrdered(mgr_id -> {
            //Register the grammar for the given manager, the dof index 
            //is always 0 as there is on input signal dimension per manager
            Grammar.register_grammar(mgr_id, 0, grammar);
        });
        Grammar.prepare_grammars();
        m_log.info("Finished preparing grammar for symbolic regression.");

        //Constructe the interface elements
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                m_managers.clear();
                m_dof_tab.getTabs().clear();
                m_progress_box.getChildren().clear();
                IntStream.range(0, (m_num_dofs - num_ss_dofs)).forEachOrdered(mgr_id -> {
                    m_log.info("Starting symbolic regression process manager for dof: " + mgr_id);

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
                                final ProcessManager mgr = m_managers.get(mgr_id);
                                if (count_finished_dofs(mgr)) {
                                    //Stop the process manager as the 100% fit individual is found
                                    m_log.info("The 100% fit individual is found for dof: " + mgr_id);
                                    mgr.stop(true);
                                    m_log.info("The process manager for dof " + mgr_id + " is stopped: ");
                                }
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
                    m_managers.add(manager);
                    m_managers_act.add(manager);
                    dofTab.setContent(grid_pane);
                    dofTab.setOnSelectionChanged(ev -> {
                        visualizer.set_active(dofTab.isSelected());
                    });
                    m_dof_tab.getTabs().add(dofTab);

                    //Start the process manager
                    manager.start();
                    m_log.info("The symbolic regression for dof " + mgr_id + " is started.");
                });
            }
        });
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
                            m_log.err("Faled to start execution: " + ex.getMessage());
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

    private void load_properties() {
        //Register UI components with saved values
        m_log.info("Started registering UI parameter components.");
        m_prop_mgr.register(m_max_pop_size_txt);
        m_prop_mgr.register(m_max_mut_txt);
        m_prop_mgr.register(m_max_tree_size_txt);
        m_prop_mgr.register(m_workers_dof_txt);
        m_prop_mgr.register(m_ftn_scale_txt);
        m_prop_mgr.register(m_ch_sp_x_txt);
        m_prop_mgr.register(m_ch_sp_y_txt);
        m_prop_mgr.register(m_ch_vs_rep_sld);
        m_prop_mgr.register(m_tm_vs_tnm_sld);
        m_prop_mgr.register(m_init_pop_sld);
        m_prop_mgr.register(m_grammar_txt);
        m_prop_mgr.register(m_min_ngf_txt);
        m_prop_mgr.register(m_max_ngf_txt);
        m_prop_mgr.register(m_attract_txt);
        m_prop_mgr.register(m_tour_cmb);
        m_prop_mgr.register(m_fit_cmb);
        m_prop_mgr.register(m_is_stop_cbx);
        m_prop_mgr.register(m_is_iter_cbx);
        m_prop_mgr.register(m_is_prop_pn_cbx);
        m_prop_mgr.register(m_is_scale_cbx);
        m_prop_mgr.register(m_is_compl_cbx);
        m_prop_mgr.register(m_is_child_lim_cbx);
        m_prop_mgr.register(m_is_avoid_equal_cbx);
        m_prop_mgr.register(m_is_opt_on_save_cbx);
        m_prop_mgr.register(m_min_ch_cnt_txt);
        m_prop_mgr.register(m_max_ch_cnt_txt);
        m_prop_mgr.register(m_max_gd_txt);
        m_prop_mgr.register(m_mc_fitness_cbx);
        m_prop_mgr.register(m_rss_ftn_cbx);
        m_prop_mgr.register(m_rel_sam_size_txt);
        m_prop_mgr.register(m_min_bis_size_txt);
        m_prop_mgr.register(m_rss_bis_ratio_sld);
        m_log.info("Finished registering UI parameter components.");

        //Load properties
        m_log.info("Started loading UI component parameters.");
        m_prop_mgr.load_properties();
        m_log.info("Loading parameters from: " + m_prop_mgr.get_abs_file_name());
        m_log.info("Finished loading UI component parameters.");
    }

    private void set_up_progress_bars() {
        m_main_prog_ind.setProgress(-1.0);
        m_main_prog_ind.setVisible(false);
        m_progress_box.setAlignment(Pos.CENTER_LEFT);
    }

    private void set_up_tournament_type() {
        m_tour_cmb.getItems().add(SelectionType.VALUE.get_idx(),
                SelectionType.VALUE);
        m_tour_cmb.getItems().add(SelectionType.PROB.get_idx(),
                SelectionType.PROB);
        m_tour_cmb.getSelectionModel().selectFirst();
    }

    private void set_up_fitness_type() {
        m_fit_cmb.getItems().add(FitnessType.EXACT);
        m_fit_cmb.getItems().add(FitnessType.ATANG);
        m_fit_cmb.getItems().add(FitnessType.INVER);
        m_fit_cmb.valueProperty().addListener(new ChangeListener<FitnessType>() {
            @Override
            public void changed(ObservableValue ov, FitnessType old_val, FitnessType new_val) {
                //Enable and disable elements depending on fitness its type
                final boolean is_dis = (new_val == FitnessType.EXACT);
                m_ftn_scale_txt.setDisable(is_dis);
                //Store the scaling factor
                final String old_str = m_ftn_scale_txt.getText();
                if ((old_val != null) && !old_str.trim().isEmpty()) {
                    old_val.set_scaling(Double.parseDouble(old_str));
                }
                m_ftn_scale_txt.setText(Double.toString(new_val.get_scaling()));
            }
        });
        m_fit_cmb.getSelectionModel().select(FitnessType.INVER);
    }

    private void set_up_max_num_iter() {
        m_max_mut_txt.setText(Long.toString(Long.MAX_VALUE));
        m_is_iter_cbx.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue) {
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
    }

    private void set_up_child_limits() {
        m_is_child_lim_cbx.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue) {
                if (m_is_child_lim_cbx.isSelected()) {
                    m_min_ch_cnt_txt.setDisable(false);
                    m_max_ch_cnt_txt.setDisable(false);
                } else {
                    m_min_ch_cnt_txt.setDisable(true);
                    m_max_ch_cnt_txt.setDisable(true);
                }
            }
        });
    }

    private void set_up_complicated_fitness() {
        m_is_compl_cbx.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue) {
                if (m_is_compl_cbx.isSelected()) {
                    //If the complex fitness is selected then
                    m_fit_cmb.setDisable(false);
                    //The exact fitness does not require the scaling factor
                    m_ftn_scale_txt.setDisable(
                            m_fit_cmb.getSelectionModel().getSelectedItem() == FitnessType.EXACT);
                    m_attract_txt.setDisable(false);
                } else {
                    //If the complex fitness is not selected then diable all
                    m_fit_cmb.setDisable(true);
                    m_ftn_scale_txt.setDisable(true);
                    m_attract_txt.setDisable(true);
                }
            }
        });
    }

    /**
     * Attempts to re-compute the sample size based on the state-space size
     */
    private void re_compute_mc_sample_size() {
        final String rel_ss = m_rel_sam_size_txt.getText().trim();
        final Object value = m_dims_cmb.getValue();
        if (!rel_ss.isEmpty() && (value != null)) {
            try {
                final int ss_dim = Integer.parseInt(value.toString());
                final int ss_size = ScotsFacade.INSTANCE.get_state_space_size(ss_dim);
                final double coeff = Double.parseDouble(rel_ss);
                final long size = Math.round(ss_size * coeff);
                m_act_sam_size_txt.setText(Long.toString(size));
            } catch (IllegalAccessException | InvocationTargetException | NumberFormatException ex) {
                LOGGER.log(Level.WARNING,
                        "Could not compute relative sample size: {0}, exception: {1}",
                        new Object[]{rel_ss, ex.getMessage()});
            }
        }
    }

    private void set_up_mc_fitness() {
        //Add the change listener to the Monte Carlo fitness check box
        m_mc_fitness_cbx.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue) {
                m_rss_ftn_cbx.setDisable(oldValue);
                m_rel_sam_size_txt.setDisable(oldValue);
                if (m_rss_ftn_cbx.isSelected()) {
                    m_min_bis_size_txt.setDisable(oldValue);
                    m_rss_bis_ratio_sld.setDisable(oldValue);
                }
            }
        });
        //Add the change listener to the number of dimensions combo box
        m_dims_cmb.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                re_compute_mc_sample_size();
            }
        });

        //Add the change listener for m_rel_sam_size_txt to change the m_act_sam_size_txt if needed
        m_rel_sam_size_txt.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                re_compute_mc_sample_size();
            }
        });
    }

    private void set_up_rss_fitness() {
        m_rss_ftn_cbx.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue) {
                m_min_bis_size_txt.setDisable(oldValue);
                m_rss_bis_ratio_sld.setDisable(oldValue);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        m_log = new ConsoleLog(m_log_lst);

        m_log.info("Started setting up UI components.");

        set_up_progress_bars();
        set_up_tournament_type();
        set_up_fitness_type();
        set_up_max_num_iter();
        set_up_max_num_iter();
        set_up_child_limits();
        set_up_complicated_fitness();
        set_up_mc_fitness();
        set_up_rss_fitness();

        //Load properties, do this after all default initializations
        load_properties();

        m_log.info("Finished setting up UI components.");
    }

    /**
     * Allows to perform the initial checks.
     */
    public void after_show() {
        //Once the scene has been shown check if the fitness computing dynamic
        //library is available, if not then open the dialog to locate it and load
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
                String lib_file_name = m_prop_mgr.get_property(PropertyManager.LIB_FILE_NAME_PROP);
                if (lib_file_name == null) {
                    m_log.warn("The SCOTS2DLL dynamic library is not set!");

                    final File file = fileChooser.showOpenDialog(m_load_btn.getScene().getWindow());
                    if (file != null) {
                        lib_file_name = file.getPath();
                        m_prop_mgr.set_property(PropertyManager.LIB_FILE_NAME_PROP, lib_file_name);
                    } else {
                        //If the library is not selected then re-try
                        Platform.runLater(this);
                        return;
                    }
                }

                m_log.info("Loading the SCOTS2DLL "
                        + "dynamic library from: " + lib_file_name);

                //Attempt loading the library
                if (ScotsFacade.INSTANCE.load_library(lib_file_name)) {
                    m_prop_mgr.remove(PropertyManager.LIB_FILE_NAME_PROP);
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
