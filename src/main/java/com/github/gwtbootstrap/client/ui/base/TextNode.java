package com.github.gwtbootstrap.client.ui.base;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

public class TextNode extends Widget implements HasText {

    private SpanElement baseNode;
    private boolean attached;
    
    public TextNode() {
    }

    public TextNode(String text) {
        setText(text);
    }


    @Override
    public String getText() {
        
        return baseNode != null ? baseNode.getInnerText() : null;
    }


    @Override
    public void setText(String text) {
        assert baseNode == null : "TextNode can be set once";
        // TODO this should be a Text node but that cannot be cast to element
//        baseNode = Document.get().createTextNode(text);
//        setElement(baseNode.<Element>cast());
      baseNode = Document.get().createSpanElement();
      baseNode.setInnerText(text);
      setElement(baseNode);
    }
    
    @Override
    public boolean isAttached() {
        return attached;
    }
    
    @Override
    protected void onAttach() {
        
        if(isAttached()) {
            throw new IllegalStateException("already added");
        }
        
        this.attached = true;
        
        onLoad();
        
        AttachEvent.fire(this, attached);
    }
    
    @Override
    protected void onDetach() {
        
        if(!isAttached()) {
            throw new IllegalStateException("is not attached");
        }
        
        this.attached = false;
        
        AttachEvent.fire(this, attached);
    }
    
    
}
