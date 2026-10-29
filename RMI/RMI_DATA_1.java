
package RMI;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

interface DataService extends Remote {

    Object requestData(String studentCode, String qCode)
            throws RemoteException;

    void submitData(String studentCode, String qCode, Object data)
            throws RemoteException;
}

public class RMI_DATA_1 {

    static final String HOST = "36.50.135.242";
    static final int PORT = 1099;

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "K8Dom3Dq";

    // ========== 1. KET NOI RMI ==========

    static DataService connect() throws Exception {
        Registry registry =
                LocateRegistry.getRegistry(HOST, PORT);

        return (DataService) registry.lookup("RMIDataService");
    }

    // ========== 2. XU LY LOGIC ==========

    // Chuyen chuoi CSV thanh mang double
    static double[] parseData(Object response) {
        String csv = (String) response;

        String[] parts = csv.split(",");
        double[] numbers = new double[parts.length];

        for (int i = 0; i < parts.length; i++) {
            numbers[i] = Double.parseDouble(parts[i].trim());
        }

        return numbers;
    }

    // Tinh average, stddev va p95
    static String processData(double[] numbers) {

        int n = numbers.length;

        if (n == 0) {
            throw new IllegalArgumentException("Mang rong");
        }

        // 1. Tinh trung binh
        double sum = 0;

        for (double x : numbers) {
            sum += x;
        }

        double average = sum / n;

        // 2. Tinh do lech chuan tong the
        double variance = 0;

        for (double x : numbers) {
            variance += Math.pow(x - average, 2);
        }

        variance /= n;

        double stddev = Math.sqrt(variance);

        // 3. Tinh phan vi 95
        double[] sorted = numbers.clone();
        Arrays.sort(sorted);

        int index = (int) Math.ceil(n * 0.95) - 1;
        double p95 = sorted[index];

        // 4. Dinh dang ket qua
        return String.format(
            Locale.US,
            "average=%.2f;stddev=%.2f;p95=%.2f",
            average, stddev, p95
        );
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            // Ket noi RMI Server
            DataService service = connect();

            // a. Nhan du lieu tu server
            Object response = service.requestData(
                    STUDENT_CODE, Q_CODE
            );

            System.out.println("Data: " + response);

            // b. Chuyen CSV thanh mang double
            double[] numbers = parseData(response);

            // c. Tinh toan
            String result = processData(numbers);

            System.out.println("Result: " + result);

            // d. Gui ket qua ve server
            service.submitData(
                    STUDENT_CODE, Q_CODE, result
            );

            System.out.println("Da gui ket qua len server!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
