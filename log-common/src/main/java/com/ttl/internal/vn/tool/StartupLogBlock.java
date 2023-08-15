package com.ttl.internal.vn.tool;

import java.util.ArrayList;
import java.util.List;

public class StartupLogBlock extends LogBlock {
    public StartupLogBlock(FileChunk chunk) {
        super(LogBlockType.STARTUP, chunk, new ArrayList<>(), new ArrayList<>());
    }
}
