package com.ttl.internal.vn.tool.cli;

import java.util.Map;

/**
 * Represent a log object that is built from the log source
 */
public interface ILogObject {
    /**
     * @param query: the string that will be used to gain insight into the log object
     * @return
     */
    Map<String, Object> query(String query);
}
