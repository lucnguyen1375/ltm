package TCP;

import java.io.*;
import java.net.Socket;

class Customer implements Serializable {

    private static final long serialVersionUID = 20170711L;

    private int id;
    private String code;
    private String name;
    private String dayOfBirth;
    private String userName;

    public Customer(int id, String code, String name,
                    String dayOfBirth, String userName) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.dayOfBirth = dayOfBirth;
        this.userName = userName;
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

    public String getDayOfBirth() {
        return dayOfBirth;
    }

    public String getUserName() {
        return userName;
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

    public void setDayOfBirth(String dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", dayOfBirth='" + dayOfBirth + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}

public class TCP_Object_Stream_2 {

    public static void main(String[] args) {

        String host = "36.50.135.242";
        int port = 2209;

        String studentCode = "B23DCCN516";
        String qCode = "Jjo2NLsC";

        try (Socket socket = new Socket(host, port)) {

            socket.setSoTimeout(5000);

            ObjectOutputStream out =
                    new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            ObjectInputStream in =
                    new ObjectInputStream(socket.getInputStream());

            // 1. Gửi studentCode;qCode
            String request = studentCode + ";" + qCode;

            out.writeObject(request);
            out.flush();

            // 2. Nhận Customer
            Customer customer = (Customer) in.readObject();

            System.out.println("Nhan:");
            System.out.println(customer);

            // Lưu lại tên ban đầu để xử lý
            String originalName = customer.getName().trim();

            // 3a. Chuẩn hóa tên
            String newName = normalizeName(originalName);
            customer.setName(newName);

            // 3b. Chuẩn hóa ngày sinh
            String newDOB = normalizeDate(customer.getDayOfBirth());
            customer.setDayOfBirth(newDOB);

            // 3c. Sinh username
            String userName = createUserName(originalName);
            customer.setUserName(userName);

            System.out.println("Sau khi sua:");
            System.out.println(customer);

            // Gửi lại Customer
            out.writeObject(customer);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // nguyen van hai duong
    // -> DUONG, Nguyen Van Hai
    public static String normalizeName(String name) {

        String[] words = name.trim().split("\\s+");

        String lastName = words[words.length - 1].toUpperCase();

        StringBuilder result = new StringBuilder();

        result.append(lastName).append(", ");

        for (int i = 0; i < words.length - 1; i++) {

            String word = words[i].toLowerCase();

            result.append(
                    Character.toUpperCase(word.charAt(0))
            );

            result.append(word.substring(1));

            if (i < words.length - 2) {
                result.append(" ");
            }
        }

        return result.toString();
    }

    // 10-11-2012 -> 11/10/2012
    public static String normalizeDate(String date) {

        String[] parts = date.split("-");

        String month = parts[0];
        String day = parts[1];
        String year = parts[2];

        return day + "/" + month + "/" + year;
    }

    // nguyen van hai duong
    // -> nvhduong
    public static String createUserName(String name) {

        String[] words = name.trim()
                .toLowerCase()
                .split("\\s+");

        StringBuilder username = new StringBuilder();

        // Lấy chữ cái đầu của các từ trừ từ cuối
        for (int i = 0; i < words.length - 1; i++) {
            username.append(words[i].charAt(0));
        }

        // Thêm toàn bộ từ cuối
        username.append(words[words.length - 1]);

        return username.toString();
    }
}