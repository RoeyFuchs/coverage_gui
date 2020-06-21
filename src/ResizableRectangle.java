import javafx.scene.shape.Rectangle;

class ResizableRectangle extends Rectangle {
    ResizableRectangle(double w, double h) {
        super(w, h);
    }
    ResizableRectangle() {
        super(0,0);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double minWidth(double height) {
        return 0.0;
    }

    @Override
    public double minHeight(double height) {return 0.0;}
}