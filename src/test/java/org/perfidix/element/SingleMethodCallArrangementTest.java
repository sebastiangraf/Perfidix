/*
 * Copyright 2008 Distributed Systems Group, University of Konstanz
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
 * $Revision$
 * $Author$
 * $Date$
 *
 */
package org.perfidix.element;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.perfidix.annotation.Bench;

/**
 * Testcase for shuffle method arrangement.
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public class SingleMethodCallArrangementTest {

    private Set<BenchmarkElement> elemSet;

    /**
     * Before method to setUp Benchmarkables.
     * 
     * @throws Exception
     *             of any kind
     */
    @Before
    public void setUp() throws Exception {
        elemSet = new HashSet<BenchmarkElement>();
        final Class< ? > testClazz = new TestBenchClass().getClass();
        for (final Method meth : testClazz.getDeclaredMethods()) {
            if (BenchmarkMethod.isBenchmarkable(meth)) {
                elemSet
                        .add(new BenchmarkElement(
                                new BenchmarkMethod(meth)));
            }
        }
        Method meth = testClazz.getMethod("bench2");
        elemSet.add(new BenchmarkElement(new BenchmarkMethod(meth)));
        meth = testClazz.getMethod("bench2");
        elemSet.add(new BenchmarkElement(new BenchmarkMethod(meth)));
        meth = testClazz.getMethod("bench4");
        elemSet.add(new BenchmarkElement(new BenchmarkMethod(meth)));
    }

    /**
     * Test method for {@link org.perfidix.element.SingleMethodCallArrangement}
     * .
     */
    @Test
    public void test() {
        try {

            final AbstractMethodArrangement arrangement =
                    AbstractMethodArrangement.getMethodArrangement(
                            elemSet,
                            KindOfArrangement.SingleMethodCallArrangement);
            final String[] expectedNames =
                    {
                            "bench2", "bench2", "bench2", "bench4",
                            "bench4", "bench1" };
            final Iterator<BenchmarkElement> iterBench =
                    arrangement.iterator();
            final BenchmarkElement elem1 = iterBench.next();
            final BenchmarkElement elem2 = iterBench.next();
            final BenchmarkElement elem3 = iterBench.next();
            final BenchmarkElement elem4 = iterBench.next();
            final BenchmarkElement elem5 = iterBench.next();
            final BenchmarkElement elem6 = iterBench.next();
            if (expectedNames[0].equals(elem1
                    .getMeth().getMethodToBench().getName())) {
                if (expectedNames[1].equals(elem2
                        .getMeth().getMethodToBench().getName())) {
                    if (expectedNames[2].equals(elem3
                            .getMeth().getMethodToBench().getName())) {
                        if (expectedNames[3].equals(elem4
                                .getMeth().getMethodToBench().getName())) {
                            if (expectedNames[4].equals(elem5
                                    .getMeth().getMethodToBench()
                                    .getName())) {
                                if (expectedNames[5].equals(elem6
                                        .getMeth().getMethodToBench()
                                        .getName())) {
                                    fail("Something has to be arranged in a different way!");
                                }

                            }
                        }
                    }
                }
            }
            assertFalse(iterBench.hasNext());

        } catch (final Exception e) {
            fail(e.toString());
        }
    }

    class TestBenchClass {

        @Bench
        public void bench1() {
        }

        @Bench
        public void bench2() {
        }

        public void bench3() {
        }

        @Bench
        public void bench4() {
        }

    }

}
