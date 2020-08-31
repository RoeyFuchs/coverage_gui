public class Report {
    private Point location;
    private Boolean isIntersting;

    public Report(Point p, Boolean isIntersting){
        this.isIntersting = isIntersting;
        this.location = p;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public Boolean getIntersting() {
        return isIntersting;
    }

    public void setIntersting(Boolean intersting) {
        isIntersting = intersting;
    }




}
