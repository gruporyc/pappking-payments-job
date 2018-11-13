package co.ppk.job.data;

import co.ppk.job.domain.Load;
import co.ppk.job.enums.Status;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Component
public class PaymentsRepository {

    private final DataSource ds;

    public PaymentsRepository() {

        this.ds = DataSourceSingleton.getInstance();
    }

    public void updateBalance(String customerId, double amount) {
        QueryRunner run = new QueryRunner(ds);
        Timestamp now = Timestamp.from(Instant.now());
        try {
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            try {
                String query = "SELECT balance FROM ppk_payments.balances WHERE customer_id = '" + customerId + "';";
                String balance = run.query(query,
                        rs -> {
                            if (!rs.next()) {
                                return "";
                            }
                            rs.last();
                            return rs.getString(1);
                        });

                if(!balance.isEmpty()) {
                    String updateBalance = "UPDATE ppk_payments.balances SET balance = " + (Float.valueOf(balance) + amount) +
                            ", update_date = '" + now + "' WHERE customer_id = '" + customerId + "';";
                    stmt.executeUpdate(updateBalance);
                } else {
                    String balanceId = UUID.randomUUID().toString();
                    run.insert(conn, "INSERT INTO ppk_payments.balances(id, " +
                            "customer_id, " +
                            "balance, " +
                            "status) " +
                            "VALUES('" + balanceId + "', '" + customerId + "', " + amount + ", '" +
                            Status.ACTIVE.name() + "');", new ScalarHandler<>());

                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                DbUtils.close(conn);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateLoadStatus(String loadId, Status status) {
        try {
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            try {
                String update = "UPDATE ppk_payments.loads " +
                        "SET status = '" + status.name() + "' " +
                        "WHERE " +
                        "id = '" + loadId + "';";
                stmt.executeUpdate(update);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                DbUtils.close(conn);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Load> getLoadsByStatus(String status, String clientId) {
        QueryRunner run = new QueryRunner(ds);
        try {
            String query = "SELECT id, customer_id, amount, currency, payer_name, payer_card_last_digits, method, " +
                    "order_id, transaction_id, status, network_code, network_message, trazability_code, response_code, " +
                    "country, create_date, update_date FROM ppk_payments.loads WHERE status = ? and client_id = ?;";

            List<Load> loads = run.query(query,
                    rs -> {
                        List<Load> newLoadsList = new LinkedList<>();
                        while (rs.next()){
                            newLoadsList.add(new Load.Builder()
                                    .setId(rs.getString(1))
                                    .setCustomerId(rs.getString(2))
                                    .setAmount(rs.getFloat(3))
                                    .setCurrency(rs.getString(4))
                                    .setPayerName(rs.getString(5))
                                    .setPayerCardLastDigits(rs.getString(6))
                                    .setMethod(rs.getString(7))
                                    .setOrderId(rs.getString(8))
                                    .setTransactionId(rs.getString(9))
                                    .setStatus(rs.getString(10))
                                    .setNetworkCode(rs.getString(11))
                                    .setNetworkMessage(rs.getString(12))
                                    .setTrazabilityCode(rs.getString(13))
                                    .setResponseCode(rs.getString(14))
                                    .setCountry(rs.getString(15))
                                    .setCreatedAt(rs.getString(16))
                                    .setUpdatedAt(rs.getString(17))
                                    .build()
                            );
                        }
                        return newLoadsList;
                    }, status, clientId);
            return loads;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
