package RMI;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

interface ByteService extends Remote {
    byte[] requestData(String studentCode, String qCode)
            throws RemoteException;

    void submitData(String studentCode, String qCode, byte[] data)
            throws RemoteException;
}

public class RMI_Byte_1 {

    static final String HOST = "36.50.135.242";
    static final int PORT = 1099;

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "2zYbStgq";

    // ========== 1. KET NOI RMI ==========

    static ByteService connect() throws Exception {
        Registry registry = LocateRegistry.getRegistry(HOST, PORT);
        return (ByteService) registry.lookup("RMIByteService");
    }

    // ========== 2. XU LY LOGIC ==========

    static byte[] processData(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Mang rong");
        }

        int[] count = new int[256];

        // Dem so lan xuat hien
        for (byte b : data) {
            count[b & 0xFF]++;
        }

        int maxCount = 0;
        byte result = data[0];

        // Tim phan tu xuat hien nhieu nhat
        for (byte b : data) {
            int frequency = count[b & 0xFF];

            if (frequency > maxCount) {
                maxCount = frequency;
                result = b;
            }
        }

        return new byte[]{result, (byte) maxCount};
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            // Ket noi RMI Server
            ByteService service = connect();

            // a. Nhan du lieu
            byte[] data = service.requestData(
                    STUDENT_CODE, Q_CODE
            );

            System.out.println("Data: " + Arrays.toString(data));

            // b. Xu ly
            byte[] result = processData(data);

            System.out.println("Result: " + Arrays.toString(result));

            // c. Gui ket qua
            service.submitData(
                    STUDENT_CODE, Q_CODE, result
            );

            System.out.println("Da gui ket qua!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
