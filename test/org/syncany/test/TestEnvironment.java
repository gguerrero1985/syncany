/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.syncany.test;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import org.syncany.Environment;

/**
 *
 * @author gguerrero
 */
public class TestEnvironment {

    public static void main(String[] args) {
        Properties properties = System.getProperties();

        Enumeration e = properties.propertyNames();

        System.out.println("---------------");
        System.out.println("Properties");
        System.out.println("---------------");

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            System.out.println(key + " = " + System.getProperty(key));
        }

        System.out.println("---------------");
        System.out.println("ENV");
        System.out.println("---------------");

        for (Map.Entry<String, String> es : System.getenv().entrySet()) {
            System.out.println(es.getKey() + " = " + es.getValue());
        }

        System.out.println("---------------");
        System.out.println("Stacksync ENV");
        System.out.println("---------------");

        for (Map.Entry<String, String> es : Environment.getInstance().getProperties().entrySet()) {
            System.out.println(es.getKey() + " = " + es.getValue());
        }

    }
}
