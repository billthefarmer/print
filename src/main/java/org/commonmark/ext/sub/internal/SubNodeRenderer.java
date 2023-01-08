package org.commonmark.ext.sub.internal;

import org.commonmark.ext.sub.Sub;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

import java.util.Collections;
import java.util.Set;

abstract class SubNodeRenderer implements NodeRenderer {

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.<Class<? extends Node>>singleton(Sub.class);
    }
}
