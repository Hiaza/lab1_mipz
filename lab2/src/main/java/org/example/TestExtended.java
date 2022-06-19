package org.example;

abstract class TestBase {
    public int testBase = 1;
    abstract void abstractSomething();
}

interface TestInterface1 {
    void implementMe1();
}

interface TestInterface2 {
    void implementMe2();
}

class TestExtended extends TestBase implements TestInterface1, TestInterface2 {

    public int testBase = super.testBase;
    public int publicI = 1;

    @Override
    public void abstractSomething() {
        System.out.println("Not abstract anymore!");
    }

    @Override
    public void implementMe1() {
        System.out.println("Gotcha1!");
    }

    @Override
    public void implementMe2() {
        System.out.println("Gotcha2!");
    }

    private void privateStuff() {
        System.out.println("Quite private!");
    }

    public int modBy(int i) {
        return publicI + i;
    }
}

class Test extends TestExtended {

    private String privateStr = "privateString";
    private int privateInt = 100;

    public int publicInt = 1;

    public int publicI = super.publicI;


    @Override
    public void abstractSomething() {
        super.abstractSomething();
    }

    @Override
    public void implementMe1() {
        System.out.println("Overwritten! " + privateStr);
    }

    public int getNewEveryTimePrivateInt() {
        privateInt = computeNew();
        return privateInt;
    }

    private int computeNew() {
        return privateInt + 1;
    }

    @Override
    public void implementMe2() {
        super.implementMe2();
    }
}