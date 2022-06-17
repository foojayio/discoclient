/*
 * Copyright (c) 2021 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.foojay.api.discoclient;

import io.foojay.api.discoclient.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static io.foojay.api.discoclient.util.Constants.API_VERSION_V3;
import static io.foojay.api.discoclient.util.Constants.DISCO_API_BASE_URL;
import static io.foojay.api.discoclient.util.Constants.DISTRIBUTION_JSON_URL;
import static io.foojay.api.discoclient.util.Constants.PROPERTY_KEY_DISCO_URL;
import static io.foojay.api.discoclient.util.Constants.PROPERTY_KEY_DISCO_VERSION;
import static io.foojay.api.discoclient.util.Constants.PROPERTY_KEY_DISTRIBUTION_JSON_URL;


public enum PropertyManager {
    INSTANCE;

    private              Properties properties;


    // ******************** Constructors **************************************
    PropertyManager() {
        properties = new Properties();
        // Load properties
        final String propFilePath = new StringBuilder(Constants.HOME_FOLDER).append(File.separator).append(Constants.PROPERTIES_FILE_NAME).toString();

        // Create properties file if not exists
        Path path = Paths.get(propFilePath);
        if (!Files.exists(path)) { createProperties(properties); }

        // Load properties file
        try (FileInputStream propFile = new FileInputStream(propFilePath)) {
            properties.load(propFile);
        } catch (IOException ex) {
            System.out.println("Error reading properties file. " + ex);
        }

        // If properties empty, fill with default values
        if (properties.isEmpty()) { createProperties(properties); }
    }


    // ******************** Methods *******************************************
    public Properties getProperties() { return properties; }

    public Object get(final String KEY) { return properties.getOrDefault(KEY, ""); }
    public void set(final String KEY, final String VALUE) {
        properties.setProperty(KEY, VALUE);
        try {
            properties.store(new FileOutputStream(String.join(File.separator, System.getProperty("user.dir"), Constants.PROPERTIES_FILE_NAME)), null);
        } catch (IOException exception) {
            System.out.println("Error writing properties file: " + exception);
        }
    }

    public String getString(final String key) { return properties.getOrDefault(key, "").toString(); }
    public void setString(final String key, final String value) { properties.setProperty(key, value); }

    public double getDouble(final String key) { return getDouble(key, 0); }
    public double getDouble(final String key, final double defaultValue) { return Double.parseDouble(properties.getOrDefault(key, Double.toString(defaultValue)).toString()); }
    public void setDouble(final String key, final double value) { properties.setProperty(key, Double.toString(value)); }

    public float getFloat(final String key) { return getFloat(key, 0); }
    public float getFloat(final String key, final float defaultValue) { return Float.parseFloat(properties.getOrDefault(key, Float.toString(defaultValue)).toString()); }
    public void setFloat(final String key, final float value) { properties.setProperty(key, Float.toString(value)); }

    public int getInt(final String key) { return getInt(key, 0); }
    public int getInt(final String key, final int defaultValue) { return Integer.parseInt(properties.getOrDefault(key, Integer.toString(defaultValue)).toString()); }
    public void setInt(final String key, final int value) { properties.setProperty(key, Integer.toString(value)); }

    public long getLong(final String key) { return getLong(key, 0); }
    public long getLong(final String key, final long defaultValue) { return Long.parseLong(properties.getOrDefault(key, Long.toString(defaultValue)).toString()); }
    public void setLong(final String key, final long value) { properties.setProperty(key, Long.toString(value)); }

    public boolean getBoolean(final String key) { return getBoolean(key, false); }
    public boolean getBoolean(final String key, final boolean defaultValue) { return Boolean.parseBoolean(properties.getOrDefault(key, Boolean.toString(defaultValue)).toString()); }
    public void setBoolean(final String key, final boolean value) { properties.setProperty(key, Boolean.toString(value)); }

    public boolean hasKey(final String key) { return properties.containsKey(key); }

    public String getApiVersion() {
        return properties.getOrDefault(PROPERTY_KEY_DISCO_VERSION, API_VERSION_V3).toString();
    }

    public String getPackagesPath() {
        String apiVersion = getApiVersion();
        return new StringBuilder().append("/disco/v").append(apiVersion).append("/packages").toString();
    }

    public String getEphemeralIdsPath() {
        String apiVersion = getApiVersion();
        return new StringBuilder().append("/disco/v").append(apiVersion).append("/ephemeral_ids").toString();
    }

    public String getMajorVersionsPath() {
        String apiVersion = getApiVersion();
        return new StringBuilder().append("/disco/v").append(apiVersion).append("/major_versions").toString();
    }

    public String getIdsPath() {
        String apiVersion = getApiVersion();
        return new StringBuilder().append("/disco/v").append(apiVersion).append("/ids").toString();
    }

    public String getDistributionsPath() {
        String apiVersion = getApiVersion();
        return new StringBuilder().append("/disco/v").append(apiVersion).append("/distributions").toString();
    }


    // ******************** Properties ****************************************
    public void storeProperties() {
        if (null == properties) { return; }
        final String propFilePath = new StringBuilder(Constants.HOME_FOLDER).append(Constants.PROPERTIES_FILE_NAME).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createProperties(Properties properties) {
        final String propFilePath = new StringBuilder(Constants.HOME_FOLDER).append(File.separator).append(Constants.PROPERTIES_FILE_NAME).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            properties.put(PROPERTY_KEY_DISCO_URL, DISCO_API_BASE_URL);
            properties.put(PROPERTY_KEY_DISCO_VERSION, API_VERSION_V3);
            properties.put(PROPERTY_KEY_DISTRIBUTION_JSON_URL, DISTRIBUTION_JSON_URL);
            properties.store(output, null);
        } catch (IOException ex) {
        }
    }
}
