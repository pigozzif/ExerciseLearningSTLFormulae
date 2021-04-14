/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.gui;

import java.io.*;

import eggloop.flow.simhya.simhya.GlobalOptions;
import eggloop.flow.simhya.simhya.script.CommandManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.util.ArrayList;
import javax.swing.SwingWorker;


/**
 *
 * @author luca
 */
public class CommandPromptInteractor extends JFrame implements KeyListener, ActionListener, 
        Runnable  {
    CommandManager cmd;
    JTextArea out;
    JTextField in;
    PrintStream outStream;
    JPopupMenu popup;

    int width = 640;
    int height = 400;
    int bottom = 25;

    ArrayList<String> history;
    int historyPos;

    


    public CommandPromptInteractor() {
        initGui();
        cmd = new CommandManager();
        cmd.setEmbeddedInGuiStatus(false);
        outStream = new TextAreaStream(out);
        cmd.setOutputStream(outStream);
        cmd.setErrorStream(outStream);
        history = new ArrayList<String>();
        historyPos = -1;
        outStream.println("SimHyA simulator version " + GlobalOptions.version);


    }

    private void initGui() {
        this.setPreferredSize(new Dimension(width,height));

        this.setTitle("SimHyA console");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel main = new JPanel();
        main.setSize(new Dimension(width,height));
        main.setBackground(Color.black);
        //main.setLayout(new GridBagLayout());
        main.setLayout(new BorderLayout());
        main.setPreferredSize(new Dimension(width,height));
        this.getContentPane().add(main);

//        GridBagConstraints c;
        
       this.out = new JTextArea();
        out.setEditable(false);
        out.setLineWrap(true);
        out.setWrapStyleWord(true);
        this.out.setBackground(Color.black);
        this.out.setForeground(Color.lightGray);
        DefaultCaret caret = (DefaultCaret)out.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scroll = new JScrollPane(out);
        scroll.setPreferredSize(new Dimension(width,height-bottom));
        //scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


//
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridwidth = 2;
//        c.fill = c.BOTH;
//        c.anchor = c.PAGE_START;
//        main.add(scroll, c);
        main.add(scroll,BorderLayout.CENTER);


        JLabel label = new JLabel("command >");
        label.setBackground(Color.black);
        label.setForeground(Color.lightGray);


//        c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = 1;
//        c.fill = c.NONE;
//        c.anchor = c.LINE_END;
//        c.insets = new Insets(5,5,5,5);
//        main.add(label, c);

        
        
        this.in = new JTextField();
        in.setBackground(Color.black);
        in.setForeground(Color.lightGray);
        in.addKeyListener(this);
        in.addActionListener(this);
        
//
//        c = new GridBagConstraints();
//        c.gridx = 1;
//        c.gridy = 1;
//        c.fill = c.HORIZONTAL;
//        c.insets = new Insets(0,0,0,5);
//        main.add(in, c);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.black);

        panel.add(label,BorderLayout.WEST);
        panel.add(in,BorderLayout.CENTER);
        main.add(panel,BorderLayout.SOUTH);


        popup = new JPopupMenu();
        JMenuItem menuItem;
        menuItem = new JMenuItem("Save command history to file");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveCommandHistoryToFile();
            }
        });
        popup.add(menuItem);
        menuItem = new JMenuItem("Save log to file");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveLogToFile();
            }
        });
        popup.add(menuItem);
        out.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                 
            }

            public void mouseEntered(MouseEvent e) {
               
            }

            public void mouseExited(MouseEvent e) {
                
            }

            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        this.setLocation((screenWidth - width)/2, (screenHeight-height) / 2);
        //main.repaint();
        this.pack();
       
    }


    private void saveCommandHistoryToFile() {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                PrintStream  p = new PrintStream(file);
                p.print(this.getCommandHistory());
                p.close();
            } catch (IOException e) {
                outStream.println("###ERROR while saving command history to file " + e.getMessage());
            }
        }
    }

    private void saveLogToFile() {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                PrintStream  p = new PrintStream(file);
                p.print(this.out.getText());
                p.close();
            } catch (IOException e) {
                outStream.println("###ERROR while saving log to file " + e.getMessage());
            }
        }
    }

    private String getCommandHistory() {
        String s = "";
        for (String h : this.history)
            s += h + "\n";
        return s;
    }

    public void run() {
        this.pack();
        this.setVisible(true);
        in.requestFocus();
    }

    public void actionPerformed(ActionEvent e) {
        final String command = in.getText();

        SwingWorker worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                executeCommand(command);
                return null;
            }  
        };
        worker.execute();
    }

    private void executeCommand(String command) {
        if (command.equals("quit") || command.equals("exit")) {
            history.add(command);
            this.historyPos = this.history.size() - 1;
            in.setText("");
            int n = JOptionPane.showConfirmDialog(this, "Exit SimHyA. Are you sure?");
            if (n== JOptionPane.YES_OPTION)
                System.exit(0);
            else {
                outStream.println("\n > " + command);
                outStream.println("Quit from SymHyA canceled by user.");    
            }
        } else if (command.equals("clear")) {
            history.add(command);
            this.historyPos = this.history.size() - 1;
            in.setText("");
            out.setText("SimHyA simulator version " + GlobalOptions.version);
        } else if (command.equals("gwd")) {
            history.add(command);
            this.historyPos = this.history.size() - 1;
            in.setText("");
            outStream.println("\n > " + command);
            outStream.println("Current working directory is " + cmd.getCurrentDirectory());
        } else if (command.equals("cwd")) {
            history.add(command);
            this.historyPos = this.history.size() - 1;
            in.setText("");
            this.changeWorkingDirectory();
            outStream.println("\n > " + command);
            outStream.println("Current working directory is " + cmd.getCurrentDirectory());
        } else if (isEmptyLine(command) || isCommentLine(command)) {
           
        } else if(!command.isEmpty()) {
            history.add(command);
            this.historyPos = this.history.size() - 1;
            in.setText("");
            outStream.println("\n > " + command);
            cmd.executeCommand(command);   
        }
    }
    
    private void changeWorkingDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(cmd.getCurrentDirectory()));
        chooser.setDialogTitle("Choose new working directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
           String newDir; 
           if(chooser.getSelectedFile().isDirectory())
               newDir = chooser.getSelectedFile().getAbsolutePath();
           else 
               newDir = chooser.getSelectedFile().getParent();
           cmd.setCurrentDirectory(newDir);
        }
    }


    private boolean isCommentLine(String command) {
        int i = command.indexOf("//");
        if (i >= 0) {
            for (int j =0; j<i; j++)
                if ( command.charAt(j) != ' ' && command.charAt(j) != '\t' && command.charAt(j) != '\r' )
                    return false;
            return true;
        } else
            return false;
    }

    private boolean isEmptyLine(String command) {
        for (int j =0; j<command.length(); j++)
            if ( command.charAt(j) != ' ' && command.charAt(j) != '\t' && command.charAt(j) != '\r' )
                return false;
        return true;
    }

    public void keyPressed(KeyEvent e) {

    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (this.historyPos >= 0) {
                in.setText(this.history.get(this.historyPos--));
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (this.historyPos < this.history.size()-1) {
                in.setText(this.history.get(++this.historyPos));
            } else if (this.historyPos == this.history.size()-1) {
                in.setText("");
            }
        }
    }

    public void keyTyped(KeyEvent e) {

    }

    





    
}
