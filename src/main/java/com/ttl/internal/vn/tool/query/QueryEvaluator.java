package com.ttl.internal.vn.tool.query;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QueryEvaluator {
    public static Object evaluate(String opName, Object... params) {
        switch (opName) {
            case "startWiths": {
                if (params.length == 2 && params[0] instanceof String && params[1] instanceof String) {
                    return ((String) params[0]).startsWith((String) params[1]);
                }
                break;
            }
            case "endWiths": {
                if (params.length == 2 && params[0] instanceof String && params[1] instanceof String) {
                    return ((String) params[0]).endsWith((String) params[1]);
                }
                break;
            }
            case "lower": {
                if (params.length == 1 && params[0] instanceof String) {
                    return ((String) params[0]).toLowerCase();
                }
                break;
            }
            case "upper": {
                if (params.length == 1 && params[0] instanceof String) {
                    return ((String) params[0]).toUpperCase();
                }
                break;
            }
            case "==":
            case "equals": {
                if (params.length == 2) {
                    return params[0].equals(params[1]);
                }
                break;
            }
            case "!=": {
                return !(boolean) evaluate("==", params);
            }
            case ">": {
                if (params.length == 2 && Comparable.class.isAssignableFrom(params[0].getClass()) && params[0].getClass().equals(params[1].getClass())) {
                    return ((Comparable) params[0]).compareTo(params[1]) > 0;
                }
            }
            case "<": {
                if (params.length == 2 && Comparable.class.isAssignableFrom(params[0].getClass()) && params[0].getClass().equals(params[1].getClass())) {
                    return ((Comparable) params[0]).compareTo(params[1]) < 0;
                }
            }
            case "<=": {
                if (params.length == 2 && Comparable.class.isAssignableFrom(params[0].getClass()) && params[0].getClass().equals(params[1].getClass())) {
                    return ((Comparable) params[0]).compareTo(params[1]) <= 0;
                }
            }
            case ">=": {
                if (params.length == 2 && Comparable.class.isAssignableFrom(params[0].getClass()) && params[0].getClass().equals(params[1].getClass())) {
                    return ((Comparable) params[0]).compareTo(params[1]) >= 0;
                }
            }
            case "not":
            case "!": {
                if (params.length == 1 && params[0] instanceof Boolean) {
                    return !(boolean) params[0];
                }
            }
            case "now": {
                return Instant.now();
            }
            case "+": {

            }
            case "-": {

            }
            case "*": {

            }
            case "/": {

            }
            case "time": {

            }
            case "duration": {

            }
        }
        throw new UnsupportedOperationException("Try to evaluate method " + opName + " with parameters " + Arrays.toString(params) + " but is not supported");
    }
}
