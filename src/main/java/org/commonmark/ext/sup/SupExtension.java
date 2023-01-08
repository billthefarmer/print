package org.commonmark.ext.sup;

import org.commonmark.Extension;
import org.commonmark.ext.sup.internal.SupDelimiterProcessor;
import org.commonmark.ext.sup.internal.SupHtmlNodeRenderer;
import org.commonmark.ext.sup.internal.SupTextContentNodeRenderer;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentNodeRendererFactory;
import org.commonmark.renderer.text.TextContentRenderer;

/**
 * Extension for sup using ^
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The parsed sup text regions are turned into {@link Sup} nodes.
 * </p>
 */
public class SupExtension implements Parser.ParserExtension,
        HtmlRenderer.HtmlRendererExtension,
        TextContentRenderer.TextContentRendererExtension {

    private SupExtension() {
    }

    public static Extension create() {
        return new SupExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new SupDelimiterProcessor());
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new HtmlNodeRendererFactory() {
            @Override
            public NodeRenderer create(HtmlNodeRendererContext context) {
                return new SupHtmlNodeRenderer(context);
            }
        });
    }

    @Override
    public void extend(TextContentRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new TextContentNodeRendererFactory() {
            @Override
            public NodeRenderer create(TextContentNodeRendererContext context) {
                return new SupTextContentNodeRenderer(context);
            }
        });
    }
}
