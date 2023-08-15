package com.ttl.internal.vn.tool;

import java.util.ArrayList;
import java.util.List;

public class RollingLogBlock extends LogBlock {
    public RollingLogBlock(FileChunk chunk) {
        super(LogBlockType.ROLL_FILE, chunk, new ArrayList<>(), new ArrayList<>());
    }
}
