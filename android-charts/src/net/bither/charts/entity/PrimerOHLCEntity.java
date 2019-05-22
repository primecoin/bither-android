package net.bither.charts.entity;

public class PrimerOHLCEntity extends OHLCEntity {


    private static final long serialVersionUID = 1L;

    private double volume;

    public PrimerOHLCEntity(double open, double high, double low, double close,
                            double volume, String title, long date) {
        super(open, high, low, close, title, date);

        this.volume = volume;

    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

}
