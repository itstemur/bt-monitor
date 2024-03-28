package com.tz.btmonitor.model;

public class Channel {
    private int id;
    private int min;
    private int max;
    private int d;
    private int value;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Channel copy() {
        Channel copyChannel = new Channel();
        copyChannel.setId(this.id);
        copyChannel.setMin(this.min);
        copyChannel.setMax(this.max);
        copyChannel.setD(this.d);
        copyChannel.setValue(this.value);
        return copyChannel;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id=" + id +
                ", min=" + min +
                ", max=" + max +
                ", d=" + d +
                ", value=" + value +
                '}';
    }
}
