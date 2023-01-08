package org.commonmark.ext.sub.internal;

import org.commonmark.node.Node;
import org.commonmark.renderer.text.TextContentNodeRendererContext;

public class SubTextContentNodeRenderer extends SubNodeRenderer {

    private final TextContentNodeRendererContext context;

    public SubTextContentNodeRenderer(TextContentNodeRendererContext context) {
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
