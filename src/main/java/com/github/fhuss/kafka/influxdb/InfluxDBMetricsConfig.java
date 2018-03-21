/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fhuss.kafka.influxdb;

import kafka.utils.VerifiableProperties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InfluxDBMetricsConfig {

    private static final List<String> RETENTION_POLICIES = Arrays.asList("one", "quorum", "all", "any");

    private static final String KAFKA_INFLUX_METRICS_CONNECT_CONFIG     = "kafka.influxdb.metrics.address";
    private static final String KAFKA_INFLUX_METRICS_DATABASE_CONFIG    = "kafka.influxdb.metrics.database";
    private static final String KAFKA_INFLUX_METRICS_USERNAME_CONFIG    = "kafka.influxdb.metrics.username";
    private static final String KAFKA_INFLUX_METRICS_PASSWORD_CONFIG    = "kafka.influxdb.metrics.password";
    private static final String KAFKA_INFLUX_METRICS_CONSISTENCY_CONFIG = "kafka.influxdb.metrics.consistency";
    private static final String KAFKA_INFLUX_METRICS_RETENTION_CONFIG   = "kafka.influxdb.metrics.retention";
    private static final String KAFKA_INFLUX_METRICS_TAGS_CONFIG        = "kafka.influxdb.metrics.tags";
    private static final String KAFKA_GRAPHITE_MEASURE_ENABLED_CONFIG   = "kafka.influxdb.measure.enabled";
    static final String KAFKA_INFLUX_METRICS_ENABLE                     = "kafka.influxdb.metrics.reporter.enabled";

    private String connectString;
    private String database;
    private String username;
    private String password;
    private String consistency;
    private String retention;
    private Map<String, String> tags = new HashMap<>();
    private Map<MetricsPredicate.Measures, Boolean> predicates = new HashMap<>();

    /**
     * Creates a new {@link InfluxDBMetricsConfig} instance.
     *
     * @param props  The Kafka configuration.
     */
    InfluxDBMetricsConfig(VerifiableProperties props) {
        this.connectString = props.getString(KAFKA_INFLUX_METRICS_CONNECT_CONFIG, "http://localhost:8086");
        this.database = props.getString(KAFKA_INFLUX_METRICS_DATABASE_CONFIG, "kafka");
        this.username = props.getString(KAFKA_INFLUX_METRICS_USERNAME_CONFIG, "root");
        this.password = props.getString(KAFKA_INFLUX_METRICS_PASSWORD_CONFIG, "root");
        if( props.containsKey(KAFKA_INFLUX_METRICS_CONSISTENCY_CONFIG) )
            this.consistency = props.getString(KAFKA_INFLUX_METRICS_CONSISTENCY_CONFIG);
        if( props.containsKey(KAFKA_INFLUX_METRICS_RETENTION_CONFIG) ) {
            this.retention = props.getString(KAFKA_INFLUX_METRICS_RETENTION_CONFIG);
            checkRetentionPolicies();
        }
        setAdditionalTags(props);
        setMeasuresFilters(props);
    }

    private void setMeasuresFilters(VerifiableProperties props) {
        for (MetricsPredicate.Measures measure : MetricsPredicate.Measures.values()) {
            boolean enable = props.getBoolean(KAFKA_GRAPHITE_MEASURE_ENABLED_CONFIG + "." + measure.name(), true);
            this.predicates.put(measure, enable);
        }
    }

    private void setAdditionalTags(VerifiableProperties props) {
        String tags = props.getString(KAFKA_INFLUX_METRICS_TAGS_CONFIG, null);
        if( tags != null) {
            for(String tag : tags.split(",") ) {
                String[] tagAndValue = tag.split("=");
                addTag(tagAndValue[0], tagAndValue[1]);
            }
        }
    }

    private void checkRetentionPolicies() {
        if( ! RETENTION_POLICIES.contains(this.retention)) {
            throw new IllegalArgumentException(
                    String.format("Unknown retention policy '%s': %s", retention, RETENTION_POLICIES ));
        }
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConsistency(String consistency) {
        this.consistency = consistency;
    }

    public void setRetention(String retention) {
        this.retention = retention;
    }

    public void addTag(String tag, String value) {
        this.tags.put(tag, value);
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    Map<String, String> getTags() {
        return this.tags;
    }

    String getConnectString() {
        return this.connectString;
    }

    String getDatabase() {
        return this.database;
    }

    String getUsername() {
        return this.username;
    }

    String getPassword() {
        return this.password;
    }

    String getConsistency() {
        return this.consistency;
    }

    String getRetention() {
        return this.retention;
    }

    Map<MetricsPredicate.Measures, Boolean> getPredicates() {
        return predicates;
    }
}
