/**
 * Copyright (c) 2020 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.common.book.commonmarkextensions;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;

import java.util.*;

public class MarkdownJsonRenderer extends AbstractVisitor implements NodeRenderer {
    private static final Set<Class<? extends Node>> nodeTypes = new HashSet<>(Arrays.asList(
            Text.class,
            Heading.class,
            MCColorNode.class,
            Emphasis.class,
            StrongEmphasis.class,
            Link.class,
            SoftLineBreak.class,
            HardLineBreak.class,
            ListItem.class,
            OrderedList.class,
            BulletList.class,
            Block.class
    ));

    private final TextComponentWriter writer;
    private int listIndex = 0;

    public MarkdownJsonRenderer(TextComponentWriter writer) {
        this.writer = writer;
    }

    // lombok auto getter is static which doesn't match the override
    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return nodeTypes;
    }

    @Override
    public void visit(Text text) {
        writer.addText(text.getLiteral());
        visitChildren(text);
    }

    @Override
    public void visit(Heading heading) {
        writer.setBold();
        visitChildren(heading);
        writer.addLine();
    }

    @Override
    public void visit(Emphasis node) {
        writer.setItalic();
        visitChildren(node);
    }

    @Override
    public void visit(StrongEmphasis node) {
        switch (node.getOpeningDelimiter()) {
            case "**":
                writer.setBold();
                break;
            case "__":
                writer.setUnderlined();
                break;
        }
        visitChildren(node);
    }

    @Override
    public void visit(Link link) {
        writer.setHyperlink(link.getDestination());
        writer.setMouseoverText(link.getTitle());
    }

    @Override
    public void visit(SoftLineBreak lineBreak) {
        writer.addText(" ");
    }

    @Override
    public void visit(HardLineBreak lineBreak) {
        writer.addLine();
    }

    @Override
    public void visit(Paragraph paragraph) {
        visitChildren(paragraph);
        writer.addLine();
    }

    @Override
    public void visit(OrderedList list) {
        visitList(list, 1);
    }

    @Override
    public void visit(BulletList list) {
        visitList(list, 0);
    }

    private void visitList(ListBlock list, int initialIndex) {
        writer.indent();
        int oldIndex = listIndex;
        listIndex = initialIndex;
        visitChildren(list);
        listIndex = oldIndex;
        writer.dedent();
    }

    @Override
    public void visit(ListItem item) {
        if (listIndex == 0) {
            writer.addText("• ");
        } else {
            writer.addText((listIndex++) + ". ");
        }
        visitChildren(item);
    }

    @Override
    public void visit(CustomNode node) {
        if (node instanceof MCColorNode) {
            visit((MCColorNode) node);
        } else {
            visitChildren(node);
        }
    }

    public void visit(MCColorNode color) {
        writer.setColor(ChatColor.of(color.getColorName()));
        visitChildren(color);
    }

    @Override
    protected void visitChildren(Node parent) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
            render(child);
        }
    }

    @Override
    public void render(Node node) {
        writer.enterLevel();
        node.accept(this);
        writer.exitLevel();
    }
}