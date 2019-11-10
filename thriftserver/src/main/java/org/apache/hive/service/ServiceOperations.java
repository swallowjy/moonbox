/*-
 * <<
 * Moonbox
 * ==
 * Copyright (C) 2016 - 2019 EDP
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hive.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.conf.HiveConf;

/**
 * ServiceOperations.
 *
 */
public final class ServiceOperations {
  private static final Log LOG = LogFactory.getLog(AbstractService.class);

  private ServiceOperations() {
  }

  /**
   * Verify that a org.apache.hive.service is in a given state.
   * @param state the actual state a org.apache.hive.service is in
   * @param expectedState the desired state
   * @throws IllegalStateException if the org.apache.hive.service state is different from
   * the desired state
   */
  public static void ensureCurrentState(Service.STATE state,
                                        Service.STATE expectedState) {
    if (state != expectedState) {
      throw new IllegalStateException("For this operation, the " +
                                          "current org.apache.hive.service state must be "
                                          + expectedState
                                          + " instead of " + state);
    }
  }

  /**
   * Initialize a org.apache.hive.service.
   *
   * The org.apache.hive.service state is checked <i>before</i> the operation begins.
   * This process is <i>not</i> thread safe.
   * @param service a org.apache.hive.service that must be in the state
   *   {@link Service.STATE#NOTINITED}
   * @param configuration the configuration to initialize the org.apache.hive.service with
   * @throws RuntimeException on a state change failure
   * @throws IllegalStateException if the org.apache.hive.service is in the wrong state
   */

  public static void init(Service service, HiveConf configuration) {
    Service.STATE state = service.getServiceState();
    ensureCurrentState(state, Service.STATE.NOTINITED);
    service.init(configuration);
  }

  /**
   * Start a org.apache.hive.service.
   *
   * The org.apache.hive.service state is checked <i>before</i> the operation begins.
   * This process is <i>not</i> thread safe.
   * @param service a org.apache.hive.service that must be in the state
   *   {@link Service.STATE#INITED}
   * @throws RuntimeException on a state change failure
   * @throws IllegalStateException if the org.apache.hive.service is in the wrong state
   */

  public static void start(Service service) {
    Service.STATE state = service.getServiceState();
    ensureCurrentState(state, Service.STATE.INITED);
    service.start();
  }

  /**
   * Initialize then start a org.apache.hive.service.
   *
   * The org.apache.hive.service state is checked <i>before</i> the operation begins.
   * This process is <i>not</i> thread safe.
   * @param service a org.apache.hive.service that must be in the state
   *   {@link Service.STATE#NOTINITED}
   * @param configuration the configuration to initialize the org.apache.hive.service with
   * @throws RuntimeException on a state change failure
   * @throws IllegalStateException if the org.apache.hive.service is in the wrong state
   */
  public static void deploy(Service service, HiveConf configuration) {
    init(service, configuration);
    start(service);
  }

  /**
   * Stop a org.apache.hive.service.
   *
   * Do nothing if the org.apache.hive.service is null or not in a state in which it can be/needs to be stopped.
   *
   * The org.apache.hive.service state is checked <i>before</i> the operation begins.
   * This process is <i>not</i> thread safe.
   * @param service a org.apache.hive.service or null
   */
  public static void stop(Service service) {
    if (service != null) {
      Service.STATE state = service.getServiceState();
      if (state == Service.STATE.STARTED) {
        service.stop();
      }
    }
  }

  /**
   * Stop a org.apache.hive.service; if it is null do nothing. Exceptions are caught and
   * logged at warn level. (but not Throwables). This operation is intended to
   * be used in cleanup operations
   *
   * @param service a org.apache.hive.service; may be null
   * @return any exception that was caught; null if none was.
   */
  public static Exception stopQuietly(Service service) {
    try {
      stop(service);
    } catch (Exception e) {
      LOG.warn("When stopping the org.apache.hive.service " + service.getName()
                   + " : " + e,
               e);
      return e;
    }
    return null;
  }

}
