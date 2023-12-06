package com.nhnacademy.iot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("ems.nhnacademy.com", 502);
                BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
                BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream())) {

            int unitId = 1;
            int transactionId = 0;

            for (int i = 0; i < 10; i++) {
                byte[] request = SimpleMB.addMBAP(++transactionId, unitId,
                        SimpleMB.makeReadHoldingRegistersRequest(0, 5));
                outputStream.write(request);
                outputStream.flush();

                byte[] response = new byte[512];
                int receivedLength = inputStream.read(response, 0, response.length);

                System.out.println(Arrays.toString(Arrays.copyOfRange(response, 0, receivedLength)));
            }

        } catch (UnknownHostException e) {
            System.err.println("Unknown host!!");
        }
    }
}