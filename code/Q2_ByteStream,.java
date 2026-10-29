import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Q2_ByteStream {

    public static void main(String[] args) {

        String host = "api.ptit.store";
        int port = 2206;

        String studentCode = "B23DCAT022";
        String qCode = "wwF0yqt8";

        try (Socket socket = new Socket(host, port)) {

            socket.setSoTimeout(5000);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // a. Gửi request
            String request = studentCode + ";" + qCode;

            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();

            // b. Nhận chuỗi
            byte[] buffer = new byte[4096];

            int len = in.read(buffer);

            String s = new String(
                    buffer, 0, len, StandardCharsets.UTF_8
            ).trim();

            System.out.println("Nhan: " + s);

            // c. Tìm longest substring không lặp
            String longest = findLongest(s);

            String result =
                    longest + ";" + longest.length();

            System.out.println("Gui: " + result);

            out.write(result.getBytes(StandardCharsets.UTF_8));
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String findLongest(String s) {

        Map<Character, Integer> lastPosition = new HashMap<>();

        int left = 0;

        int maxLength = 0;
        int start = 0;

        for (int right = 0; right < s.length(); right++) {

            char c = s.charAt(right);

            // Nếu ký tự đã xuất hiện trong window hiện tại
            if (lastPosition.containsKey(c)
                    && lastPosition.get(c) >= left) {

                left = lastPosition.get(c) + 1;
            }

            lastPosition.put(c, right);

            int currentLength = right - left + 1;

            if (currentLength > maxLength) {
                maxLength = currentLength;
                start = left;
            }
        }

        return s.substring(start, start + maxLength);
    }
}