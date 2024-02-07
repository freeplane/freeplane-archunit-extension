/*
 * Created on 7 Feb 2024
 *
 * author dimitry
 */
package com.tngtech.archunit.freeplane.extension.a;

import com.tngtech.archunit.freeplane.extension.AppModule;
import com.tngtech.archunit.freeplane.extension.b.B;

@AppModule("module A")
public class A{
    public B createB() {
        return new B();
    }
    public A1 createA1() {
        return new A1();
    }
}