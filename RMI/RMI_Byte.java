package RMI;


import RMI.ByteService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class RMI_Byte {

    static final String HOST = "36.50.135.242";
    static final int PORT = 1099;

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "cx4kVRjK";

    // ========== 1. KET NOI RMI ==========

    static ByteService connect() throws Exception {
        Registry registry = LocateRegistry.getRegistry(HOST, PORT);

        return (ByteService) registry.lookup("RMIByteService");
    }

    // ========== 2. XU LY LOGIC ==========

    static byte[] processData(byte[] data) {
        int[] count = new int[256];

        // Dem so lan xuat hien
        for (byte b : data) {
            count[b & 0xFF]++;
        }

        int minCount = Integer.MAX_VALUE;
        byte result = 0;

        // Duyet theo thu tu ban dau
        for (byte b : data) {
            int frequency = count[b & 0xFF];

            if (frequency < minCount) {
                minCount = frequency;
                result = b;
            }
        }

        return new byte[]{result, (byte) minCount};
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            // Ket noi server
            ByteService service = connect();

            // a. Lay du lieu
            byte[] data = service.requestData(
                    STUDENT_CODE, Q_CODE
            );

            System.out.println("Du lieu: " + Arrays.toString(data));

            // b. Xu ly
            byte[] result = processData(data);

            System.out.println("Ket qua: " + Arrays.toString(result));

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
