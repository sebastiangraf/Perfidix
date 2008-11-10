/*
 * Copyright 2007 University of Konstanz
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
 * 
 * $Id: Perfidix.java 2624 2007-03-28 15:08:52Z kramis $
 * 
 */

package org.perfidix;

import java.util.TreeMap;

import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.MilliMeter;
import org.perfidix.result.AbstractResult;
import org.perfidix.result.SingleResult;

/**
 * this is the main class, consisting of all the factory methods needed in order
 * to perform a benchmark run.
 * 
 * @author axo
 */
public final class Perfidix {

    /**
     * a hashtable of meters .
     */
    private TreeMap<String, AbstractMeter> meters =
            new TreeMap<String, AbstractMeter>();

    private static Perfidix instance = new Perfidix();

    /**
     * the memory unit's string.
     */
    public static final String MEM_UNIT = "B";

    /**
     * the memory unit's description.
     */
    public static final String MEM_DESCRIPTION = "bytes";

    /**
     * the millisecond unit's string.
     */
    public static final String MSEC_UNIT = "ms";

    /**
     * the millisecond unit's description.
     */
    public static final String MSEC_DESCRIPTION = "milliseconds";

    /**
     * the nanometer unit string.
     */
    public static final String NANO_UNIT = "ns";

    /**
     * the nanometer unit description.
     */
    public static final String NANO_DESCRIPTION = "nanoseconds";

    /**
     * the second unit string
     */
    public static final String SECOND_UNIT = "sec";

    /**
     * the second unit description
     */
    public static final String SECOND_DESCRIPTION = "seconds";

    /**
     * the default unit.
     */
    public static final String DEFAULT_UNIT = "-";

    /**
     * the default description for the counters.
     */
    public static final String DEFAULT_DESCRIPTION = "- no description -";

    /**
     * the default counter name (if no counter name given).
     */
    public static final String DEFAULT_COUNTER_NAME = "counter";

    /**
     * the default initial value for the counter.
     */
    public static final int DEFAULT_COUNTER_INITVALUE = 0;

    /**
     * used as a hashCode seed in the classes overriding Object.hashCode().
     */
    public static final int HASHCODE_SEED = 17;

    /**
     * a prime number as a multiplier for the hashCode() overriding.
     */
    public static final int HASHCODE_PRIME = 37;

    public static final AbstractMeter DEFAULTMETER = new MilliMeter();

    /**
     * private constructor.
     */
    private Perfidix() {

    }

    /**
     * @return
     */
    private static Perfidix getInstance() {
        if (instance == null) {
            instance = new Perfidix();
        }
        return instance;
    }

    /**
     * Method to register Meters
     * 
     * @param meter
     *            to register
     */
    public static void registerMeter(final AbstractMeter meter) {
        Perfidix.getInstance().meters.put(meter.getName(), meter);
    }

    /**
     * returns the meter assigned to by name. if the meter does not exist, it
     * will be created.
     * 
     * @param meterName
     *            the name of the meter.
     * @return the meter assigned to. if it has not been created, null will be
     *         returned.
     */
    public static AbstractMeter getMeter(final String meterName) {
        Perfidix p = Perfidix.getInstance();
        if (!p.meters.containsKey(meterName)) {
            return null;
        }
        return p.meters.get(meterName);
    }

    /**
     * @return the registered meters.
     */
    static TreeMap<String, AbstractMeter> getRegisteredMeters() {
        return Perfidix.getInstance().meters;
    }

    /**
     * @return a benchmark.
     */
    public static Benchmark createBenchmark() {
        return new Benchmark();
    }

    /**
     * @return the default meter.
     */
    public static final AbstractMeter defaultMeter() {
        return new MilliMeter();
    }

    /**
     * @param set
     *            the dataset.
     * @return the single result.
     */
    public static final SingleResult createSingleResult(final double[] set) {
        return new SingleResult(set, Perfidix.defaultMeter());
    }

    /**
     * @param name
     *            the name.
     * @param set
     *            the result set.
     * @return a single result.
     */
    public static final SingleResult createSingleResult(
            final String name, final double[] set) {
        SingleResult r = Perfidix.createSingleResult(set);
        // r.setName(name);
        return r;
    }

    public static AbstractResult runBenchs(final String[] benchs) {
        Benchmark bench = new Benchmark();
        bench.setLogger(false);
        for (String each : benchs) {
            try {
                bench.add(Class.forName(each).newInstance());
            } catch (ClassNotFoundException e) {
                System.out.println("Could not find class: " + each);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return bench.run();
    }

    public static void main(final String[] args) {
        System.out.println(runBenchs(args).toString());

    }

}
