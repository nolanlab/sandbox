/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import java.io.Serializable;

/**
 *
 * @author Nikolay
 */
public interface BarCode extends Serializable {

    long serialVersionUID = 258L;

    double[] getProfile();

    double[] getRawValues();

    int getSideVectorBeginIdx();

    String[] getParameterNames();
}
