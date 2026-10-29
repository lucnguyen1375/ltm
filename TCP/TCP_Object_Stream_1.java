package TCP;

import java.io.*;
import java.net.Socket;

// KHÔNG public
class Laptop implements Serializable {

    private static final long serialVersionUID = 20150711L;

    private int id;
    private String code;
    private String name;
    private int quantity;

    public Laptop(int id, String code, String name, int quantity) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

public class TCP_Object_Stream_1 {

    public static void main(String[] args) {

        String host = "36.50.135.242";
        int port = 2209;

        String studentCode = "B23DCCN516";
        String qCode = "ll5MQaHY";

        try (Socket socket = new Socket(host, port)) {

            socket.setSoTimeout(5000);

            ObjectOutputStream out =
                    new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            ObjectInputStream in =
                    new ObjectInputStream(socket.getInputStream());

            // Gửi request
            String request = studentCode + ";" + qCode;

            out.writeObject(request);
            out.flush();

            // Nhận Laptop
            Laptop laptop = (Laptop) in.readObject();

            // Sửa tên
            String[] words = laptop.getName().trim().split("\\s+");

            if (words.length >= 2) {
                String temp = words[0];
                words[0] = words[words.length - 1];
                words[words.length - 1] = temp;
            }

            laptop.setName(String.join(" ", words));

            // Đảo quantity
            String reversed = new StringBuilder(
                    String.valueOf(laptop.getQuantity())
            ).reverse().toString();

            laptop.setQuantity(Integer.parseInt(reversed));

            // Gửi lại object
            out.writeObject(laptop);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}