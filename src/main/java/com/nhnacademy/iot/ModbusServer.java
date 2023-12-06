package com.nhnacademy.iot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ModbusServer {

    public static void main(String[] args) {
        byte unitId = 1;
        int[] holdingRegisters = new int[100];

        for (int i = 0; i < holdingRegisters.length; i++) {
            holdingRegisters[i] = i;
        }

        try (ServerSocket serverSocket = new ServerSocket(11502)) {
            try (Socket socket = serverSocket.accept()) {
                BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
                BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());

                while (socket.isConnected()) {
                    byte[] inputBuffer = new byte[1024];

                    int receivedLength = inputStream.read(inputBuffer, 0, inputBuffer.length);

                    if (receivedLength > 0) {
                        System.out.println(Arrays.toString(Arrays.copyOfRange(inputBuffer, 0, receivedLength)));

                        if ((receivedLength > 7) && (6 + inputBuffer[5]) == receivedLength) {
                            if (unitId == inputBuffer[6]) {
                                int transactionId = (inputBuffer[0] << 8) | inputBuffer[1];
                                int functionCode = inputBuffer[7];

                                switch (functionCode) {
                                    case 3:
                                        int address = (inputBuffer[8] << 8) | inputBuffer[9];
                                        int quantity = (inputBuffer[10] << 8) | inputBuffer[11];

                                        if (address + quantity < holdingRegisters.length) {
                                            System.out.println("Address : " + address + ", Quantity: " + quantity);

                                            outputStream.write(SimpleMB.addMBAP(transactionId, unitId,
                                                    SimpleMB.makeReadHoldingRegistersResponse(address,
                                                            Arrays.copyOfRange(holdingRegisters, address, quantity))));
                                            outputStream.flush();
                                        }

                                        break;

                                    default:
                                }
                            }
                        } else {
                            System.err.println("수신 패킷 길이가 잘못되었습니다.");
                        }

                    } else if (receivedLength < 0) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
