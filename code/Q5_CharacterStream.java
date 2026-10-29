import java.io.*;
import java.net.*;
import java.util.*;

public class Q5_CharacterStream {

    public static void main(String[] args) {

        String host = "36.50.135.242";
        int port = 2208;

        String studentCode = "B23DCCN516";
        String qCode = "NWMoIO6T";

        try (Socket socket = new Socket(host, port)) {

            socket.setSoTimeout(5000);

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

            BufferedWriter out =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream()
                            )
                    );

            // a. Gửi request
            out.write(studentCode + ";" + qCode);
            out.newLine();
            out.flush();

            // b. Nhận danh sách domain
            String data = in.readLine();

            System.out.println("Nhan:");
            System.out.println(data);

            String[] domains = data.split(",");

            List<String> resultList =
                    new ArrayList<>();

            // c. Tìm .edu
            for (String domain : domains) {

                domain = domain.trim();

                if (domain.toLowerCase()
                        .endsWith(".edu")) {

                    resultList.add(domain);
                }
            }

            String result =
                    String.join(", ", resultList);

            System.out.println("Gui:");
            System.out.println(result);

            out.write(result);
            out.newLine();
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}