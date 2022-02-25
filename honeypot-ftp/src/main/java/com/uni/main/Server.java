package com.uni.main;

import com.uni.log.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
    // data port = 22
    private ServerSocket serverSocket;
    // == controlSocket
    private Socket clientSocket;
    // == dataConnection
    private Socket dataSocket;
    private PrintWriter clientOut;
    private PrintWriter dataOut;
    private BufferedReader clientIn;
    private final Logger logger = new Logger();
    private Path root = Path.of("./src/main/resources/user");
    private Path curDir = root;
    private boolean quit;
    private boolean loggedIn = false;

    public void log(String message, boolean server) {
        try {
            message = server ? "Server: " + message : "Client: " + message;
            message = new Timestamp(System.currentTimeMillis()) + " " + message;
            logger.append(message + "\n");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void openDataConnection(String ipAddress, int port) {
        try {
            dataSocket = new Socket(ipAddress, port);
            dataOut = new PrintWriter(dataSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeDataConnection() {
        try {
            dataOut.close();
            dataSocket.close();
//            if (dataSocket != null) {
//                dataSocket.close();
//            }
        } catch (IOException e) {
            System.out.println("Could not close data connection");
            e.printStackTrace();
        }
        dataOut = null;
        dataSocket = null;
        //dataSocket = null;
    }

    private void dataMessage(String message) throws IOException {
        if (dataSocket == null || dataSocket.isClosed()) {
            message(FTPCodes.CANT_OPEN_DATA_CONNECTION, "No data connection was established");
        } else {
            dataOut.println(message);
            log(message, true);
        }
    }

    private void message(FTPCodes code, String message) {
        String res = code.getCode() + ". " + message;
        clientOut.println(res);
        log(res, true);
    }

    private void handleCommand(String commandAndArgs) throws IOException {
        log(commandAndArgs, false);
        System.out.println(commandAndArgs);

        int index = commandAndArgs.indexOf(' ');
        String command = ((index == -1)
                ? commandAndArgs.toUpperCase()
                : (commandAndArgs.substring(0, index)).toUpperCase());
        String args = ((index == -1) ? null : commandAndArgs.substring(index + 1));

        if (command.equals("USER")) {
            user(args);
            return;
        } else if (command.equals("QUIT")) {
            stop();
            quit = true;
            return;
        }

        if (!loggedIn) {
            message(FTPCodes.NOT_LOGGED_IN, "Please login to continue. Type 'user'.");
        } else {
            switch (command) {
                case "EPRT" -> eprt(args);
                case "LIST" -> dir();
                case "XMKD", "MKD" -> mkdir(args);
                case "CWD" -> cd(args);
                case "STOR" -> put(args);
                case "RMD", "XRMD", "DELE", "RNFR" -> fail();
                case "PWD", "XPWD" -> pwd();
                case "RETR" -> get(args);
                default -> message(FTPCodes.WRONG_COMMAND, "Unknown Command");
            }
        }
    }

    private void pwd() {
        message(FTPCodes.OK, curDir + "\\");
    }

    private void get(String args) {
        Path tmp = Path.of(curDir + "/" + args);

        if (!Files.exists(tmp)) {
            message(FTPCodes.FILE_UNAVAILABLE, "File does not exists");
        } else {
            message(FTPCodes.OPEN_DATA_CONNECTION,
                    "Opening data connection for requested file " + args);

            BufferedOutputStream fout = null;
            BufferedInputStream fin = null;
            File file = new File(tmp.toString());

            try {
                fout = new BufferedOutputStream(dataSocket.getOutputStream());
                fin = new BufferedInputStream(new FileInputStream(file));
            } catch (Exception e) {
                System.out.println("Could not create file streams");
            }

            // write file with buffer
            byte[] buf = new byte[1024];
            int l;
            try {
                while ((l = fin.read(buf, 0, 1024)) != -1) {
                    fout.write(buf, 0, l);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fin.close();
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            message(FTPCodes.FINISH_TRANSFER, "File transfer successful. Closing data connection.");
        }
        closeDataConnection();
    }

    private void put(String args) {
        if (args == null) {
            message(FTPCodes.ARGS_SYNTAX_ERROR, "No filename given");
        } else {
            Path tmp = Path.of(curDir + "/" + args);

            if (Files.exists(tmp)) {
                message(FTPCodes.FILE_UNAVAILABLE, "File already exists");
            } else {
                message(FTPCodes.OPEN_DATA_CONNECTION,
                        "Opening data connection for requested file " + args);
                try {
                    Files.createFile(tmp);
                } catch (IOException e) {
                    e.printStackTrace();
                    message(FTPCodes.FILE_UNAVAILABLE, "Failed to upload file");
                }
                message(FTPCodes.FINISH_TRANSFER, "File transfer successful. Closing data connection.");
            }
            closeDataConnection();
        }
    }

    private void fail() {
        message(FTPCodes.NOT_IMPLEMENTED, "Unknown error has occurred");
    }

    private void cd(String args) {
        if (args.equals("..")) {
            if (curDir.equals(root)) {
                message(FTPCodes.FILE_UNAVAILABLE, "You can't go higher the your root directory");
            } else {
                curDir = curDir.getParent();
                message(FTPCodes.OK, "Current directory has been changed to " + curDir);
            }
            return;
        }

        Path tmp;
        if ((args != null) && (!args.equals("."))) {
            tmp = Path.of(curDir.toString() + "/" + args);
            if (Files.exists(tmp) && Files.isDirectory(tmp) && tmp.startsWith(root)) {
                curDir = tmp;
                message(FTPCodes.OK, "Current directory has been changed to " + curDir);
            } else {
                message(FTPCodes.FILE_UNAVAILABLE, "Specified directory is unavailable");
            }
        }
    }

    private void mkdir(String args) {
        // only numbers and letters
        if (args != null && args.matches("^[a-zA-Z0-9]+$")) {
            if (!new File(curDir.toString() + "/" + args).mkdir()) {
                message(FTPCodes.FILE_UNAVAILABLE, "Failed to create new directory");
            } else {
                message(FTPCodes.OK, "Directory successfully created");
            }
        } else {
            message(FTPCodes.NAME_NOT_ALLOWED, "Invalid name");
        }
    }

    private void user(String username) throws IOException {
        if (username.equals("")) {
            username = clientIn.readLine();
            log(username, false);
        }
        message(FTPCodes.LOGIN_OK, "Input password");
        String password = clientIn.readLine();
        log(password, false);
        if (ThreadLocalRandom.current().nextBoolean()) {
            if (username.contains(" "))
                username = username.substring(username.indexOf(' ') + 1);

            root = Path.of("./src/main/resources/" + username);
            if (!Files.exists(root)) {
                new File(root.toString()).mkdir();
            }
            curDir = root;
            loggedIn = true;
            message(FTPCodes.PASSWORD_OK, "Login successful.");
        } else {
            message(FTPCodes.BAD_LOGIN, "Invalid username or password.");
        }
    }

    private void eprt(String args) {
        //     |2|::1|58770|
        String[] splitArgs = args.split("\\|");
        String ipAddress = splitArgs[2];
        int port = Integer.parseInt(splitArgs[3]);

        // Initiate data connection
        openDataConnection(ipAddress, port);
        message(FTPCodes.OK, "Command OK");
    }

    public void dir() throws IOException {
        if (dataSocket == null || dataSocket.isClosed()) {
            message(FTPCodes.CANT_OPEN_DATA_CONNECTION, "No data connection was established");
        } else {
            message(FTPCodes.START_TRANSFER, "Data Transfer started.");
            Files.list(curDir).forEach(d -> {
                try {
                    int start = d.getNameCount() - 2;
                    int length = d.getNameCount();
                    dataMessage(d.subpath(start, length).toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            dataMessage("..");
            message(FTPCodes.FINISH_TRANSFER, "File Transfer is successful.");
            closeDataConnection();
        }
    }

    public void start() throws IOException {
        log("FTP server started on port 21", true);

        clientSocket = serverSocket.accept();
        log("connection established with " + clientSocket.getInetAddress().toString(), true);

        clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
        clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        quit = false;

        // trash talk
        message(FTPCodes.SERVER_READY, "Connection Established. Welcome");
        clientIn.readLine();
        log("Please Log in.", true);
        clientOut.println("Please Log in.");

        //Log in
        user("");

        //working process
        while (!quit) {
            handleCommand(clientIn.readLine());
        }
    }

    public void stop() throws IOException {
        message(FTPCodes.CLOSING_CONNECTION, "Closing Connection");
        loggedIn = false;
        clientIn.close();
        clientOut.close();
        clientSocket.close();
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.serverSocket = new ServerSocket(21);
            while (true) {
                server.start();
            }
        } catch (IOException e) {
            System.err.println("Caught exception while creating Server socket");
            e.printStackTrace();
        }
    }
}
