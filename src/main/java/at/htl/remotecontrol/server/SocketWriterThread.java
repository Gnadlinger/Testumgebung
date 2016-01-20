package at.htl.remotecontrol.server;

import at.htl.remotecontrol.actions.*;
import at.htl.remotecontrol.entity.FileStream;
import at.htl.remotecontrol.entity.Session;
import at.htl.remotecontrol.entity.Student;
import javafx.scene.chart.XYChart;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * Im SocketWriterTrhead wird die Wartezeit zwischen Screenshots kontrolliert
 *
 * @timeline Text
 * 27.10.2015: PHI 025  Student wird nach dem Logout aus der Liste entfernt
 * 31.10.2015: MET 060  Angabe zur Verfügung stellen
 * 11.12.2015: PHI 010  LoC werden immer mit dem Screenshot berechnet
 * 06.01.2016: PHI 005  Serie bekommt einen Namen zur vermeidung von doppelten Serien
 * 12.01.2016: PHI 055  Writer fragt jetzt auch nach dem Lines of Code.
 * 13.01.2016: PHI 045  Schweren Fehler behoben bei der Abfrage nach LinesOfCode & Screenshot (PipeÜberlastung)
 * 13.01.2016: PHI 015  Einführen des TransferPackets.
 */
class SocketWriterThread extends Thread {

    private final Student student;
    private final ObjectOutputStream out;
    private final RobotActionQueue jobs;

    public SocketWriterThread(Student student,
                              ObjectOutputStream out) {
        super("Writer to " + student.getName());
        this.student = student;
        this.out = out;
        this.jobs = new RobotActionQueue();
    }

    /**
     * the time to wait, before taking another screenshot.
     *
     * @return the time to wait.
     */
    public long getWaitTime() {
        return Session.getInstance().getInterval();
    }

    /**
     * adds a new job for the robot.
     * The Screenshot from the Student.
     * Counts the Lines of Code from the Student.
     *
     * @param student The student, of the screenshot and LinesOfCode
     */
    private void startLittleHarvester(Student student) {
        jobs.add(new LittleHarvester(student.getName(), student.getPathOfWatch()));
    }


    /**
     * sends the test-file to the students.
     */
    public void handOut() {
        FileStream.send(out, Session.getInstance().getHandOutFile());
    }

    public void run() {
        handOut();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startLittleHarvester(student);
        XYChart.Series<Number, Number> seri = new XYChart.Series<>();
        seri.setName(student.getName() + "/" + LocalTime.now());

        Session.getInstance()
                .findStudentByName(student.getName())
                .addSeries(seri);

        try {
            while (!isInterrupted()) {
                try {
                    RobotAction action = jobs.poll(
                            getWaitTime(), TimeUnit.SECONDS);
                    if (action == null) {
                        startLittleHarvester(student);
                    } else {
                        out.writeObject(action);
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
            System.out.println("Connection to " + student.getName() + " closed (" + e + ')');
        }
        System.out.println("Closing connection to " + student.getName());

        // REMOVES the student from the list / marks him in a different color + plays a sound
        Session.getInstance().removeStudent(student.getName());
        Session.getInstance().notification();
    }

}