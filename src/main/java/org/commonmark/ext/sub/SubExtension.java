package org.commonmark.ext.sub;

import org.commonmark.Extension;
import org.commonmark.ext.sub.internal.SubDelimiterProcessor;
import org.commonmark.ext.sub.internal.SubHtmlNodeRenderer;
import org.commonmark.ext.sub.internal.SubTextContentNodeRenderer;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentNodeRendererFactory;
import org.commonmark.renderer.text.TextContentRenderer;

/**
 * Extension for sub using ~
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The parsed sub text regions are turned into {@link Sub} nodes.
 * </p>
 */
public class SubExtension implements Parser.ParserExtension,
        HtmlRenderer.HtmlRendererExtension,
        TextContentRenderer.TextContentRendererExtension {

    private SubExtension() {
    }

    public static Extension create() {
        return new SubExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new SubDelimiterProcessor());
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new HtmlNodeRendererFactory() {
            @Override
            public NodeRenderer create(HtmlNodeRendererContext context) {
                return new SubHtmlNodeRenderer(context);
            }
        });
    }

    @Override
    public void extend(TextContentRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new TextContentNodeRendererFactory() {
            @Override
            public NodeRenderer create(TextContentNodeRendererContext context) {
                return new SubTextContentNodeRenderer(context);
            }
        });
    }
}
