package RMI;

import java.rmi.*;
import java.rmi.registry.*;

interface CharacterService extends Remote {
    String requestCharacter(String studentCode, String qCode)
            throws RemoteException;

    void submitCharacter(String studentCode, String qCode, String data)
            throws RemoteException;
}

public class RMI_Character_1 {

    static final String HOST = "36.50.135.242";
    static final int PORT = 1099;

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "mQ29ZSMx";

    // ========== 1. KET NOI RMI ==========

    static CharacterService connect() throws Exception {
        Registry registry = LocateRegistry.getRegistry(HOST, PORT);

        return (CharacterService)
                registry.lookup("RMICharacterService");
    }

    // ========== 2. XU LY LOGIC ==========

    static String processData(String data) {
        int number = Integer.parseInt(data.trim());

        if (number <= 0 || number > 3999) {
            throw new IllegalArgumentException(
                    "So phai nam trong khoang 1-3999"
            );
        }

        int[] values = {
                1000, 900, 500, 400,
                100, 90, 50, 40,
                10, 9, 5, 4, 1
        };

        String[] roman = {
                "M", "CM", "D", "CD",
                "C", "XC", "L", "XL",
                "X", "IX", "V", "IV", "I"
        };

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                result.append(roman[i]);
                number -= values[i];
            }
        }

        return result.toString();
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            // Ket noi server
            CharacterService service = connect();

            // a. Nhan du lieu
            String data = service.requestCharacter(
                    STUDENT_CODE, Q_CODE
            );

            System.out.println("Data: " + data);

            // b. Chuyen sang so La Ma
            String result = processData(data);

            System.out.println("Result: " + result);

            // c. Gui ket qua
            service.submitCharacter(
                    STUDENT_CODE, Q_CODE, result
            );

            System.out.println("Da gui ket qua!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}