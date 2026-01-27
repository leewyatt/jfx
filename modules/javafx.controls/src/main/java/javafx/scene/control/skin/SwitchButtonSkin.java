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

package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.ButtonBehavior;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HorizontalDirection;
import javafx.scene.control.SwitchButton;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Default skin implementation for the {@link SwitchButton} control.
 *
 * @see SwitchButton
 */
public class SwitchButtonSkin extends LabeledSkinBase<SwitchButton> {

    /* *************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/

    /**
     * The track background of the switch.
     */
    private final StackPane track;

    /**
     * The thumb that slides along the track.
     */
    private final StackPane thumb;

    private final BehaviorBase<SwitchButton> behavior;

    private final Timeline timeline;

    /**
     * The thumb position ratio (0.0 = unselected, 1.0 = selected).
     * Using a ratio allows correct positioning during resize.
     */
    private final DoubleProperty thumbPosition = new SimpleDoubleProperty();

    /**
     * Listener for thumbPosition changes, stored for removal in dispose().
     */
    private final ChangeListener<Number> thumbPositionListener;

    /**
     * The X coordinate of the thumb at position 0.
     */
    private double thumbStartX;

    /**
     * The distance the thumb travels from position 0 to position 1.
     */
    private double thumbMoveRange;

    /* *************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new SwitchButtonSkin instance, installing the necessary child
     * nodes into the control's children list.
     *
     * @param control the control that this skin should be installed onto
     */
    public SwitchButtonSkin(SwitchButton control) {
        super(control);

        behavior = new ButtonBehavior<>(control);

        track = new StackPane();
        track.getStyleClass().setAll("track");

        thumb = new StackPane();
        thumb.getStyleClass().setAll("thumb");

        timeline = new Timeline();

        thumbPositionListener = (obs, oldVal, newVal) -> thumb.setLayoutX(thumbStartX + thumbMoveRange * newVal.doubleValue());
        thumbPosition.addListener(thumbPositionListener);

        thumbPosition.set(control.isSelected() ? 1.0 : 0.0);

        registerChangeListener(control.selectedProperty(), e -> selectedStateChanged());
        registerChangeListener(control.switchPositionProperty(), e -> control.requestLayout());

        updateChildren();
    }

    /* *************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        timeline.stop();
        timeline.getKeyFrames().clear();

        thumbPosition.removeListener(thumbPositionListener);

        if (behavior != null) {
            behavior.dispose();
        }

        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateChildren() {
        super.updateChildren();
        if (track != null && thumb != null) {
            getChildren().addAll(track, thumb);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset,
                                     double bottomInset, double leftInset) {
        final double switchWidth = computeSwitchWidth();
        return super.computeMinWidth(height, topInset, rightInset, bottomInset, leftInset)
                + snapSizeX(switchWidth);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        final double switchWidth = computeSwitchWidth();
        final double switchHeight = computeSwitchHeight();
        return Math.max(
                super.computeMinHeight(width - switchWidth, topInset, rightInset, bottomInset, leftInset),
                topInset + switchHeight + bottomInset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        final double switchWidth = computeSwitchWidth();
        return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset)
                + snapSizeX(switchWidth);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset,
                                       double bottomInset, double leftInset) {
        final double switchWidth = computeSwitchWidth();
        final double switchHeight = computeSwitchHeight();
        return Math.max(
                super.computePrefHeight(width - switchWidth, topInset, rightInset, bottomInset, leftInset),
                topInset + switchHeight + bottomInset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutChildren(final double x, final double y, final double w, final double h) {
        final SwitchButton switchButton = getSkinnable();

        final double trackWidth = snapSizeX(track.prefWidth(-1));
        final double trackHeight = snapSizeY(track.prefHeight(-1));
        final double thumbWidth = snapSizeX(thumb.prefWidth(-1));
        final double thumbHeight = snapSizeY(thumb.prefHeight(-1));

        final double switchWidth = computeSwitchWidth();
        final double switchHeight = computeSwitchHeight();

        final double controlWidth = Math.max(switchButton.prefWidth(-1), switchButton.minWidth(-1));
        final double labelWidth = Math.min(controlWidth - switchWidth, w - snapSizeX(switchWidth));
        final double labelHeight = Math.min(switchButton.prefHeight(labelWidth), h);
        final double maxHeight = Math.max(switchHeight, labelHeight);

        final double xOffset = Utils.computeXOffset(w, labelWidth + switchWidth,
                switchButton.getAlignment().getHpos()) + x;
        final double yOffset = Utils.computeYOffset(h, maxHeight,
                switchButton.getAlignment().getVpos()) + y;

        final boolean switchOnRight = switchButton.getSwitchPosition() != HorizontalDirection.LEFT;
        final double labelX;
        final double switchX;
        if (switchOnRight) {
            labelX = xOffset;
            switchX = xOffset + labelWidth;
        } else {
            switchX = xOffset;
            labelX = xOffset + switchWidth;
        }

        layoutLabelInArea(labelX, yOffset, labelWidth, maxHeight, switchButton.getAlignment());

        track.resize(trackWidth, trackHeight);
        final double trackX = switchX + (switchWidth - trackWidth) / 2;
        final double trackY = yOffset + (maxHeight - trackHeight) / 2;
        track.setLayoutX(trackX);
        track.setLayoutY(trackY);

        // Calculate thumb movement range
        if (thumbWidth <= trackWidth) {
            thumbMoveRange = trackWidth - thumbWidth;
            thumbStartX = trackX;
        } else {
            // Edge case: thumb is wider than track
            thumbMoveRange = 0;
            thumbStartX = switchX + (switchWidth - thumbWidth) / 2;
        }

        // Thumb vertical position: centered within track
        final double thumbY = trackY + (trackHeight - thumbHeight) / 2;

        thumb.resize(thumbWidth, thumbHeight);
        thumb.setLayoutX(thumbStartX + thumbMoveRange * thumbPosition.get());
        thumb.setLayoutY(thumbY);
    }

    /* *************************************************************************
     *                                                                         *
     * Private implementation                                                  *
     *                                                                         *
     **************************************************************************/

    private double computeSwitchWidth() {
        final double trackWidth = Math.max(track.minWidth(-1), track.prefWidth(-1));
        final double thumbWidth = Math.max(thumb.minWidth(-1), thumb.prefWidth(-1));
        return Math.max(trackWidth, thumbWidth);
    }

    private double computeSwitchHeight() {
        final double trackHeight = Math.max(track.minHeight(-1), track.prefHeight(-1));
        final double thumbHeight = Math.max(thumb.minHeight(-1), thumb.prefHeight(-1));
        return Math.max(trackHeight, thumbHeight);
    }

    private void selectedStateChanged() {
        final double targetPosition = getSkinnable().isSelected() ? 1.0 : 0.0;

        timeline.stop();
        timeline.getKeyFrames().setAll(
                new KeyFrame(Duration.millis(150),
                        new KeyValue(thumbPosition, targetPosition, Interpolator.EASE_BOTH))
        );
        timeline.play();
    }
}