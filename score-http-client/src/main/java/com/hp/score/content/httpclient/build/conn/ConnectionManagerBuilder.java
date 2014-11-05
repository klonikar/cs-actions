/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.content.httpclient.build.conn;

import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SessionResource;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import com.hp.score.content.httpclient.HttpClientInputs;

import java.util.HashMap;
import java.util.Map;

public class ConnectionManagerBuilder {
    private GlobalSessionObject<Map<String, PoolingHttpClientConnectionManager>> connectionPoolHolder;
    private SSLConnectionSocketFactory sslsf;
    private String connectionManagerMapKey;
    private String defaultMaxPerRoute;
    private String totalMax;

    public ConnectionManagerBuilder setConnectionPoolHolder(GlobalSessionObject connectionPoolHolder) {
        this.connectionPoolHolder = connectionPoolHolder;
        return this;
    }

    public ConnectionManagerBuilder setSslsf(SSLConnectionSocketFactory sslsf) {
        this.sslsf = sslsf;
        return this;
    }

    public ConnectionManagerBuilder setTotalMax(String totalMax) {
        this.totalMax = totalMax;
        return this;
    }

    public ConnectionManagerBuilder setDefaultMaxPerRoute(String defaultMaxPerRoute) {
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        return this;
    }

    public ConnectionManagerBuilder setConnectionManagerMapKey(String... connectionManagerMapKeys) {
        this.connectionManagerMapKey = buildConnectionManagerMapKey(connectionManagerMapKeys);
        return this;
    }

    public static String buildConnectionManagerMapKey(String... connectionManagerMapKeys) {
        StringBuilder keyBuilder = new StringBuilder();
        for (String token : connectionManagerMapKeys) {
            keyBuilder.append(token).append(":");
        }
        if (keyBuilder.length() > 0) {
            keyBuilder.deleteCharAt(keyBuilder.length() - 1);
        }
        return keyBuilder.toString();
    }

    public  PoolingHttpClientConnectionManager buildConnectionManager() {
        if (connectionPoolHolder != null) {
            Map<String, PoolingHttpClientConnectionManager> connectionManagerMap
                    = connectionPoolHolder.get();
            PoolingHttpClientConnectionManager connManager = null;
            if (connectionManagerMap != null) {
                connManager = connectionManagerMap.get(connectionManagerMapKey);
            }
            if (connManager == null) {
                synchronized(connectionPoolHolder) {
                    connectionManagerMap = connectionPoolHolder.get();

                    if (connectionManagerMap == null) {
                        final HashMap<String, PoolingHttpClientConnectionManager> connectionManagerMapFinal  = new HashMap<>();
                        connectionPoolHolder.setResource(new SessionResource<Map<String, PoolingHttpClientConnectionManager>>(){
                            @Override
                            public Map<String, PoolingHttpClientConnectionManager> get() {
                                return connectionManagerMapFinal;
                            }
                            @Override
                            public void release() {
                            }
                        });
                        connectionManagerMap = connectionPoolHolder.get();
                    }

                    connManager = connectionManagerMap.get(connectionManagerMapKey);
                    if (connManager == null) {
                        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                                .register("https", sslsf)
                                .build();
                        connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

                        connectionManagerMap.put(connectionManagerMapKey, connManager);
                    }
                }
            }
            //the DefaultMaxPerRoute default is 2
            if (!StringUtils.isEmpty(defaultMaxPerRoute)) {
                try {
                    connManager.setDefaultMaxPerRoute(Integer.parseInt(defaultMaxPerRoute));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("the '"+ HttpClientInputs.CONNECTIONS_MAX_PER_ROUTE
                            +"' input should be integer" +e.getMessage(), e);
                }
            }
            //the Default totalMax default is 20
            if (!StringUtils.isEmpty(totalMax)) {
                try {
                    connManager.setMaxTotal(Integer.parseInt(totalMax));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("the '"+ HttpClientInputs.CONNECTIONS_MAX_TOTAL
                            +"' input should be integer" +e.getMessage(), e);
                }
            }
            return connManager;
        }
        return null;
    }
}