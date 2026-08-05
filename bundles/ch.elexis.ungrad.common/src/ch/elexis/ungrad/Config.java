/*******************************************************************************
 * Copyright (c) 2026 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/

package ch.elexis.ungrad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A simple configuration class that reads and writes key-value pairs to an INI
 * file. It implements IPreferenceStore and IPersistentPreferenceStore
 * interfaces. Since CoreHub.localCfg is deprecated and seems dysfunctional in
 * 3.13, this class is used to manage configuration settings in a more reliable
 * way.
 * 
 * Since the config is a simple text-file (~/elexis/ungrad.ini), it can be
 * edited by hand if necessary and simply copied to other installations. The
 * config file is created if it does not exist.
 */
public class Config implements IPreferenceStore, IPersistentPreferenceStore {

	private final File iniFile;
	private final Map<String, String> properties = new HashMap<>();
	private final Map<String, String> defaults = new HashMap<>();
	private final CopyOnWriteArrayList<IPropertyChangeListener> listeners = new CopyOnWriteArrayList<>();
	private boolean dirty = false;

	private static Config defaultInstance;

	public static synchronized Config getDefaultInstance() {
		try {
			if (defaultInstance == null) {
				defaultInstance = new Config(null);
			}
			return defaultInstance;
		} catch (IOException e) {
			System.out.print("Failed to create default Config instance");
			return null;
		}
	}

	public static synchronized Config createInstance(String filePath) throws IOException {
		return new Config(filePath);
	}

	/**
	 * Creates a new Config instance backed by an INI file. If the file does not
	 * exist, it will be created.
	 * 
	 * @param filePath path to the INI file, or null to use ~/elexis/ungrad.ini if
	 *                 the property "UngradSettings" is set, that path
	 *                 will be used instead of the default.
	 * @throws IOException if the file cannot be created or read
	 */
	private Config(String filePath) throws IOException {
		if (filePath == null) {
			String defaultPath = System.getProperty("UngradSettings");
			if (defaultPath != null && !defaultPath.isEmpty()) {
				System.out.println("Using UngradSettings environment variable for config file: " + defaultPath);
				filePath = defaultPath;
			} else {
				String userHome = System.getProperty("user.home");
				filePath = userHome + File.separator + "elexis" + File.separator + "ungrad.ini";
			}
		}
		this.iniFile = new File(filePath);
		if (!iniFile.exists()) {
			File parentDir = iniFile.getParentFile();
			if (parentDir != null && !parentDir.exists()) {
				parentDir.mkdirs();
			}
			iniFile.createNewFile();
		}
		load();
	}

	/**
	 * Loads properties from the INI file. Supports multi-line values using
	 * backslash continuation.
	 */
	private void load() throws IOException {
		properties.clear();
		if (!iniFile.exists()) {
			return;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(iniFile))) {
			String line;
			String currentKey = null;
			StringBuilder currentValue = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				String trimmed = line.trim();

				// Skip empty lines and comments (only if not in continuation)
				if (currentKey == null && (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith(";"))) {
					continue;
				}

				// Handle continuation from previous line
				if (currentKey != null) {
					// Previous line ended with backslash, append this line
					currentValue.append("\n").append(line);

					// Check if this line also continues
					if (!line.endsWith("\\")) {
						// End of multi-line value, remove trailing backslashes
						String finalValue = currentValue.toString().replace("\\\n", "\n");
						properties.put(currentKey, finalValue);
						currentKey = null;
						currentValue.setLength(0);
					} else {
						// Remove the trailing backslash for next iteration
						currentValue.setLength(currentValue.length() - 1);
					}
					continue;
				}

				// Parse new key=value line
				int equalsIndex = trimmed.indexOf('=');
				if (equalsIndex > 0) {
					String key = trimmed.substring(0, equalsIndex).trim();
					String value = trimmed.substring(equalsIndex + 1).trim();

					// Check if value continues on next line
					if (value.endsWith("\\")) {
						currentKey = key;
						currentValue.append(value, 0, value.length() - 1);
					} else {
						properties.put(key, value);
					}
				}
			}

