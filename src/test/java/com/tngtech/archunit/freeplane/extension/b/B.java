/*
 * Created on 7 Feb 2024
 *
 * author dimitry
 */
package com.tngtech.archunit.freeplane.extension.b;

import com.tngtech.archunit.freeplane.extension.AppModule;
import com.tngtech.archunit.freeplane.extension.a.A;

@AppModule("module B")
public class B{
    public A createA() {
        return new A();
    }
    public B1 createB1() {
        return new B1();
    }
}