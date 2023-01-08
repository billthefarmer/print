package org.commonmark.ext.sup.internal;

import org.commonmark.node.Node;
import org.commonmark.renderer.text.TextContentNodeRendererContext;

public class SupTextContentNodeRenderer extends SupNodeRenderer {

    private final TextContentNodeRendererContext context;

    public SupTextContentNodeRenderer(TextContentNodeRendererContext context) {
        this.context = context;
    }

    @Override
    public void render(Node node) {
        renderChildren(node);
    }

    private void renderChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }
}
