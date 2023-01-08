package org.commonmark.ext.sup.internal;

import org.commonmark.ext.sup.Sup;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

import java.util.Collections;
import java.util.Set;

abstract class SupNodeRenderer implements NodeRenderer {

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.<Class<? extends Node>>singleton(Sup.class);
    }
}
