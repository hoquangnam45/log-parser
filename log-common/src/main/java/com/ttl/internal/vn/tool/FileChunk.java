package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.File;

@AllArgsConstructor
@Builder
@Getter
public class FileChunk {
    private final File file;
    private Range range;

    public void placeAfter(Range anotherRange) {
        range = range.after(anotherRange);
    }
}
