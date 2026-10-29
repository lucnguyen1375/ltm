import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class TCPClient {

    public static void main(String[] args) {
        String host = "36.50.135.242";
        int port = 2208;

        String studentCode = "B23DCCN516";
        String qCode = "EC4F899B";

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            socket.setSoTimeout(5000);

            try (
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
                );

                BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
                )
            ) {

            String request = studentCode + ";" + qCode;

            writer.write(request);
            writer.newLine();
            writer.flush();

            System.out.println("Đã gửi: " + request);

            String response = reader.readLine();

            System.out.println("Server trả về:");
            System.out.println(response);

            String result = Arrays.stream(response.split(","))
                    .map(String::trim)
                    .filter(domain -> domain.endsWith(".edu"))
                    .collect(Collectors.joining(", "));

            System.out.println("Các domain .edu:");
            System.out.println(result);

            writer.write(result);
            writer.newLine();
            writer.flush();

            System.out.println("Đã gửi kết quả: " + result);

            }
        } catch (SocketTimeoutException e) {
            System.out.println("Quá thời gian chờ server (> 5 giây).");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
