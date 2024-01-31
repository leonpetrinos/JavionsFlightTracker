package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a color gradient named Plasma
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class ColorRamp {
    private final Color[] colors;

    /**
     * Plasma sequence of colors
     */
    public static final ColorRamp PLASMA = new ColorRamp(
            Color.valueOf("0x0d0887ff"), Color.valueOf("0x220690ff"),
            Color.valueOf("0x320597ff"), Color.valueOf("0x40049dff"),
            Color.valueOf("0x4e02a2ff"), Color.valueOf("0x5b01a5ff"),
            Color.valueOf("0x6800a8ff"), Color.valueOf("0x7501a8ff"),
            Color.valueOf("0x8104a7ff"), Color.valueOf("0x8d0ba5ff"),
            Color.valueOf("0x9814a0ff"), Color.valueOf("0xa31d9aff"),
            Color.valueOf("0xad2693ff"), Color.valueOf("0xb6308bff"),
            Color.valueOf("0xbf3984ff"), Color.valueOf("0xc7427cff"),
            Color.valueOf("0xcf4c74ff"), Color.valueOf("0xd6556dff"),
            Color.valueOf("0xdd5e66ff"), Color.valueOf("0xe3685fff"),
            Color.valueOf("0xe97258ff"), Color.valueOf("0xee7c51ff"),
            Color.valueOf("0xf3874aff"), Color.valueOf("0xf79243ff"),
            Color.valueOf("0xfa9d3bff"), Color.valueOf("0xfca935ff"),
            Color.valueOf("0xfdb52eff"), Color.valueOf("0xfdc229ff"),
            Color.valueOf("0xfccf25ff"), Color.valueOf("0xf9dd24ff"),
            Color.valueOf("0xf5eb27ff"), Color.valueOf("0xf0f921ff"));

    /**
     * Initiates an array of colors
     *
     * @param colors (Color[]) : variable array of colors
     * @throws IllegalArgumentException if the number of colors is strictly less than 2
     */
    public ColorRamp(Color... colors) {
        Preconditions.checkArgument(colors.length >= 2);
        this.colors = colors.clone();
    }

    /**
     * Determines a color given the parameter d of the function
     *
     * @param d (double) : number calculated using the altitude of the aircraft
     * @return a color given the parameter d of the function
     */
    public Color at(double d) {
        if (d <= 0) {
            return colors[0];
        }

        double span = colors.length - 1d;
        if (d >= 1) {
            return colors[(int) span];
        }

        int idx = (int) (d * span);
        double separation = 1d / span;
        double lb = idx * separation;

        double proportionColor2 = (d - lb) / separation;
        Color c1 = colors[idx], c2 = colors[idx + 1];
        return c1.interpolate(c2, proportionColor2);
    }

}
