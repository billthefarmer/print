package org.commonmark.ext.sub.internal;

import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.Collections;
import java.util.Map;

public class SubHtmlNodeRenderer extends SubNodeRenderer {

    private final HtmlNodeRendererContext context;
    private final HtmlWriter html;

    public SubHtmlNodeRenderer(HtmlNodeRendererContext context) {
        this.context = context;
        this.html = context.getWriter();
    }

    @Override
    public void render(Node node) {
        Map<String, String> attributes = context.extendAttributes(node, "sub", Collections.<String, String>emptyMap());
        html.tag("sub", attributes);
        renderChildren(node);
        html.tag("/sub");
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
