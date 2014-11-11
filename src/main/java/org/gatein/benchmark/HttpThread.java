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
import java.util.concurrent.CountDownLatch;

import org.exoplatform.services.cache.impl.jboss.AbstractExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author Lucas Ponce
 */
public class HttpThread extends Thread {

    static final Log LOG = ExoLogger.getLogger("HttpThread");

    final private AbstractExoCache<Serializable, Object> cache;
    final int numRequests;
    final String name;
    final CountDownLatch startSignal;
    final CountDownLatch doneSignal;
    final int requestForWritting;
    final int pageSize = 1024 * 10;

    public HttpThread(CountDownLatch startSignal,
                      CountDownLatch doneSignal,
                      AbstractExoCache<Serializable, Object> cache,
                      int numRequests,
                      int requestForWritting,
                      String name) {
        super(name);
        this.name = name;
        this.cache = cache;
        this.numRequests = numRequests;
        this.requestForWritting = requestForWritting;
        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
    }

    @Override
    public void run(){
        try {

            startSignal.await();

            long start = System.currentTimeMillis();

            for (int i = 0; i < numRequests; i++) {
                if ( i < requestForWritting) {
                    createPage();
                } else {
                    readPage();
                }
            }

            long stop = System.currentTimeMillis();

            LOG.info("----> " + name + " took " + (stop - start) + " miliseconds");

        } catch (InterruptedException e) {

        } finally {
            doneSignal.countDown();
        }
    }

    public void createPage() {
        MyKey dummyKey = new MyKey("DummyKey");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pageSize; i++) {
            sb.append(new Integer(pageSize).toString().charAt(0));
        }
        try {
            cache.put(dummyKey, sb.toString());
        } catch (Exception e) {
            LOG.error("Error on createPage(): " + e.getMessage(), e);
        }
    }

    public void readPage() {
        MyKey dummyKey = new MyKey("DummyKey");
        try {
            cache.get(dummyKey);
        } catch (Exception e) {
            LOG.error("Error on readPage(): " + e.getMessage(), e);
        }
    }
}
