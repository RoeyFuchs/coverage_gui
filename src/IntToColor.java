import javafx.scene.paint.Color;

//convert numbers to color. here you can change the rectangles colors
public class IntToColor {
    public static Color getColor(int v) {
        if (v == (Map.BORDER)) {
            return Color.BLACK;
        }

        if (v==(Map.AGENT)) {
            return Color.SILVER;
        }

        if (v==(Map.BEEN_HERE)) {
            return Color.GRAY;
        }

        if (v==(Map.REGULAR)) {
            return Color.WHITE;
        }

        if(v==(Map.INTERES)) {
            return Color.YELLOW;
        }
        return Color.GREEN;
    }
}
