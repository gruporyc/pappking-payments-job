package co.ppk.job.data;

import co.ppk.job.domain.Client;
import co.ppk.job.enums.Status;
import org.apache.commons.dbutils.QueryRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Component
public class ClientsRepository {
    private final DataSource ds;

    public ClientsRepository() {
        this.ds = DataSourceSingleton.getInstance();
    }

    public List<Client> getClientsByStatus(Status status) {
        QueryRunner run = new QueryRunner(ds);
        try {
            String query = "SELECT id, name, status, gateway_account_id, gateway_merchant_id, gateway_api_key, " +
                    "gateway_api_login, create_date, update_date FROM ppk_payments.clients WHERE status = ?";

            List<Client> clients = run.query(query,
                    rs -> {
                        List<Client> newClientsList = new LinkedList<>();
                        while (rs.next()){
                            newClientsList.add(new Client.Builder()
                                    .setId(rs.getString(1))
                                    .setName(rs.getString(2))
                                    .setStatus(rs.getString(3))
                                    .setGatewayAccountId(rs.getString(4))
                                    .setGatewayMerchantId(rs.getString(5))
                                    .setGatewayApiKey(rs.getString(6))
                                    .setGatewayApiLogin(rs.getString(7))
                                    .setCreatedAt(rs.getString(8))
                                    .setUpdatedAt(rs.getString(9))
                                    .build()
                            );
                        }
                        return newClientsList;
                    }, status.name());
            return clients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}