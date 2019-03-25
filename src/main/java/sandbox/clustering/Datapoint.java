/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import java.io.Serializable;
import java.util.Arrays;
import util.MatrixOp;

/**
 *
 * @author Nikolay
 */
public class Datapoint implements Serializable, Comparable<Datapoint> {

    // private double[] unityLenVec = null;
    public static final long serialVersionUID = 206L;
    private double[] vector;
    private double[] sideVector;
    private String name;
    private int ID;
    private String filename;
    private int idWithinFile;
    //   private String tag;
    private static final double[] svStub = new double[0];

    public double[] getSideVector() {
        if (sideVector == null) {
            return svStub;
        }
        return sideVector;
    }

    public String getFilename() {
        return filename;
    }

    public int getIndexInFile() {
        return idWithinFile;
    }

    public String deepToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getFullName());
        double[] d = getVector();
        sb.append(", ");
        for (int i = 0; i < d.length; i++) {
            sb.append(d[i]);
            sb.append(", ");
        }
        return sb.toString();
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    @Override
    public int compareTo(Datapoint o) {
        return this.getID() - o.getID();
    }

    public double[] getUnityLengthVector() {
        return MatrixOp.toUnityLen(vector);
    }

    @Override
    public int hashCode() {
        return (ID == 0) ? (this.getFullName().hashCode() + Arrays.toString(vector).hashCode()) : ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Datapoint)) {
            return false;
        } else {
            return ((Datapoint) obj).getID()== this.getID();
        }
    }

    public boolean isUnnamed() {
        return name == null;
    }

    /**
     *
     * @return ID of the datapoint within its dataset - always zero-based
     */
    public int getID() {
        return ID;
    }

    public String getFullName() {
        return (filename + " " + String.format("Event %07d", idWithinFile) + " " + getName()).trim();
    }

    public String getName() {
        //String evn = String.format("Event %07d", idWithinFile);
        return (name == null) ? "" : name;
    }

    /*
     * not persisted
     */
    public Datapoint(String name, double[] vector, int ID) {
        this.vector = vector;
        this.name = name;
        this.ID = ID;
        this.sideVector = null;
        this.filename = "";
        this.idWithinFile = ID;
    }

    public Datapoint(String name, double[] vector, double[] sideVector, int ID) {
        this.vector = vector;
        this.name = name;
        this.ID = ID;
        this.sideVector = sideVector;
        this.filename = "";
        this.idWithinFile = ID;
    }

    public Datapoint(String name, double[] vector, double[] sideVector, int ID, String filename, int idWithinFile) {
        this.vector = vector;
        this.name = name;
        this.ID = ID;
        this.sideVector = sideVector;
        this.filename = filename;
        this.idWithinFile = idWithinFile;
    }

    public double[] getVector() {
        return vector;
    }

    @Override
    public String toString() {
        return name;// + ", " + ID;
    }

    @Override
    public Datapoint clone() {
        return new Datapoint(name, MatrixOp.copy(vector), MatrixOp.copy(sideVector), ID, filename, idWithinFile);
    }
}
