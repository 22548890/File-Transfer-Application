import java.awt.Font;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * File_Client
 */
public class File_Client {
    DatagramSocket dsocket;
    InetAddress address;
    File file;
    String host;
    static String port;
    static Socket socket;
    static String ip;

    public static void main(String[] args) {
        final File[] fileToSend = new File[1];
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame jframe = new JFrame("File Transfer File_Client");

        jframe.setSize(500, 350);
        jframe.setLayout(new BoxLayout(jframe.getContentPane(), BoxLayout.Y_AXIS));
        jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JLabel jTitle = new JLabel("File Transfer Sender");
        jTitle.setFont(new Font("Serif", Font.BOLD, 25));
        jTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        jTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel JFileName = new JLabel("Select file and protocol");
        JFileName.setFont(new Font("Serif", Font.PLAIN, 18));
        JFileName.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 0));
        JFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpButton = new JPanel();
        jpButton.setBorder(BorderFactory.createEmptyBorder(70, 0, 10, 0));

        JButton btnSendTCP = new JButton("Send TCP");
        btnSendTCP.setFont(new Font("Serif", Font.BOLD, 12));
        btnSendTCP.setPreferredSize(new Dimension(155, 75));

        JButton btnSendUDP = new JButton("Send UDP");
        btnSendUDP.setFont(new Font("Serif", Font.BOLD, 12));
        btnSendUDP.setPreferredSize(new Dimension(155, 75));

        JButton btnChooseFile = new JButton("Choose File");
        btnChooseFile.setFont(new Font("Serif", Font.BOLD, 18));
        btnChooseFile.setPreferredSize(new Dimension(140, 75));
        btnChooseFile.setBackground(Color.GREEN);
        btnChooseFile.setForeground(Color.GREEN);

        jpButton.add(btnChooseFile);
        jpButton.add(btnSendTCP);
        jpButton.add(btnSendUDP);

        // start GUI
        ip = JOptionPane.showInputDialog("Enter the IP address: ", "localhost");
        if (ip == null) {
            System.exit(0);
        }
        port = JOptionPane.showInputDialog("Enter the port number: ", "1234");
        if (port == null) {
            System.exit(0);
        }
        File_Client sender = new File_Client();
        btnChooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("Select file and protocol");

                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    fileToSend[0] = jFileChooser.getSelectedFile();
                    sender.file = jFileChooser.getSelectedFile();
                    JFileName.setText("The file you want to send it: " + fileToSend[0].getName());
                }
            }

        });

        btnSendTCP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileToSend[0] == null) {
                    JFileName.setText("Please choose a file first.");
                } else {
                    try {

                        // Timeout timeout = new Timeout();
                        // Thread t = new Thread(timeout);
                        // t.start();
                        socket = new Socket(ip, Integer.parseInt(port));
                        // t.interrupt();
                        FileInputStream fileIn = new FileInputStream(fileToSend[0].getAbsolutePath());
                        if (socket.isConnected()) {
                            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

                            String fileName = fileToSend[0].getName();
                            byte[] fileNameBytes = fileName.getBytes();

                            byte[] fileContentBytes = new byte[(int) fileToSend[0].length()];
                            fileIn.read(fileContentBytes);

                            dOut.writeInt(fileNameBytes.length);
                            dOut.write(fileNameBytes);

                            dOut.writeInt(fileContentBytes.length);
                            dOut.write(fileContentBytes);
                        } else {
                            JFileName.setText("Please connect to the server first.");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Invalid IP address or invalid port number",
                                "SERVER NOT FOUND", JOptionPane.ERROR_MESSAGE);
                        JFileName.setText("Please connect to the server first.");
                    }
                    JFileName.setText("File: " + fileToSend[0].getName() + " was sent through TCP");

                }

            }

        });

        btnSendUDP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // btnSendUDP.setText("Send File through UDP");
                // btnSendUDP.setEnabled(true);
                if (sender.file == null) {
                    JFileName.setText("Please choose a file first.");
                } else {
                    try {
                        // udp tings
                        sender.send();
                        btnSendUDP.setText("Sent");
                        btnSendUDP.setEnabled(false);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }
        });

        jframe.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    sender.dsocket.close();
                    socket.close();
                } catch (Exception er) {
                    // TODO: handle exception
                }

                System.exit(0);
            }
        });

        jframe.add(jTitle);
        jframe.add(JFileName);
        jframe.add(jpButton);
        jframe.setVisible(true);

    }

    public void send() throws UnknownHostException, IOException {
        String tcphost = ip;
        int tcpport = 1234;

        String udphost = JOptionPane.showInputDialog("UDP: Enter the IP address: ", "localhost");

        int udpport = 1345;

        // String udphost = "localhost";
        // int udpport = 1345;

        // setting up socket for tcp communications
        Socket tcpsocket = new Socket(tcphost, tcpport);
        ObjectOutputStream oos = new ObjectOutputStream(tcpsocket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(tcpsocket.getInputStream());

        // sending filename and size over tcp
        String filename = file.getName();
        oos.writeObject(filename);
        oos.flush();
        int filesize = (int) file.length();
        oos.writeInt(filesize);
        oos.flush();

        // setting up socket for udp communications
        dsocket = new DatagramSocket();
        address = InetAddress.getByName(udphost);

        // convert file into byte array
        FileInputStream fis = null;
        byte[] fileBytes = new byte[filesize];
        try {
            fis = new FileInputStream(file);
            fis.read(fileBytes);
            fis.close();
        } catch (IOException e) {
            System.out.println("Error when converting file to byte array");
            System.exit(0);
        }

        ArrayList<DatagramPacket> packets = new ArrayList<DatagramPacket>();
        int[] seqNums = new int[filesize / 1021 + 1];
        int seqNum = 0;
        // read file into datagram packets
        for (int i = 0; i < filesize; i += 1021) {
            seqNum++;
            seqNums[seqNum - 1] = seqNum;
            byte[] packetBytes = new byte[1024];

            // add unqiue sequence number (starting at 1)
            packetBytes[0] = (byte) (seqNum >> 8);
            packetBytes[1] = (byte) (seqNum);

            if (i + 1021 >= filesize) { // if end of file is reached
                packetBytes[2] = (byte) (1);
                System.arraycopy(fileBytes, i, packetBytes, 3, filesize - i);
            } else {
                packetBytes[2] = (byte) (0);
                System.arraycopy(fileBytes, i, packetBytes, 3, 1021);
            }

            DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length, address, udpport);
            packets.add(packet);
        }

        // for (int i = 1; i < packets.size(); i++) {
        // System.out.println(i);
        // dsocket.send(packets.get(i - 1));
        // if (i % 42 == 0) { // every multiple of 42
        // int[] toSend = new int[42];
        // System.out.println(i - 42);
        // System.arraycopy(seqNums, i - 42, toSend, 0, 42);
        // oos.writeObject(toSend);
        // oos.flush();
        // } else if (i == packets.size() - 1) { // final bunch of packets
        // int[] toSend = new int[i % 42];
        // System.arraycopy(seqNums, i - (i % 42), toSend, 0, i % 42);
        // oos.writeObject(toSend);
        // oos.flush();
        // }
        // }
        // dsocket.send(packets.get(packets.size() - 1)); // final packet

        int[] toSend = new int[packets.size()];
        int i = 0;
        for (DatagramPacket packet : packets) {
            dsocket.send(packet);
            toSend[i] = i + 1;
            i++;
        }
        oos.writeObject(toSend);
        oos.flush();
    }

}