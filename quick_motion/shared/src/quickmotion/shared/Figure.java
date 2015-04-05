package quickmotion.shared;

import java.util.*;

/**
 *
 */
public class Figure implements Cloneable {
    private static long id_counter = 0;

    private long id;
    private LinkedList<DrawnLine> lines;

    public Figure() {
        this.lines = new LinkedList<DrawnLine>();
        this.id = id_counter++;
    }

    public Figure(LinkedList<DrawnLine> lines) {
        this.lines = lines;
        this.id = id_counter++;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int size() {
        return lines.size();
    }

    public List<DrawnLine> getLines() {
        return lines;
    }

    public void addLine(DrawnLine line) {
        synchronized (lines) {
            lines.add(line);
        }
    }

    public void removeLine(DrawnLine l) {
        synchronized (lines) {
            lines.remove(l);
        }
    }

    @SuppressWarnings("unchecked")
    public Figure clone() {
        return new Figure((LinkedList)lines.clone());
    }

}
