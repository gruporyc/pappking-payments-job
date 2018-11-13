package co.ppk.job.services.impl;

import co.ppk.job.data.ClientsRepository;
import co.ppk.job.data.PaymentsRepository;
import co.ppk.job.domain.Client;
import co.ppk.job.domain.Load;
import co.ppk.job.enums.Status;
import com.payu.sdk.PayU;
import com.payu.sdk.PayUReports;
import com.payu.sdk.model.TransactionResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static co.ppk.job.enums.Status.DISMISSED;
import static co.ppk.job.utilities.Constants.DATABASE_DATETIME_FORMAT;
import co.ppk.job.services.JobManager;

@Component
public class JobManagerImpl implements JobManager{

    private static PaymentsRepository paymentsRepository;
    private static ClientsRepository clientsRepository;
    private Properties pm;

    public JobManagerImpl(Properties pm) {
        paymentsRepository = new PaymentsRepository();
        clientsRepository = new ClientsRepository();
        this.pm = pm;
    }


    @Override
    public void checkPendingPayments() {
        List<Client> clients = clientsRepository.getClientsByStatus(Status.ACTIVE);
        if(clients.isEmpty()) {
            System.out.println("No loads pending for conciliate.");
        }
        if(clients.isEmpty()) { return; }

        for (Client client : clients) {
            List<Load> pendingLoads = paymentsRepository.getLoadsByStatus(Status.PENDING.name(), client.getId());
            System.out.println("Loads pending to conciliate for client " + client.getId() + ": " + pendingLoads.size());
            for (Load load: pendingLoads) {
                try {
                    DateFormat dateFormat = new SimpleDateFormat(DATABASE_DATETIME_FORMAT);
                    Calendar loadDate = Calendar.getInstance();
                    loadDate.setTime(dateFormat.parse(load.getCreatedAt()));
                    loadDate.add(Calendar.HOUR, Integer.valueOf(pm.getProperty("PAYMENTS.MAX.PENDING.HOURS")));
                    if(loadDate.getTime().before(new Date())) {
                        paymentsRepository.updateLoadStatus(load.getId(), DISMISSED);
                        continue;
                    }

// If status is APPROVED then update balance with new amount
                    Status status = checkOrder(load.getTransactionId(), client);
                    if (Objects.isNull(status)) {
                        continue;
                    }
                    if(status.name().equals(Status.APPROVED.name())) {
                        paymentsRepository.updateBalance(load.getCustomerId(), round(load.getAmount(), 2));
                        paymentsRepository.updateLoadStatus(load.getId(), Status.APPROVED);
                    } else if (!status.equals(Status.PENDING)){
                        paymentsRepository.updateLoadStatus(load.getId(), status);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Status checkOrder(String transactionId, Client client) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PayU.PARAMETERS.TRANSACTION_ID, transactionId);
        Status statusResponse = null;

        try {
            PayU.paymentsUrl = pm.getProperty("PAYMENTS.API.URL");
            PayU.reportsUrl = pm.getProperty("PAYMENTS.REPORTS.RESPONSE.URL");
            PayU.apiKey = client.getGatewayApiKey();
            PayU.apiLogin = client.getGatewayApiLogin();
            PayU.merchantId = client.getGatewayMerchantId();
            PayU.isTest = Boolean.valueOf(pm.getProperty("PAYMENTS.TEST.PAYMENT"));
            TransactionResponse response = PayUReports.getTransactionResponse(parameters);
            statusResponse = Status.valueOf(response.getState().name());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusResponse;
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
