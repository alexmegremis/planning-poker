package com.alexmegremis.planningpoker;

import com.vaadin.server.FontAwesome;
import org.springframework.util.StringUtils;

public abstract class Hideable {

    private boolean hidden = true;

    protected abstract String getHideableValue();

    public void hide() {
        hidden = true;
    }

    public void reveal() {
        hidden = false;
    }

    public void toggle() {
        hidden = !hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public String getHideable() {
        String result;
        if(StringUtils.isEmpty(getHideableValue())) {
            result = "";
        } else result = hidden ? FontAwesome.EYE_SLASH.getHtml() : getHideableValue();

        return result;
    }
}
