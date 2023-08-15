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

    // TODO: Implement this
    public Range findOverlap(Range anotherRange) {
        if (!isOverlap(anotherRange)) {
            return null;
        }
        return Range.builder()
                .begin(Math.)
                .end()
                .build();
    }

    public boolean isOverlap(Range anotherRange) {
        return anotherRange.begin <= end && anotherRange.end >= begin;
    }

    public Range after(Range anotherRange) {
        return moveBegin(anotherRange.getEnd() + 1, this);
    }

    public static Range moveBegin(int newBegin, Range rangeToMove) {
        return Range.builder()
                .begin(newBegin)
                .end(newBegin + rangeToMove.getEnd() - rangeToMove.getBegin())
                .build();
    }

    // TODO: Recheck to allow expand if range at front, or overlap
    public void expandRange(Range anotherRange) {
        // The range must be started immediately after chunk range
        // Validate the range
        if (anotherRange.begin - end != 1) {
            throw new IllegalStateException("Invalid usage of expand range");
        }
        end = anotherRange.end;
    }
}
