package at.htl.remotecontrol;

import at.htl.remotecontrol.actions.RobotAction;
import at.htl.remotecontrol.actions.RobotActionQueue;
import at.htl.remotecontrol.server.TeacherServer;

import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client {

    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final Robot robot;
    private RobotActionQueue jobs = new RobotActionQueue();
    private final ProcessorThread processor;
    private final ReaderThread reader;

    public Client(String serverMachine, String studentName)
            throws IOException, AWTException {
        Socket socket = new Socket(serverMachine, TeacherServer.PORT);
        robot = new Robot();
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        out.writeObject(studentName);
        out.flush();
        processor = new ProcessorThread();
        reader = new ReaderThread();
    }

    private class ReaderThread extends Thread {
        public void run() {
            try {
                RobotAction action;
                while ((action = (RobotAction) in.readObject()) != null) {
                    if (!action.equals(jobs.peekLast())) {
                        jobs.add(action);
                        System.out.println("jobs = " + jobs);
                    } else {
                        System.out.println("Discarding duplicate request");
                    }
                }
            } catch (EOFException eof) {
                System.out.println("Connection closed");
            } catch (Exception ex) {
                System.out.println("Connection closed abruptly: " + ex);
            }
        }
    }

    private class ProcessorThread extends Thread {
        public ProcessorThread() {
            super("ProcessorThread");
            setDaemon(true);
        }

        public void run() {
            try {
                while (!isInterrupted()) {
                    try {
                        RobotAction action = jobs.take();
                        Object result = action.execute(robot);
                        if (result != null) {
                            out.writeObject(result);
                            out.reset();
                            out.flush();
                        }
                    } catch (InterruptedException e) {
                        interrupt();
                        break;
                    }
                }
                out.close();
            } catch (IOException e) {
                System.out.println("Connection closed (" + e + ')');
            }
        }
    }

    public void start() {
        processor.start();
        reader.start();
    }

    public void stop() {
        processor.interrupt();
        reader.interrupt();
    }

}