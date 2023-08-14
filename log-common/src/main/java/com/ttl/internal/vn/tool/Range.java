package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Range {
    private int begin;
    private int end;

    public Range after(Range anotherRange) {
        return moveBegin(anotherRange.getEnd() + 1, this);
    }

    public Range moveByDistance(int distance) {
        return Range.builder()
                .begin(begin + distance)
                .end(end + distance)
                .build();
    }

    public static Range moveBegin(int newBegin, Range rangeToMove) {
        return Range.builder()
                .begin(newBegin)
                .end(newBegin + rangeToMove.getEnd() - rangeToMove.getBegin())
                .build();
    }
}
