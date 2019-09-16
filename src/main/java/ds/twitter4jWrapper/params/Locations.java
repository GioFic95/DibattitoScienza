package ds.twitter4jWrapper.params;

/**
 * The array of locations to use in a filtered query.
 */
public class Locations
{
    private double[][] location;

    public Locations(double[][] location)
    {
        this.location = location;
    }

    public double[][] getLocations()
    {
        return location;
    }
}
