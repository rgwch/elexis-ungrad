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

import static java.awt.Color.BLACK;

/**
 * Source: https://commons.wikimedia.org/wiki/File:Scissors_icon_black.svg
 * 
 * This class has been automatically generated using
 * <a href="https://flamingo.dev.java.net">Flamingo SVG transcoder</a>.
 */
public class Scissor {

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

        // _0
        transformations.offer(g.getTransform());
        g.transform(new AffineTransform(1, 0, 0, 1, 0, -992.3622f));

        // _0_0
        transformations.offer(g.getTransform());
        g.transform(new AffineTransform(4.2610846f, -1.2351263f, 1.2351263f, 4.2610846f, -1337.7659f, -2994.9736f));

        // _0_0_0

        // _0_0_0_0
        shape = new GeneralPath();
        ((GeneralPath) shape).moveTo(59.731667, 956.4006);
        ((GeneralPath) shape).curveTo(59.121883, 955.0077, 57.428177, 954.565, 56.032764, 954.6883);
        ((GeneralPath) shape).lineTo(47.29016, 955.384);
        ((GeneralPath) shape).curveTo(45.354424, 953.4697, 42.982697, 953.1791, 43.158024, 952.56744);
        ((GeneralPath) shape).curveTo(43.295067, 952.08936, 43.704018, 952.32, 43.99513, 951.0564);
        ((GeneralPath) shape).curveTo(44.27464, 949.84314, 43.28042, 948.6013, 42.07866, 948.3415);
        ((GeneralPath) shape).curveTo(40.900932, 947.9962, 39.418846, 948.54974, 39.050983, 949.79346);
        ((GeneralPath) shape).curveTo(38.6031, 951.02405, 39.324852, 952.49316, 40.550518, 952.9335);
        ((GeneralPath) shape).curveTo(41.951454, 953.57086, 44.797127, 953.42725, 45.388298, 955.9544);
        ((GeneralPath) shape).curveTo(44.00349, 957.3657, 42.39515, 956.90967, 40.96616, 956.4095);
        ((GeneralPath) shape).curveTo(39.779778, 955.99426, 38.23181, 955.9246, 37.316532, 956.9454);
        ((GeneralPath) shape).curveTo(36.434814, 957.9223, 36.516373, 959.696, 37.693207, 960.4097);
        ((GeneralPath) shape).curveTo(38.845978, 961.25183, 40.78647, 961.1615, 41.517487, 959.768);
        ((GeneralPath) shape).curveTo(42.08096, 958.69385, 41.426296, 957.9256, 41.85548, 957.60004);
        ((GeneralPath) shape).curveTo(42.17932, 957.35443, 43.614635, 957.962, 46.605385, 957.53143);
        ((GeneralPath) shape).lineTo(54.290077, 962.9954);
        ((GeneralPath) shape).curveTo(55.283794, 963.6066, 56.60658, 963.9749, 57.86789, 963.1871);
        ((GeneralPath) shape).lineTo(49.965164, 957.29614);
        ((GeneralPath) shape).lineTo(59.731667, 956.4004);
        ((GeneralPath) shape).closePath();
        ((GeneralPath) shape).moveTo(42.656425, 949.5286);
        ((GeneralPath) shape).curveTo(43.701683, 950.5802, 43.05973, 952.4359, 41.661915, 952.4302);
        ((GeneralPath) shape).curveTo(40.37575, 952.5022, 39.207325, 950.937, 39.94349, 949.7764);
        ((GeneralPath) shape).curveTo(40.459965, 948.7991, 41.940872, 948.8087, 42.656425, 949.5286);
        ((GeneralPath) shape).closePath();
        ((GeneralPath) shape).moveTo(40.50119, 957.2511);
        ((GeneralPath) shape).curveTo(41.662247, 958.1029, 40.811913, 960.23676, 39.40679, 960.1535);
        ((GeneralPath) shape).curveTo(38.443832, 960.2065, 37.317192, 959.4101, 37.52439, 958.3529);
        ((GeneralPath) shape).curveTo(37.713287, 957.04944, 39.51252, 956.3386, 40.50119, 957.2511);
        ((GeneralPath) shape).closePath();

        g.setPaint(BLACK);
        g.fill(shape);

        g.setTransform(transformations.poll()); // _0_0_0

        g.setTransform(transformations.poll()); // _0_0

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
    public static float getOrigWidth() {
        return 100f;
    }

    /**
     * Returns the height of the bounding box of the original SVG image.
     * 
     * @return The height of the bounding box of the original SVG image.
     */
    public static float getOrigHeight() {
        return 60f;
    }
}
