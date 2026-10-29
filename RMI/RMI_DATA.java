
package RMI;

import RMI.DataService;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

// interface DataService extends Remote {

//     Object requestData(String studentCode, String qCode)
//             throws RemoteException;

//     void submitData(String studentCode, String qCode, Object data)
//             throws RemoteException;
// }

public class RMI_DATA {

    static final String HOST = "36.50.135.242";
    static final int PORT = 1099;

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "uXQowvBC";

    // ========== 1. KET NOI RMI ==========

    static DataService connect() throws Exception {
        Registry registry =
                LocateRegistry.getRegistry(HOST, PORT);

        return (DataService) registry.lookup("RMIDataService");
    }

    // ========== 2. XU LY LOGIC ==========

    // Chuyen Object thanh List<Integer>
    static List<Integer> parseData(Object data) {
        List<Integer> numbers = new ArrayList<>();

        if (data instanceof int[]) {
            for (int x : (int[]) data) {
                numbers.add(x);
            }
        } else if (data instanceof List<?>) {
            for (Object x : (List<?>) data) {
                numbers.add(((Number) x).intValue());
            }
        } else if (data instanceof Integer[]) {
            numbers.addAll(Arrays.asList((Integer[]) data));
        } else {
            throw new IllegalArgumentException(
                    "Kieu du lieu khong ho tro: " +
                    (data == null ? "null" : data.getClass())
            );
        }

        return numbers;
    }

    // Tim vi tri cac dinh cuc bo
    static List<Integer> processData(List<Integer> data) {

        List<Integer> result = new ArrayList<>();
        int n = data.size();

        for (int i = 0; i < n; i++) {

            boolean greaterLeft =
                    (i == 0 || data.get(i) > data.get(i - 1));

            boolean greaterRight =
                    (i == n - 1 || data.get(i) > data.get(i + 1));

            if (greaterLeft && greaterRight) {
                result.add(i+1);
            }
        }

        return result;
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            // Ket noi server
            DataService service = connect();

            // a. Nhan du lieu
            Object response = service.requestData(
                    STUDENT_CODE, Q_CODE
            );

            List<Integer> data = parseData(response);

            System.out.println("Data: " + data);

            // b. Tim dinh cuc bo
            List<Integer> result = processData(data);

            System.out.println("Result: " + result);

            // c. Gui ket qua
            service.submitData(
                    STUDENT_CODE, Q_CODE, result
            );

            System.out.println("Da gui ket qua len server!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
