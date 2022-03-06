/*-
 * #%L
 * QR Invoice Solutions
 * %%
 * Copyright (C) 2017 - 2022 Codeblock GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * -
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * -
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * -
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses are available for this software. These replace the above 
 * AGPLv3 terms and offer support, maintenance and allow the use in commercial /
 * proprietary products.
 * -
 * More information on commercial licenses are available at the following page:
 * https://www.qr-invoice.ch/licenses/
 * #L%
 */
package ch.codeblock.qrinvoice.graphics;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;

/**
 * Source: https://www.paymentstandards.ch/de/shared/downloads.html
 * 
 * This class has been automatically generated using
 * <a href="https://flamingo.dev.java.net">Flamingo SVG transcoder</a>.
 */
public class SwissCross {

    /**
     * Paints the transcoded SVG image on the specified graphics context. You
     * can install a custom transformation on the graphics context to scale the
     * image.
     * 
     * @param g Graphics context.
     */
    public static void paint(Graphics2D g) {
        Shape shape = null;
        
        float origAlpha = 1.0f;
        Composite origComposite = ((Graphics2D)g).getComposite();
        if (origComposite instanceof AlphaComposite) {
            AlphaComposite origAlphaComposite = (AlphaComposite)origComposite;
            if (origAlphaComposite.getRule() == AlphaComposite.SRC_OVER) {
                origAlpha = origAlphaComposite.getAlpha();
            }
        }
        
        java.util.LinkedList<AffineTransform> transformations = new java.util.LinkedList<AffineTransform>();
        

        // 
        transformations.offer(g.getTransform());
        g.transform(new AffineTransform(0.050505053f, 0, 0, 0.050505053f, 0, 0));

        // _0

        // _0_0
        shape = new GeneralPath();
        ((GeneralPath) shape).moveTo(18.3, 0.7);
        ((GeneralPath) shape).lineTo(1.6, 0.7);
        ((GeneralPath) shape).lineTo(0.7, 0.7);
        ((GeneralPath) shape).lineTo(0.7, 1.6);
        ((GeneralPath) shape).lineTo(0.7, 18.3);
        ((GeneralPath) shape).lineTo(0.7, 19.1);
        ((GeneralPath) shape).lineTo(1.6, 19.1);
        ((GeneralPath) shape).lineTo(18.3, 19.1);
        ((GeneralPath) shape).lineTo(19.1, 19.1);
        ((GeneralPath) shape).lineTo(19.1, 18.3);
        ((GeneralPath) shape).lineTo(19.1, 1.6);
        ((GeneralPath) shape).lineTo(19.1, 0.7);
        ((GeneralPath) shape).closePath();

        g.setPaint(BLACK);
        g.fill(shape);

        // _0_1
        shape = new Rectangle2D.Double(8.300000190734863, 4, 3.299999952316284, 11);
        g.setPaint(WHITE);
        g.fill(shape);

        // _0_2
        shape = new Rectangle2D.Double(4.400000095367432, 7.900000095367432, 11, 3.299999952316284);
        g.fill(shape);

        // _0_3
        shape = new GeneralPath();
        ((GeneralPath) shape).moveTo(0.7, 1.6);
        ((GeneralPath) shape).lineTo(0.7, 18.3);
        ((GeneralPath) shape).lineTo(0.7, 19.1);
        ((GeneralPath) shape).lineTo(1.6, 19.1);
        ((GeneralPath) shape).lineTo(18.3, 19.1);
        ((GeneralPath) shape).lineTo(19.1, 19.1);
        ((GeneralPath) shape).lineTo(19.1, 18.3);
        ((GeneralPath) shape).lineTo(19.1, 1.6);
        ((GeneralPath) shape).lineTo(19.1, 0.7);
        ((GeneralPath) shape).lineTo(18.3, 0.7);
        ((GeneralPath) shape).lineTo(1.6, 0.7);
        ((GeneralPath) shape).lineTo(0.7, 0.7);
        ((GeneralPath) shape).closePath();

        g.setStroke(new BasicStroke(1.4357f, 0, 0, 10));
        g.draw(shape);

        g.setTransform(transformations.poll()); // _0

    }

    /**
     * Returns the X of the bounding box of the original SVG image.
     * 
     * @return The X of the bounding box of the original SVG image.
     */
    public static int getOrigX() {
        return 0;
    }

    /**
     * Returns the Y of the bounding box of the original SVG image.
     * 
     * @return The Y of the bounding box of the original SVG image.
     */
    public static int getOrigY() {
        return 0;
    }

    /**
     * Returns the width of the bounding box of the original SVG image.
     * 
     * @return The width of the bounding box of the original SVG image.
     */
    public static int getOrigWidth() {
        return 1;
    }

    /**
     * Returns the height of the bounding box of the original SVG image.
     * 
     * @return The height of the bounding box of the original SVG image.
     */
    public static int getOrigHeight() {
        return 1;
    }
}

