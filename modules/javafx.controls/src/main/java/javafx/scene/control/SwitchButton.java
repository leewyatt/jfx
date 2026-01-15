/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.skin.SwitchButtonSkin;

/**
 * A two-state selection control that is typically rendered as a sliding thumb
 * on a track, representing on/off states.
 *
 * <p>SwitchButton is commonly used for settings or preferences that take
 * immediate effect. Unlike {@link CheckBox}, it does not support an
 * indeterminate state. Unlike {@link ToggleButton}, it is not designed
 * to be used with a {@link ToggleGroup}.
 *
 * <p>Example:
 * <pre><code> SwitchButton sw = new SwitchButton("Enable notifications");
 * sw.setSelected(true);</code></pre>
 *
 * <p>MnemonicParsing is enabled by default for SwitchButton.</p>
 *
 * @see CheckBox
 * @see ToggleButton
 */
public class SwitchButton extends ButtonBase {

    /* *************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a SwitchButton with an empty string for its label.
     */
    public SwitchButton() {
        initialize();
    }

    /**
     * Creates a SwitchButton with the specified text as its label.
     *
     * @param text a text string for its label
     */
    public SwitchButton(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
        // Use TOGGLE_BUTTON as the accessible role since AccessibleRole does not currently define a SWITCH role.
        // TOGGLE_BUTTON provides the same accessibility attributes (TEXT, SELECTED) and actions (FIRE)
        // needed for a switch control.
        setAccessibleRole(AccessibleRole.TOGGLE_BUTTON);
        setAlignment(Pos.CENTER_LEFT);
        setMnemonicParsing(true);
        pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, false);
    }

    /* *************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Indicates whether this SwitchButton is selected.
     *
     * @defaultValue false
     */
    private BooleanProperty selected;

    /**
     * Sets the selected state of this SwitchButton.
     *
     * @param value the selected state
     */
    public final void setSelected(boolean value) {
        selectedProperty().set(value);
    }

    /**
     * Returns the selected state of this SwitchButton.
     *
     * @return the selected state
     */
    public final boolean isSelected() {
        return selected == null ? false : selected.get();
    }

    /**
     * The selected state of this SwitchButton.
     *
     * @return the selected property
     * @defaultValue false
     */
    public final BooleanProperty selectedProperty() {
        if (selected == null) {
            selected = new BooleanPropertyBase(false) {
                @Override
                protected void invalidated() {
                    final boolean v = get();
                    pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, v);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
                }

                @Override
                public Object getBean() {
                    return SwitchButton.this;
                }

                @Override
                public String getName() {
                    return "selected";
                }
            };
        }
        return selected;
    }

    /* *************************************************************************
     *                                                                         *
     * Methods                                                                 *
     *                                                                         *
     **************************************************************************/

    /**
     * Toggles the state of this SwitchButton. If this SwitchButton is selected,
     * it becomes unselected. If it is unselected, it becomes selected.
     */
    @Override
    public void fire() {
        if (!isDisabled()) {
            setSelected(!isSelected());
            fireEvent(new ActionEvent());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new SwitchButtonSkin(this);
    }

    /* *************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    private static final String DEFAULT_STYLE_CLASS = "switch-button";
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

    /* *************************************************************************
     *                                                                         *
     * Accessibility handling                                                  *
     *                                                                         *
     **************************************************************************/

    /**
     * {@inheritDoc}
     */
    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        switch (attribute) {
            case SELECTED: return isSelected();
            default: return super.queryAccessibleAttribute(attribute, parameters);
        }
    }
}