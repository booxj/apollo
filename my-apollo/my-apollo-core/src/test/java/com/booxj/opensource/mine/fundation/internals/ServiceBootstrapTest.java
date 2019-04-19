package com.booxj.opensource.mine.fundation.internals;


import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ServiceBootstrapTest {

    @Test
    public void loadInterface1Test(){
        Interface1 service = ServiceBootstrap.loadFirst(Interface1.class);
        assertTrue(service instanceof Interface1Impl);
    }

    @Test
    public void loadInterface2Test(){
        Interface2 service = ServiceBootstrap.loadFirst(Interface2.class);
    }


    private interface Interface1 {
    }

    public static class Interface1Impl implements Interface1 {
    }

    private interface Interface2 {
    }
}
