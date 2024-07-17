package top.badguy.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class NetworkStatsDAO {
    public String getNetworkStats() {
        try (Statement statement = MysqlConfig.getStatement()){
            ResultSet resultSet = statement.executeQuery("SELECT * FROM network_stats");
            while (resultSet.next()) {
                System.out.println(resultSet.getString("ip_address"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return "NetworkStats";
    }
}
