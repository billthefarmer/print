package org.commonmark.ext.sub.internal;

import org.commonmark.ext.sub.Sub;
import org.commonmark.node.Node;
import org.commonmark.node.Nodes;
import org.commonmark.node.SourceSpans;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

public class SubDelimiterProcessor implements DelimiterProcessor {

    @Override
    public char getOpeningCharacter() {
        return '~';
    }

    @Override
    public char getClosingCharacter() {
        return '~';
    }

    @Override
    public int getMinLength() {
        return 1;
    }

    @Override
    public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
        if (openingRun.length() == 1 && closingRun.length() == 1) {
            // Use exactly one delimiter.

            Text opener = openingRun.getOpener();

            // Wrap nodes between delimiters in sub.
            Node sub = new Sub();

            SourceSpans sourceSpans = new SourceSpans();
            sourceSpans.addAllFrom(openingRun.getOpeners(1));

            for (Node node : Nodes.between(opener, closingRun.getCloser())) {
                sub.appendChild(node);
                sourceSpans.addAll(node.getSourceSpans());
            }

            sourceSpans.addAllFrom(closingRun.getClosers(1));
            sub.setSourceSpans(sourceSpans.getSourceSpans());

            opener.insertAfter(sub);

            return 1;
        } else {
            return 0;
        }
    }
}
