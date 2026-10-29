import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.math.*;
import java.util.*;

public class Q3_NIO {

    public static void main(String[] args) {

        String host = "36.50.135.242";
        int port = 2211;

        String studentCode = "B23DCCN516";
        String qCode = "z1SFrZnQ";

        try (SocketChannel channel = SocketChannel.open()) {

            channel.connect(
                    new InetSocketAddress(host, port)
            );

            // a. Gửi frame studentCode;qCode
            String request = studentCode + ";" + qCode;

            writeFrame(channel, request);

            // b. Nhận đúng 3 frame
            StringBuilder data = new StringBuilder();

            for (int i = 0; i < 3; i++) {
                String payload = readFrame(channel);

                System.out.println(
                        "Frame " + (i + 1) + ": " + payload
                );

                data.append(payload);
            }

            System.out.println("Data: " + data);

            // c. Xử lý invoice
            String[] invoices =
                    data.toString().split("\\|");

            BigDecimal total = BigDecimal.ZERO;

            int large = 0;

            for (String invoice : invoices) {

                if (invoice.isBlank()) {
                    continue;
                }

                String[] parts = invoice.split(":");

                // invoiceId
                String invoiceId = parts[0];

                int quantity =
                        Integer.parseInt(parts[1]);

                BigDecimal unitPrice =
                        new BigDecimal(parts[2]);

                BigDecimal amount =
                        unitPrice.multiply(
                                BigDecimal.valueOf(quantity)
                        );

                total = total.add(amount);

                if (amount.compareTo(
                        new BigDecimal("500")) >= 0) {

                    large++;
                }
            }

            total = total.setScale(
                    2,
                    RoundingMode.HALF_UP
            );

            // d. Gửi frame kết quả
            String result =
                    "total=" + total.toPlainString()
                    + ";large=" + large;

            System.out.println("Gui: " + result);

            writeFrame(channel, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================
    // GỬI 1 FRAME
    // =============================
    public static void writeFrame(
            SocketChannel channel,
            String payload
    ) throws IOException {

        byte[] data =
                payload.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer =
                ByteBuffer.allocate(4 + data.length);

        // 4 byte length, big-endian
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.putInt(data.length);
        buffer.put(data);

        buffer.flip();

        // write có thể không ghi hết trong 1 lần
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    // =============================
    // ĐỌC 1 FRAME
    // =============================
    public static String readFrame(
            SocketChannel channel
    ) throws IOException {

        // Đọc đúng 4 byte length
        ByteBuffer lengthBuffer =
                ByteBuffer.allocate(4);

        lengthBuffer.order(ByteOrder.BIG_ENDIAN);

        readFully(channel, lengthBuffer);

        lengthBuffer.flip();

        int length = lengthBuffer.getInt();

        if (length < 0 || length > 10_000_000) {
            throw new IOException(
                    "Frame length khong hop le: " + length
            );
        }

        // Đọc đúng số byte payload
        ByteBuffer payloadBuffer =
                ByteBuffer.allocate(length);

        readFully(channel, payloadBuffer);

        payloadBuffer.flip();

        return StandardCharsets.UTF_8
                .decode(payloadBuffer)
                .toString();
    }

    // Đảm bảo đọc đủ buffer
    public static void readFully(
            SocketChannel channel,
            ByteBuffer buffer
    ) throws IOException {

        while (buffer.hasRemaining()) {

            int n = channel.read(buffer);

            if (n == -1) {
                throw new EOFException(
                        "Server dong ket noi som"
                );
            }
        }
    }
}