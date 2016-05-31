package at.htl.common.transfer;

import java.io.File;
import java.io.Serializable;
import java.time.LocalTime;

/**
 * @timeline HandOutPackage
 * 31.10.2015: MET 005  created class
 */

/**
 * Diese Klasse verwaltet alle Informationen, die für den
 * "Client" bzw. für den Schüler relevant sind.
 */
@Deprecated
public class HandOutPackage implements Serializable {

    private File file;
    private LocalTime endTime;
    private String comment;

    /**
     * A package with information for the test.
     *
     * @param file    Specialises the file where the test-questions are listed.
     * @param endTime Specialises the time the test ends.
     * @param comment Specialises a comment from the teacher to the client for the test.
     */
    public HandOutPackage(File file, LocalTime endTime, String comment) {
        this.file = file;
        this.endTime = endTime;
        this.comment = comment;
    }

    //region Getter ans Setter
    public File getFile() {
        return file;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getComment() {
        return comment;
    }

    //endregion

}