			// Handle case where file ends with continuation
			if (currentKey != null) {
				properties.put(currentKey, currentValue.toString().replace("\\\n", "\n"));
			}
		}
		dirty = false;
	}

	@Override
	public void save() throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(iniFile))) {
			for (Map.Entry<String, String> entry : properties.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();

				// Handle multi-line values
				if (value.contains("\n")) {
					String[] lines = value.split("\n", -1);
					writer.write(key + "=");
					for (int i = 0; i < lines.length; i++) {
						writer.write(lines[i]);
						if (i < lines.length - 1) {
							writer.write("\\");
							writer.newLine();
						}
					}
					writer.newLine();
				} else {
					writer.write(key + "=" + value);
					writer.newLine();
				}
			}
		}
		dirty = false;
	}

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public boolean contains(String name) {
		return properties.containsKey(name);
	}

	@Override
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, name, oldValue, newValue);
		for (IPropertyChangeListener listener : listeners) {
			listener.propertyChange(event);
		}
	}

	@Override
	public boolean getBoolean(String name) {
		String value = properties.get(name);
		if (value != null) {
			return Boolean.parseBoolean(value);
		}
		return getDefaultBoolean(name);
	}

	public boolean getBoolean(String name, boolean defaultValue) {
		String value = properties.get(name);
		if (value != null) {
			return Boolean.parseBoolean(value);
		}
		return defaultValue;
	}

	@Override
	public boolean getDefaultBoolean(String name) {
		String value = defaults.get(name);
		return value != null ? Boolean.parseBoolean(value) : false;
	}

	@Override
	public double getDefaultDouble(String name) {
		String value = defaults.get(name);
		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				return 0.0;
			}
		}
		return 0.0;
	}

	@Override
	public float getDefaultFloat(String name) {
		String value = defaults.get(name);
		if (value != null) {
			try {
				return Float.parseFloat(value);
			} catch (NumberFormatException e) {
				return 0.0f;
			}
		}
		return 0.0f;
	}

	@Override
	public int getDefaultInt(String name) {
		String value = defaults.get(name);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return 0;
	}

	@Override
	public long getDefaultLong(String name) {
		String value = defaults.get(name);
		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				return 0L;
			}
		}
		return 0L;
	}

	@Override
	public String getDefaultString(String name) {
		String value = defaults.get(name);
		return value != null ? value : "";
	}

	@Override
	public double getDouble(String name) {
		String value = properties.get(name);
		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				return getDefaultDouble(name);
			}
		}
		return getDefaultDouble(name);
	}

	@Override
	public float getFloat(String name) {
		String value = properties.get(name);
		if (value != null) {
			try {
				return Float.parseFloat(value);
			} catch (NumberFormatException e) {
				return getDefaultFloat(name);
			}
		}
		return getDefaultFloat(name);
	}

	@Override
	public int getInt(String name) {
		String value = properties.get(name);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return getDefaultInt(name);
			}
		}
		return getDefaultInt(name);
	}

	public int getInt(String name, int defaultValue) {
		String value = properties.get(name);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	@Override
	public long getLong(String name) {
		String value = properties.get(name);
		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				return getDefaultLong(name);
			}
		}
		return getDefaultLong(name);
	}

	@Override
	public String getString(String name) {
		String value = properties.get(name);
		return value != null ? value : getDefaultString(name);
	}

	public String getString(String name, String defaultValue) {
		String value = properties.get(name);
		return value != null ? value : defaultValue;
	}

	@Override
	public boolean isDefault(String name) {
		if (!contains(name)) {
			return true;
		}
		String value = properties.get(name);
		String defaultValue = defaults.get(name);
		if (defaultValue == null) {
			return value == null;
		}
		return defaultValue.equals(value);
	}

	@Override
	public boolean needsSaving() {
		return dirty;
	}

	@Override
	public void putValue(String name, String value) {
		String oldValue = properties.put(name, value);
		dirty = true;
		firePropertyChangeEvent(name, oldValue, value);
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void setDefault(String name, double value) {
		defaults.put(name, Double.toString(value));
	}

	@Override
	public void setDefault(String name, float value) {
		defaults.put(name, Float.toString(value));
	}

	@Override
	public void setDefault(String name, int value) {
		defaults.put(name, Integer.toString(value));
	}

	@Override
	public void setDefault(String name, long value) {
		defaults.put(name, Long.toString(value));
	}

	@Override
	public void setDefault(String name, String defaultObject) {
		if (defaultObject != null) {
			defaults.put(name, defaultObject);
		}
	}

	@Override
	public void setDefault(String name, boolean value) {
		defaults.put(name, Boolean.toString(value));
	}

	@Override
	public void setToDefault(String name) {
		String defaultValue = defaults.get(name);
		if (defaultValue != null) {
			setValue(name, defaultValue);
		} else {
			String oldValue = properties.remove(name);
			dirty = true;
			firePropertyChangeEvent(name, oldValue, null);
		}
	}

	@Override
	public void setValue(String name, double value) {
		putValue(name, Double.toString(value));
	}

	@Override
	public void setValue(String name, float value) {
		putValue(name, Float.toString(value));
	}

	@Override
	public void setValue(String name, int value) {
		putValue(name, Integer.toString(value));
	}

	@Override
	public void setValue(String name, long value) {
		putValue(name, Long.toString(value));
	}

	@Override
	public void setValue(String name, String value) {
		putValue(name, value);
	}

	@Override
	public void setValue(String name, boolean value) {
		putValue(name, Boolean.toString(value));
	}

}
