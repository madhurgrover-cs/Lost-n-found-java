import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO = Data Access Object.
 * This class contains all the SQL. Each method performs one database operation (CRUD):
 *   Create -> addItem      Read -> getAllItems, searchItems
 *   Update -> updateItem, markReturned      Delete -> deleteItem
 *
 * Every method uses a PreparedStatement with ? placeholders, which prevents SQL injection,
 * and try-with-resources, which closes the Connection, PreparedStatement and ResultSet automatically.
 * Errors (SQLException) are passed back to the GUI, which shows them to the user.
 */
public class ItemDAO {

    // CREATE: insert a new item. Returns the number of rows inserted (1 on success).
    public int addItem(String name, String category, String location,
                       String description, String status) throws SQLException {
        String sql = "INSERT INTO items (item_name, category, location, description, status) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setString(3, location);
            ps.setString(4, description);
            ps.setString(5, status);
            return ps.executeUpdate();
        }
    }

    // READ: get every item, newest ID first.
    public List<Item> getAllItems() throws SQLException {
        String sql = "SELECT * FROM items ORDER BY item_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return readItems(rs);
        }
    }

    // READ: search by name (partial match, e.g. "wal" finds "Black Wallet") or by exact ID.
    public List<Item> searchItems(String keyword) throws SQLException {
        String sql = "SELECT * FROM items WHERE item_name LIKE ? OR item_id = ? ORDER BY item_id DESC";
        int id = -1; // -1 matches no row, used when the keyword is not a number
        try {
            id = Integer.parseInt(keyword);
        } catch (NumberFormatException e) {
            // not a number, so only the name search is used
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%"); // % means "any characters" in LIKE
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return readItems(rs);
            }
        }
    }

    // UPDATE: change all details of an existing item. Returns rows updated (0 if the ID does not exist).
    public int updateItem(int id, String name, String category, String location,
                          String description, String status) throws SQLException {
        String sql = "UPDATE items SET item_name = ?, category = ?, location = ?, "
                   + "description = ?, status = ? WHERE item_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setString(3, location);
            ps.setString(4, description);
            ps.setString(5, status);
            ps.setInt(6, id);
            return ps.executeUpdate();
        }
    }

    // UPDATE: only change the status to 'Returned'.
    public int markReturned(int id) throws SQLException {
        String sql = "UPDATE items SET status = 'Returned' WHERE item_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    // DELETE: remove an item. Returns rows deleted (0 if the ID does not exist).
    public int deleteItem(int id) throws SQLException {
        String sql = "DELETE FROM items WHERE item_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    // Helper: turn each row of a ResultSet into an Item object.
    private List<Item> readItems(ResultSet rs) throws SQLException {
        List<Item> items = new ArrayList<>();
        while (rs.next()) { // moves to the next row; false when there are no more rows
            items.add(new Item(
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    rs.getString("category"),
                    rs.getString("location"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("reported_on")));
        }
        return items;
    }
}
