/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.gatein.benchmark;

import java.io.Serializable;

import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author Lucas Ponce
 */
public class MyListener implements CacheListener<Serializable, Object> {

    private String name;

    private static final Log LOG = ExoLogger.getLogger("MyListener");

    public MyListener(String name) {
        this.name = name;
    }

    @Override
    public void onExpire(CacheListenerContext context, Serializable key, Object obj) throws Exception {
        LOG.info("Listener: " + name + " onExpire() key: " + key + " value: " + obj);
    }

    @Override
    public void onRemove(CacheListenerContext context, Serializable key, Object obj) throws Exception {
        LOG.info("Listener: " + name + " onExpire() key: " + key + " value: " + obj);
    }

    @Override
    public void onPut(CacheListenerContext context, Serializable key, Object obj) throws Exception {
        LOG.info("Listener: " + name + " onPut() key: " + key + " value: " + obj);
    }

    @Override
    public void onGet(CacheListenerContext context, Serializable key, Object obj) throws Exception {
        LOG.info("Listener: " + name + " onGet() key: " + key + " value: " + obj);
    }

    @Override
    public void onClearCache(CacheListenerContext context) throws Exception {
        LOG.info("onClearCache()");
    }
}
