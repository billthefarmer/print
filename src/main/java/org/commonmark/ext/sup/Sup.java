package org.commonmark.ext.sup;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

/**
 * A sup node containing text and other inline nodes as children.
 */
public class Sup extends CustomNode implements Delimited {

    private static final String DELIMITER = "^";

    @Override
    public String getOpeningDelimiter() {
        return DELIMITER;
    }

    @Override
    public String getClosingDelimiter() {
        return DELIMITER;
    }
}
