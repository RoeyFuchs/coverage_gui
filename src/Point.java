import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.shape.Rectangle;

public class Point {
    private int x;
    private int y;
    private IntegerProperty value = new SimpleIntegerProperty(this, "value", -1);

    private Rectangle rec;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getValue() {
        return value.get();
    }

    public IntegerProperty valueProperty() {
        return value;
    }

    public void setValue(int value) {
        this.value.set(value);
    }

    public Rectangle getRec() {
        return rec;
    }

    public void setRec(Rectangle rec) {
        this.rec = rec;
    }


}
