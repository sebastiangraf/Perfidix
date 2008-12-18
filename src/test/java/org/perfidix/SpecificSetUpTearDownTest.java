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
 * $Id: SpecificSetUpTearDownTest.java 2624 2007-03-28 15:08:52Z kramis $
 * 
 */

package org.perfidix;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.AfterLastRun;
import org.perfidix.annotation.BeforeEachRun;
import org.perfidix.annotation.BeforeFirstRun;
import org.perfidix.annotation.Bench;
import org.perfidix.depreacted.Benchmark;

public class SpecificSetUpTearDownTest {

    private boolean firstRun;

    private boolean setUp;

    private boolean tearDown;

    private boolean lastRun;

    private boolean specialFirstRun;

    private boolean specialSetUp;

    private boolean specialTearDown;

    private boolean specialLastRun;

    private boolean benchWithout;

    private boolean benchWith;

    @Before
    public void setUp() {
        firstRun = false;
        setUp = false;
        tearDown = false;
        lastRun = false;
        specialFirstRun = false;
        specialSetUp = false;
        specialTearDown = false;
        specialLastRun = false;
        benchWithout = false;
        benchWith = false;
    }

    @Test
    @Ignore
    public void testWithout() {
        final Without test = new Without();
        final Benchmark benchMark = new Benchmark();
        benchMark.add(test);
        benchMark.run(1);
        assertTrue(firstRun);
        assertTrue(setUp);
        assertTrue(tearDown);
        assertTrue(lastRun);
        assertFalse(specialFirstRun);
        assertFalse(specialSetUp);
        assertFalse(specialTearDown);
        assertFalse(specialLastRun);
        assertTrue(benchWithout);
        assertFalse(benchWith);
    }

    @Test
    @Ignore
    public void testWith() {
        final With test = new With();
        final Benchmark benchMark = new Benchmark();
        benchMark.add(test);
        benchMark.run(1);
        assertFalse(firstRun);
        assertFalse(setUp);
        assertFalse(tearDown);
        assertFalse(lastRun);
        assertTrue(specialFirstRun);
        assertTrue(specialSetUp);
        assertTrue(specialTearDown);
        assertTrue(specialLastRun);
        assertFalse(benchWithout);
        assertTrue(benchWith);
    }

    class Without {

        @BeforeFirstRun
        public void firstRun() {
            firstRun = true;
        }

        @BeforeEachRun
        public void setUp() {
            setUp = true;
        }

        @AfterEachRun
        public void tearDown() {
            tearDown = true;
        }

        @AfterLastRun
        public void lastRun() {
            lastRun = true;
        }

        public void specialFirstRun() {
            specialFirstRun = true;
        }

        public void specialSetUp() {
            specialSetUp = true;
        }

        public void specialTearDown() {
            specialTearDown = true;
        }

        public void specialLastRun() {
            specialLastRun = true;
        }

        @Bench
        public void bench() {
            benchWithout = true;
        }
    }

    class With {
        @BeforeFirstRun
        public void firstRun() {
            firstRun = true;
        }

        @BeforeEachRun
        public void setUp() {
            setUp = true;
        }

        @AfterEachRun
        public void tearDown() {
            tearDown = true;
        }

        @AfterLastRun
        public void lastRun() {
            lastRun = true;
        }

        public void specialFirstRun() {
            specialFirstRun = true;
        }

        public void specialSetUp() {
            specialSetUp = true;
        }

        public void specialTearDown() {
            specialTearDown = true;
        }

        public void specialLastRun() {
            specialLastRun = true;
        }

        @Bench(beforeEachRun = "specialSetUp", afterEachRun = "specialTearDown", beforeFirstRun = "specialFirstRun", afterLastRun = "specialLastRun")
        public void bench() {
            benchWith = true;
        }

    }

}
