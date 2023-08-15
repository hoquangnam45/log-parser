package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class Line {
    private final String line;
    private final int lineNumber;

    public Range toRange() {
        return Range.builder()
                .begin(lineNumber)
                .end(lineNumber)
                .build();
    }
}
