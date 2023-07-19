package com.ttl.internal.vn.tool.cli;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Each log query node represent completed node that could be used to construct a query in itself
// multiple log query node could be joined together using AND, OR expression, itself could contains
// multiple smaller query nodes
// An atomic log query nodes contain no bracket, no AND, OR expression and could be turned directly into
// a query without further processing
@Getter
public class LogQueryNode {
    private final Pair<Integer, Integer> range;
    private final List<LogQueryNode> childNodes = new ArrayList<>();

    public LogQueryNode(Pair<Integer, Integer> range) {
        this.range = range;
    }

    // Return whether this range contains the new range
    private boolean contains(Pair<Integer, Integer> newRange) {
        return range.getLeft() <= newRange.getLeft() && range.getRight() >= newRange.getRight();
    }

    public void addChild(Pair<Integer, Integer> childRange) {
        if (!contains(childRange)) {
            return;
        }
        for (LogQueryNode node : childNodes) {
            if (node.contains(childRange)) {
                node.addChild(childRange);
                return;
            }
        }
        childNodes.add(new LogQueryNode(childRange));
    }

    public LogQueryNode and(LogQueryNode sameLevelNode) {

    }

    //
    public List<LogQueryNode> evaluateOrder(String queryString) {
        List<Pair<Integer, Integer>> atomicRanges = new ArrayList<>();

        // Inverse all the range that in child nodes
        List<Pair<Integer, Integer>> ranges = new ArrayList<>();
        if (childNodes.size() == 0) {
            ranges.add(getRange());
        } else {
            ranges.add(Pair.of(range.getLeft(), childNodes.get(0).getRange().getLeft()));
            ranges.addAll(childNodes.stream().map(LogQueryNode::getRange).collect(Collectors.toList()));
            ranges.add(Pair.of(childNodes.get(childNodes.size() - 1).getRange().getRight(), range.getRight()));
        }

        // Segment in inverse ranges for AND, OR expressions
        ranges.stream().map(it -> queryString.substring(0, ))
        // Segment ranges further into atomic ranges
        for (Pair<Integer, Integer> range : ranges) {
            String substring = queryString
        }
    }
}
