package de.ugdata.mousesharett.model;

public class InputMessage {
    public enum Type { MOUSE_MOVE, MOUSE_CLICK, MOUSE_PRESS, MOUSE_RELEASE, KEY_PRESS, KEY_RELEASE }
    
    private Type type;
    private int x;
    private int y;
    private int keyCode;
    private int button;

    public InputMessage() {}

    public static InputMessage move(int x, int y) {
        InputMessage m = new InputMessage();
        m.type = Type.MOUSE_MOVE;
        m.x = x;
        m.y = y;
        return m;
    }

    // Getters and Setters
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getKeyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = keyCode; }
    public int getButton() { return button; }
    public void setButton(int button) { this.button = button; }
}
