package com.airwar.core.model;

import com.airwar.core.config.GameConstants;
import com.airwar.core.model.aircraft.AbstractAircraft;

public abstract class FlyingObject {

    private static final int UNSET_SIZE = -1;

    protected int locationX;
    protected int locationY;
    protected int speedX;
    protected int speedY;
    protected int width;
    protected int height;
    protected boolean valid = true;

    protected FlyingObject(int locationX, int locationY, int speedX, int speedY) {
        this(locationX, locationY, speedX, speedY, UNSET_SIZE, UNSET_SIZE);
    }

    protected FlyingObject(int locationX, int locationY, int speedX, int speedY, int width, int height) {
        this.locationX = locationX;
        this.locationY = locationY;
        this.speedX = speedX;
        this.speedY = speedY;
        this.width = width;
        this.height = height;
    }

    public void forward() {
        locationX += speedX;
        locationY += speedY;
        if (locationX <= 0 || locationX >= GameConstants.WINDOW_WIDTH) {
            speedX = -speedX;
        }
    }

    public boolean crash(FlyingObject flyingObject) {
        requireSizeInitialized();
        flyingObject.requireSizeInitialized();

        int factor = this instanceof AbstractAircraft ? 2 : 1;
        int otherFactor = flyingObject instanceof AbstractAircraft ? 2 : 1;

        int x = flyingObject.getLocationX();
        int y = flyingObject.getLocationY();
        int otherWidth = flyingObject.getWidth();
        int otherHeight = flyingObject.getHeight();

        return x + (otherWidth + this.getWidth()) / 2 > locationX
                && x - (otherWidth + this.getWidth()) / 2 < locationX
                && y + (otherHeight / otherFactor + this.getHeight() / factor) / 2 > locationY
                && y - (otherHeight / otherFactor + this.getHeight() / factor) / 2 < locationY;
    }

    public int getLocationX() {
        return locationX;
    }

    public int getLocationY() {
        return locationY;
    }

    public void setLocation(double locationX, double locationY) {
        this.locationX = (int) locationX;
        this.locationY = (int) locationY;
    }

    public int getSpeedY() {
        return speedY;
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

    public boolean notValid() {
        return !this.valid;
    }

    public void vanish() {
        this.valid = false;
    }

    private void requireSizeInitialized() {
        if (width <= 0 || height <= 0) {
            throw new IllegalStateException("width/height must be set before collision check");
        }
    }
}
