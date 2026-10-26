
public class Item {

    private int id;
    private String name;
    private String category;
    private String location;
    private String description;
    private String status;
    private String reportedOn;

    public Item(int id, String name, String category, String location,
                String description, String status, String reportedOn) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.location = location;
        this.description = description;
        this.status = status;
        this.reportedOn = reportedOn;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getReportedOn() { return reportedOn; }
}
