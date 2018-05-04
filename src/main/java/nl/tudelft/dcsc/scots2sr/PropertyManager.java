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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * The options manager class
 */
public class PropertyManager {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(PropertyManager.class.getName());

    //Stores the property name prefix
    static final String PROPERTY_IDX_PREF = "$$$";

    //Stores the library file property name
    static final String LIB_FILE_NAME_PROP = "Native Library Name";

    //Stores the properties file
    private final File m_props_file;
    //Stores the properties
    private final Properties m_props;
    //Stores the list of registered controls
    private final Map<String, Control> m_controls;

    /**
     * The basic constructor
     *
     * @param file_name the file name to store/load properties in/from
     */
    public PropertyManager(final String file_name) {
        m_props_file = new File(file_name);
        m_props = new Properties();
        m_controls = new HashMap<>();
    }

    /**
     * Allows to register control whoes value is to be stored/loaded in/from
     * properties
     *
     * @param name the control property name
     * @param ctrl the control object
     */
    public void register(final String name, final Control ctrl) {
        m_controls.put(name, ctrl);
    }

    /**
     * Allows to get the absolute path name for the properties file
     *
     * @return the absolute path name for the properties file
     */
    public String get_abs_file_name() {
        return m_props_file.getAbsolutePath();
    }

    /**
     * Allows to load the properties from file
     */
    public void load_properties() {
        try {
            try (FileReader reader = new FileReader(m_props_file)) {
                m_props.load(reader);
                m_controls.forEach((name, control)->{
                    get_prop(name, control);
                });
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Unable to load config file: {0}",
                    m_props_file.getName());
        }
    }

    /**
     * Allows to store properties into file
     */
    public void save_properties() {
        try {
            try (FileWriter writer = new FileWriter(m_props_file)) {
                m_controls.forEach((name, control)->{
                    set_prop(name, control);
                });
                m_props.store(writer, "Settings");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Unable to save config file: "
                    + m_props_file.getName(), ex);
        }
    }

    /**
     * Allows to get the property value
     *
     * @param key the key
     * @param obj the control to get the value from
     * @return the corresponding value or null if none
     */
    private String get_prop(final String key, final Control obj) {
        final String value = m_props.getProperty(key);
        if (value != null) {
            if (obj instanceof ComboBox<?>) {
                get_property(key, (ComboBox<?>) obj);
            } else {
                if (obj instanceof CheckBox) {
                    get_property(key, (CheckBox) obj);
                } else {
                    if (obj instanceof Slider) {
                        get_property(key, (Slider) obj);
                    } else {
                        if (obj instanceof TextField) {
                            get_property(key, (TextField) obj);
                        } else {
                            if (obj instanceof TextArea) {
                                get_property(key, (TextArea) obj);
                            } else {
                                LOGGER.log(Level.SEVERE,
                                        "Could not set property {0} into {1}",
                                        new Object[]{key, obj});
                            }
                        }
                    }
                }
            }
        }
        return value;
    }

    /**
     * Allows to get the property value
     *
     * @param key the key
     * @param ctrl the control to get the value for
     */
    public void get_property(final String key, final TextArea ctrl) {
        final String value = get_property(key);
        remove(key);
        if (value != null) {
            ctrl.setText(value);
        } else {
            LOGGER.log(Level.SEVERE,
                    "Could not get value for TextArea with id {0}", key);
        }
    }

    /**
     * Allows to get the property value
     *
     * @param key the key
     * @param ctrl the control to get the value for
     */
    public void get_property(final String key, final TextField ctrl) {
        final String value = get_property(key);
        remove(key);
        if (value != null) {
            ctrl.setText(value);
        } else {
            LOGGER.log(Level.SEVERE,
                    "Could not get value for TextField with id {0}", key);
        }
    }

