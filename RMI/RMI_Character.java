package RMI;

import RMI.CharacterService;
import java.rmi.*;
import java.rmi.registry.*;
import java.net.URI;

public class RMI_Character {

    static final String HOST = "36.50.135.242";
    static final int PORT = 1099;

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "y0fNZzUW";

    // ========== 1. KET NOI RMI ==========

    static CharacterService connect() throws Exception {
        Registry registry = LocateRegistry.getRegistry(HOST, PORT);

        return (CharacterService)
                registry.lookup("RMICharacterService");
    }

    // ========== 2. XU LY LOGIC ==========

    static String processData(String data) throws Exception {
        URI uri = new URI(null, null, data, null);
        return uri.toASCIIString();
    }

    // ========== 3. HAM MAIN ==========

    public static void main(String[] args) {
        try {
            // Ket noi RMI Server
            CharacterService service = connect();

            // a. Nhan chuoi
            String data = service.requestCharacter(
                    STUDENT_CODE, Q_CODE
            );

            System.out.println("Data: " + data);

            // b. Ma hoa URL
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