package group_42;

import java.awt.Font;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * File_Client
 */
public class File_Client {

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
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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

        JButton btnSendTCP = new JButton("Send File through TCP");
        btnSendTCP.setFont(new Font("Serif", Font.BOLD, 12));
        btnSendTCP.setPreferredSize(new Dimension(155, 75));

        JButton btnSendUDP = new JButton("Send File through UDP");
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
        String ip = JOptionPane.showInputDialog("Enter the IP address: ", "localhost");
        if (ip == null) {
            System.exit(0);
        }
        String port = JOptionPane.showInputDialog("Enter the port number: ", "1234");
        if (port == null) {
            System.exit(0);
        }

        btnChooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("Select file and protocol");

                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    fileToSend[0] = jFileChooser.getSelectedFile();
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
                        Socket socket = new Socket(ip, Integer.parseInt(port));
                        // t.interrupt();
                        FileInputStream fileIn = new FileInputStream(fileToSend[0].getAbsolutePath());
                        if (socket.isConnected()) {
                            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

                            String fileName = fileToSend[0].getName();
                            byte[] fileNameBytes = fileName.getBytes();

                            byte[] fileContentBytes = new byte[(int) fileToSend[0].length()];
                            fileIn.read(fileNameBytes);

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

                }
            }

        });

        btnSendUDP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // btnSendUDP.setText("Send File through UDP");
                // btnSendUDP.setEnabled(true);
                if (fileToSend[0] == null) {
                    JFileName.setText("Please choose a file first.");
                } else {
                    try {
                        // udp tings
                        btnSendUDP.setText("Sending...");
                        btnSendUDP.setEnabled(false);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }
        });

        jframe.add(jTitle);
        jframe.add(JFileName);
        jframe.add(jpButton);
        jframe.setVisible(true);

    }

}