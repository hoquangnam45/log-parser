package com.ttl.internal.vn.tool;

import io.reactivex.rxjava3.core.Observable;

import java.util.ArrayList;
import java.util.List;

public class LogReducer {
    private List<StartupLogBlock> startupLogBlocks = new ArrayList<>();

    public Observable<LogSession> reduce(Observable<LogBlock> blocks) {
        // Step 1: Sort by log timestamp
        blocks.
        // Step 2: Group all rolling blocks into startup blocks

        // Step 3: Create a log session
    }


}
