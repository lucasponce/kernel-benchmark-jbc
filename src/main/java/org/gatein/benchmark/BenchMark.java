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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.impl.jboss.AbstractExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author Lucas Ponce
 */
public class BenchMark {

    static final Log LOG = ExoLogger.getLogger("BenchMark");

    static CacheService service;
    static AbstractExoCache<Serializable, Object> cache;

    public static void initCache() throws Exception {
        service = (CacheService)PortalContainer.getInstance().getComponentInstanceOfType(CacheService.class);
        cache = (AbstractExoCache<Serializable, Object>)service.getCacheInstance("BenchMarkCache");
    }

    public static void syncCluster(String node, int clusterSize) throws Exception {
        Set<String> topology = (Set<String>)cache.get("topology");
        while (topology == null || topology.size() < clusterSize) {
            if (topology == null) {
                topology = new HashSet<String>();
                topology.add(node);
                cache.put("topology", topology);
            } else {
                if (!topology.contains(node)) {
                    topology.add(node);
                    cache.put("topology", topology);
                }
            }
            Thread.sleep(2 * 1000);
            topology = (Set<String>)cache.get("topology");
        }
    }

    /**
     * Expected arguments:
     * - Node name of the cluster
     * - Expected cluster size, test will be waiting for other nodes to join into cluster
     * - Number of threads writing/reading to/from cache
     * - Number of requests to be executed by thread
     * - Number of writting request per thread
     * - true/false flag to exit process after finish
     */
    public static void main(String[] args) throws Exception {

        String node = "single";
        int clusterSize = 1;
        int nThreads = 10;
        int nRequestPerThread = 10;
        int nWriteRequestsPerThread = 5;
        boolean finish = false;

        if (args.length == 6) {
            node = args[0];
            clusterSize = new Integer(args[1]);
            nThreads = new Integer(args[2]);
            nRequestPerThread = new Integer(args[3]);
            nWriteRequestsPerThread = new Integer(args[4]);
            finish = new Boolean(args[5]);
        }

        LOG.info("-> Test using " + nThreads + " threads and " + nRequestPerThread + " request/thread");

        initCache();
        syncCluster(node, clusterSize);

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(nThreads);

        for (int i = 0; i < nThreads; i++) {
            new HttpThread(startSignal, doneSignal, cache, nRequestPerThread, nWriteRequestsPerThread, "HttpThread-" + i).start();
        }

        long start = System.currentTimeMillis();

        startSignal.countDown();
        doneSignal.await();

        long stop = System.currentTimeMillis();

        LOG.info("-> Test took " + (stop - start) + " miliseconds ");

        if (finish) {
            System.exit(0);
        }
    }
}