    /**
     * Allows to get the property value
     *
     * @param key the key
     * @param ctrl the control to get the value for
     */
    public void get_property(final String key, final ComboBox<?> ctrl) {
        final String value = get_property(key);
        remove(key);
        try {
            ctrl.getSelectionModel().select(Integer.parseInt(value));
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.SEVERE,
                    "Could not get value for ComboBox with id {0}", key);
        }
    }

    /**
     * Allows to get the property value
     *
     * @param key the key
     * @param ctrl the control to get the value for
     */
    public void get_property(final String key, final CheckBox ctrl) {
        final String value = get_property(key);
        remove(key);
        try {
            ctrl.setSelected(Boolean.parseBoolean(value));
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.SEVERE,
                    "Could not get value for CheckBox with id {0}", key);
        }
    }

    /**
     * Allows to get the property value
     *
     * @param key the key
     * @param ctrl the control to get the value for
     */
    public void get_property(final String key, final Slider ctrl) {
        final String value = get_property(key);
        remove(key);
        try {
            ctrl.setValue(Double.parseDouble(value));
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.SEVERE,
                    "Could not get value for Slider with id {0}", key);
        }
    }

    /**
     * Allows to get the property value
     *
     * @param key the key
     * @param obj the control to set the value into
     */
    private void set_prop(final String key, final Control obj) {
        if (obj instanceof ComboBox<?>) {
            set_property(key, (ComboBox<?>) obj);
        } else {
            if (obj instanceof CheckBox) {
                set_property(key, (CheckBox) obj);
            } else {
                if (obj instanceof Slider) {
                    set_property(key, (Slider) obj);
                } else {
                    if (obj instanceof TextField) {
                        set_property(key, (TextField) obj);
                    } else {
                        if (obj instanceof TextArea) {
                            set_property(key, (TextArea) obj);
                        } else {
                            LOGGER.log(Level.SEVERE,
                                    "Could not set property {0} into {1}",
                                    new Object[]{key, obj});
                        }
                    }
                }
            }
        }
    }

    /**
     * Allows to get the property value
     *
     * @param key the key
     * @return the corresponding value or null if none
     */
    public String get_property(final String key) {
        return m_props.getProperty(key);
    }

    /**
     * Allows to set a new property value
     *
     * @param key the key
     * @param value the value
     */
    public void set_property(final String key, final String value) {
        m_props.setProperty(key, value);
    }

    /**
     * Allows to set a new property value
     *
     * @param key the key
     * @param value the value
     */
    public void set_property(final String key, final double value) {
        m_props.setProperty(key, Double.toString(value));
    }

    /**
     * Allows to set a new property value
     *
     * @param key the key
     * @param value the value
     */
    public void set_property(final String key, final int value) {
        m_props.setProperty(key, Integer.toString(value));
    }

    /**
     * Allows to set a new property value
     *
     * @param key the key
     * @param value the value
     */
    public void set_property(final String key, final boolean value) {
        m_props.setProperty(key, Boolean.toString(value));
    }

    /**
     * Allows to set a new property value
     *
     * @param key the key
     * @param ctrl the control to store the state of
     */
    public void set_property(final String key, final TextArea ctrl) {
        m_props.setProperty(key, ctrl.getText());
    }

    /**
     * Allows to set a new property value
     *
     * @param key the key
     * @param ctrl the control to store the state of
     */
    public void set_property(final String key, final TextField ctrl) {
        m_props.setProperty(key, ctrl.getText());
    }

    /**
     * Allows to set a new property value
     *
     * @param key the key
     * @param ctrl the control to store the state of
     */
    public void set_property(final String key, final ComboBox<?> ctrl) {
        set_property(key, ctrl.getSelectionModel().getSelectedIndex());
    }

    /**
     * Allows to set a new property value
     *
     * @param key the key
     * @param ctrl the control to store the state of
     */
    public void set_property(final String key, final CheckBox ctrl) {
        set_property(key, ctrl.isSelected());
    }

    /**
     * Allows to set a new property value
     *
     * @param key the key
     * @param ctrl the control to store the state of
     */
    public void set_property(final String key, final Slider ctrl) {
        set_property(key, ctrl.getValue());
    }

    /**
     * Allows to remove property
     *
     * @param key the property name
     * @return the stored value or null if none
     */
    public Object remove(final String key) {
        return m_props.remove(key);
    }

}
