/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import sandbox.clustering.DistanceMeasure;

/**
 *
 * @author Nikolay
 */
public abstract class IsotropicDistanceMeasure extends DistanceMeasure {

    @Override
    public abstract IsotropicDistanceMeasure clone();
}
