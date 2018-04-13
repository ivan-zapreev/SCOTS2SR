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
package nl.tudelft.dcsc.scots2sr.jni;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tudelft.dcsc.scots2jni.Scots2JNI;
import nl.tudelft.dcsc.scots2jni.FConfig;
import nl.tudelft.dcsc.sr2jlib.fitness.Fitness;
import nl.tudelft.dcsc.sr2jlib.fitness.FitnessComputerClass;
import nl.tudelft.dcsc.sr2jlib.fitness.FitnessManager;
import nl.tudelft.dcsc.sr2jlib.grid.Individual;
import nl.tudelft.dcsc.sr2jlib.instance.Loader;

/**
 *
 * Represents the Scots/JNI facade class, is a singleton.
 *
 * @author Dr. Ivan S. Zapreev
 */
public class ScotsFacade extends FitnessComputerClass {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(ScotsFacade.class.getName());

    //Stores the unfit points file suffix for the BDD file name
    public static final String UNFIT_FILE_SUFFIX = ".unfit";

    //Stores the class loader
    private Loader m_loader;
    //Stores the Scots2JNI loaded class
    private Class<?> m_class;
    //Stores the Scots2JNI class interface methods
    private Method m_load;
    private Method m_configure;
    private Method m_compute_fitness;
    private Method m_start_unfit_export;
    private Method m_export_unfit_points;
    private Method m_finish_unfit_export;

    /**
     * The private constructor for the singleton
     */
    private ScotsFacade() {
    }

    //Stores the singleton class instance
    public static final ScotsFacade INSTANCE = new ScotsFacade();

    //Set the fitness computer
    static {
        FitnessManager.set_inst(INSTANCE);
    }

    /**
     * Allows to load the dynamic library interacting with SCOTS and checking
     * for fitness.
     *
     * @param lib_file_name the library file name
     * @return true if the library could not be loaded, otherwise false
     */
    public boolean load_library(final String lib_file_name) {
        m_loader = new Loader();
        try {
            final String name = Scots2JNI.class.getName();
            m_class = m_loader.loadClassNC(name);

            //Load the Native library within the class as otherwise 
            //it is not accessible through the custom class loader
            try {
                final Method load_lib = m_class.getMethod("load_lib", String.class);
                load_lib.invoke(null, lib_file_name);
            } catch (UnsatisfiedLinkError | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, "Native code library '"
                        + lib_file_name + "' failed to load.\n", ex);
                return true;
            }

            m_load = m_class.getMethod("load", String.class);
            m_configure = m_class.getMethod("configure", FConfig.class);
            m_compute_fitness = m_class.getMethod("compute_fitness", String.class, int.class);
            m_start_unfit_export = m_class.getMethod("start_unfit_export");
            m_export_unfit_points = m_class.getMethod("export_unfit_points", String.class, int.class);
            m_finish_unfit_export = m_class.getMethod("finish_unfit_export", String.class);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
            LOGGER.log(Level.SEVERE, "Failed when loading and instantiating "
                    + Scots2JNI.class.getName(), ex);
            return true;
        }

        return false;
    }

    /**
     * Allows to load the controller
     *
     * @param file_name the file name to load the controller form
     * @return the number of controller dimensions
     * @throws java.io.FileNotFoundException if the controller file is not found
     * @throws IllegalAccessException if the JNI invocation has failed
     * @throws InvocationTargetException if the JNI invocation has failed
     */
    public int load(final String file_name) throws FileNotFoundException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return (Integer) m_load.invoke(null, file_name);
    }

    /**
     * Allows to configure the fitness computer
     *
     * @param cfg the configuration object
     * @throws IllegalArgumentException if one of the configuration parameters
     * has an incorrect value
     * @throws IllegalAccessException if the JNI invocation has failed
     * @throws InvocationTargetException if the JNI invocation has failed
     */
    public void configure(final FConfig cfg) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        m_configure.invoke(null, cfg);
    }

    @Override
    public Fitness compute_fitness(
            final int mgr_id, final String class_name)
            throws IllegalStateException, IllegalArgumentException,
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException {
        return (Fitness) m_compute_fitness.invoke(null, class_name, mgr_id);
    }

    /**
     * Allows to store the unfit points for the resulting symbolic controller
     *
     * @param file_name the file name, without the extension (will be stored as
     * a BDD)
     * @param inds the manager-id to individual mapping, each manager id
     * corresponds to the dof index, the list index must correspond to the
     * manager id stored inside the given individual.
     * @throws java.lang.IllegalAccessException if the JNI illegal access occurs
     * @throws java.lang.reflect.InvocationTargetException if the JNI target can
     * not be invoked
     */
    public void store_unfit_points(final String file_name,
            final List<Individual> inds) throws IllegalAccessException, InvocationTargetException {
        //Start new unfit points export
        m_start_unfit_export.invoke(null, new Object[]{});
        //Iterate over the individuals
        for (int idx = 0; idx < inds.size(); ++idx) {
            final Individual ind = inds.get(idx);
            final int mgr_id = ind.get_mgr_id();
            //Check on that the manager id corresponds to the list index
            if (mgr_id != idx) {
                throw new IllegalArgumentException("Theindividual's manager index "
                        + mgr_id + " must be equal to the array index " + idx);
            }
            //Export the unfit points, missuse the fintess compute class instance for that.
            new FitnessComputerClass() {
                @Override
                public Fitness compute_fitness(int mgr_id, String class_name)
                        throws IllegalStateException, IllegalArgumentException,
                        ClassNotFoundException, IllegalAccessException,
                        InvocationTargetException {
                    m_export_unfit_points.invoke(null, class_name, mgr_id);
                    return null;
                }
            }.compute_fitness(mgr_id, ind.get_expr_array());
        }
        //Finish new unfit points export
        m_finish_unfit_export.invoke(null, file_name + UNFIT_FILE_SUFFIX);
    }

}
