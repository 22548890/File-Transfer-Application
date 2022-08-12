package group_42;

import java.awt.Font;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * File_Client
 */
public class File_Client {

    public static void main(String[] args) {
        final File[] fileToSend = new File[1];

        JFrame jframe = new JFrame("File Transfer File_Client");

        jframe.setSize(450, 450);
        jframe.setLayout(new BoxLayout(jframe.getContentPane(), BoxLayout.Y_AXIS));
        jframe.setDefaultCloseOperation(jframe.EXIT_ON_CLOSE);

        JLabel jTitle = new JLabel("File Transfer Sender");
        jTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        jTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel JFileName = new JLabel("Choose a file to send");
        JFileName.setFont(new Font("Arial", Font.BOLD, 20));
        JFileName.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        JFileName.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel jpButton = new JPanel();
        jpButton.setBorder(BorderFactory.createEmptyBorder(75, 0, 10, 0));

        JButton btnSend = new JButton("Send File");
        btnSend.setPreferredSize(new Dimension(150, 75));

        JButton btnChooseFile = new JButton("Choose File");
        btnChooseFile.setPreferredSize(new Dimension(150, 75));

        jpButton.add(btnSend);
        jpButton.add(btnChooseFile);

        btnChooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("Choose file to send");

                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    fileToSend[0] = jFileChooser.getSelectedFile();
                    JFileName.setText("The file you want to send it: " + fileToSend[0].getName());
                }
            }

        });

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileToSend[0] == null) {
                    JFileName.setText("Please choose a file first.");
                } else {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(fileToSend[0].getAbsolutePath());
                        Socket socket = new Socket("localhost", 1234);

                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                        String fileName = fileToSend[0].getName();
                        byte[] fileNameBytes = fileName.getBytes();

                        byte[] fileContentBytes = new byte[(int) fileToSend[0].length()];
                        fileInputStream.read(fileNameBytes);

                        dataOutputStream.writeInt(fileNameBytes.length);
                        dataOutputStream.write(fileNameBytes);

                        dataOutputStream.writeInt(fileContentBytes.length);
                        dataOutputStream.write(fileContentBytes);
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