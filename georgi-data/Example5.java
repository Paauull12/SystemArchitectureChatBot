class Rectangle {
    protected int width;
    protected int height;

    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int area() {
        return width * height;
    }
}

class Square extends Rectangle {
    public Square(int side) {
        super(side, side);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        super.setHeight(width);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        super.setWidth(height);
    }
}

public class Example5 {
    public static void main(String[] args) {
        Rectangle rect = new Rectangle(5, 10);
        System.out.println("Rectangle area: " + rect.area());

        Square square = new Square(5);
        System.out.println("Square area: " + square.area());

        Rectangle anotherRect = new Square(4);
        anotherRect.setWidth(6);
        System.out.println("Unexpected area: " + anotherRect.area());
    }

    public static void processRectangle(Rectangle r) {
        r.setWidth(10);
        r.setHeight(5);
        System.out.println("Processed rectangle area: " + r.area());
    }
}