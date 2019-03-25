/* ********************************************************************
 * Copyright (©) 2009 Josef Spidlen, Ph.D.
 * 
 * License
 * The software is distributed under the terms of the 
 * GNU Lesser General Public License (LGPL)
 * 
 * Disclaimer
 * This software and documentation come with no warranties of any kind.
 * This software is provided "as is" and any and any express or implied 
 * warranties, including, but not limited to, the implied warranties of
 * merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall the  copyright holder be liable for any direct, 
 * indirect, incidental, special, exemplary, or consequential damages
 * (including but not limited to, procurement of substitute goods or 
 * services; loss of use, data or profits; or business interruption)
 * however caused and on any theory of liability, whether in contract,
 * strict liability, or tort arising in any way out of the use of this 
 * software.    
 *  
 * Acknowledgment
 * This work has been supported by Brinkman/Sekaly Genome Canada 
 * Technology Development Project High Throughput High-Dimensional 
 * Multi-parametric Analysis of the Immune System.
 * 
 * ******************************************************************** 
 */
package sandbox.fcs;

import java.io.Closeable;
import java.io.IOException;
import net.sf.flowcyt.gp.module.csv2fcs.ConversionConfig;
import net.sf.flowcyt.gp.module.csv2fcs.FCSException;
import net.sf.flowcyt.gp.module.csv2fcs.HelperUtils;
import net.sf.flowcyt.gp.module.csv2fcs.IO;
import org.flowcyt.cfcs.CFCSData;
import org.flowcyt.cfcs.CFCSDataSet;
import org.flowcyt.cfcs.CFCSDatatype;
import org.flowcyt.cfcs.CFCSErrorCodes;
import org.flowcyt.cfcs.CFCSListModeData;
import org.flowcyt.cfcs.CFCSParameter;
import org.flowcyt.cfcs.CFCSParameters;
import org.flowcyt.cfcs.CFCSSystem;

/**
 * Simple encapsulation of an FCS data file / data set based on the CFCS
 * library.
 *
 * @author Josef Spidlen, Ph.D., BC Cancer Agency, jspidlen@bccrc.ca
 */
public class FCSDataset implements Closeable {

    /**
     * Main entry point, CFCS implementation of an FCS file
     */
    private CFCSSystem cfcsSystem;

    /**
     * Our list mode data set / the only data set in our file
     */
    private CFCSDataSet cfcsDs;

    /**
     * List mode data in the data set
     */
    private CFCSListModeData cfcsData;

    /**
     * What data type is being used for the list mode data set?
     */
    private int cfcsDatatype = CFCSDatatype.FLOAT;

    /**
     * The range for all parameters
     */
    private int range = 0;

    /**
     * What value of $PnB will be used in the data set (all parameters are using
     * the same size)
     */
    private int fieldSize;

    /**
     * Create a list mode data file / data set based on a file name, data type
     * (CFCSDatatype.FLOAT or CFCSDatatype.INTEGER) and fieldSize (value of
     * $PnB) and range ($PnR)for all parameters.
     *
     * @param fileName the name of the FCS file to store the
     * @param cfcsDatatype either CFCSDatatype.FLOAT or CFCSDatatype.INTEGER
     * @param fieldSize value for $PnB for all parameters, 8, 16, or 32
     * @param range value for $PnR for all parameters.
     */
    public FCSDataset(String fileName, int cfcsDatatype, int fieldSize, int range) {
        cfcsSystem = new CFCSSystem();
        cfcsSystem.create("file:" + fileName);
        this.cfcsDatatype = cfcsDatatype;
        this.fieldSize = fieldSize;
        cfcsDs = cfcsSystem.createDataSet(CFCSData.LISTMODE, cfcsDatatype);
        cfcsData = (CFCSListModeData) cfcsDs.getData();
        this.range = range;
    }

    /**
     * Create a list mode data file / data set based on a file name, data type
     * (CFCSDatatype.FLOAT or CFCSDatatype.INTEGER) and fieldSize (value of
     * $PnB) and range ($PnR)for all parameters. These are taken from a
     * ConversionConfig object config as config.getOutputFileName(),
     * config.getCfcsDatatype(), config.getNeededBits(), config.getRange().
     *
     * @param config conversion configuration, values used:
     * config.getOutputFileName(), config.getCfcsDatatype(),
     * config.getNeededBits(), config.getRange()
     */
    public FCSDataset(ConversionConfig config) {
        this(config.getOutputFileName(), config.getCfcsDatatype(), config.getNeededBits(), config.getRange());
    }

    /**
     * Add a parameter to the data set.
     *
     * @param name The name of the parameter; it may contain two parts separated
     * by ":". If this is the case, these will split into $PnN and $PnS names.
     * Otherwise, the $PnN name will be filled and the $PnS will be left empty.
     *
     * @throws FCSException if the parameter cannot be added.
     */
    public void addParameter(String name) throws FCSException {
        String[] names = HelperUtils.splitInTwoStrings(name, ':');
        addParameter(names[0], names[1]);
    }

    public void setRange(int range) {
        this.range = range;
    }

    /**
     * Add a parameter to the data set.
     *
     * @param shortName the value for the $PnN keyword
     * @param fullName the value for the $PnS keyword
     *
     * @throws FCSException if the parameter cannot be added.
     */
    public void addParameter(String shortName, String fullName) throws FCSException {
        CFCSParameters cfcsParams = cfcsDs.getParameters();
        shortName = shortName.replace(',', ';');

        CFCSParameter cfcsParam = new CFCSParameter();
        switch (cfcsDatatype) {
            case CFCSDatatype.INTEGER:
                cfcsParam.setFieldSize(fieldSize);
                break;
            case CFCSDatatype.FLOAT:
                cfcsParam.setFieldSize(32);
                break;
            default:
                throw new FCSException("Unsupported data type (" + cfcsDatatype
                        + ")", CFCSErrorCodes.CFCSNotImplemented);
        }

        if (shortName == null || shortName.length() <= 0) {
            throw new FCSException("Missing parameter name",
                    CFCSErrorCodes.CFCSIllegalName);
        }
        cfcsParam.setShortName(shortName);
        if (fullName != null && fullName.length() > 0) {
            cfcsParam.setFullName(fullName);
        }

        cfcsParam.setLogDecades(0);
        cfcsParam.setOffset(0);
        cfcsParam.setRange(range);
        cfcsParams.addParameter(cfcsParam);
    }

    /**
     * Add an event to the data set. Event values are in a String array that
     * gets converted to either floating point numbers or to integers.
     *
     * @param eventValues values to be add to the data set
     * @throws FCSException if values cannot be add, e.g., the array is longer
     * than the number of parameters.
     */
    public void addEvent(String eventValues[]) throws FCSException {
        switch (cfcsDatatype) {
            case CFCSDatatype.INTEGER: {
                int[] eventData = new int[eventValues.length];
                for (int i = 0; i < eventValues.length; i++) {
                    try {
                        eventData[i] = Math.round(Float.parseFloat(eventValues[i]));
                    } catch (NumberFormatException e) {
                        eventData[i] = 0;
                        CFCSParameter cfcsParam = cfcsDs.getParameters()
                                .getParameter(i);
                        IO.error("Failed to parse " + cfcsParam.getShortName()
                                + " parameter value: " + eventValues[i]
                                + "; setting to 0.");
                    }
                }
                cfcsData.addEvent(eventData);
            }
            break;
            case CFCSDatatype.FLOAT: {
                float[] eventData = new float[eventValues.length];
                for (int i = 0; i < eventValues.length; i++) {
                    try {
                        eventData[i] = Float.parseFloat(eventValues[i]);
                    } catch (NumberFormatException e) {
                        eventData[i] = 0;
                        CFCSParameter cfcsParam = cfcsDs.getParameters()
                                .getParameter(i);
                        IO.error("Failed to parse " + cfcsParam.getShortName()
                                + " parameter value: " + eventValues[i]
                                + "; setting to 0.");
                    }
                }
                cfcsData.addEvent(eventData);
            }
            break;
            default:
                throw new FCSException("Unsupported data type (" + cfcsDatatype
                        + ")", CFCSErrorCodes.CFCSNotImplemented);
        }

    }

    public void addEvent(float[] eventValues) throws FCSException {
        switch (cfcsDatatype) {
            case CFCSDatatype.INTEGER: {
                int[] eventData = new int[eventValues.length];
                for (int i = 0; i < eventValues.length; i++) {
                    try {
                        eventData[i] = Math.round(eventValues[i]);
                    } catch (NumberFormatException e) {
                        eventData[i] = 0;
                        CFCSParameter cfcsParam = cfcsDs.getParameters()
                                .getParameter(i);
                        IO.error("Failed to parse " + cfcsParam.getShortName()
                                + " parameter value: " + eventValues[i]
                                + "; setting to 0.");
                    }
                }
                cfcsData.addEvent(eventData);
            }
            break;
            case CFCSDatatype.FLOAT: {
                float[] eventData = new float[eventValues.length];
                for (int i = 0; i < eventValues.length; i++) {
                    try {
                        eventData[i] = eventValues[i];
                    } catch (NumberFormatException e) {
                        eventData[i] = 0;
                        CFCSParameter cfcsParam = cfcsDs.getParameters()
                                .getParameter(i);
                        IO.error("Failed to parse " + cfcsParam.getShortName()
                                + " parameter value: " + eventValues[i]
                                + "; setting to 0.");
                    }
                }
                cfcsData.addEvent(eventData);
            }
            break;
            default:
                throw new FCSException("Unsupported data type (" + cfcsDatatype
                        + ")", CFCSErrorCodes.CFCSNotImplemented);
        }

    }

    /**
     * Save the dataset and close the file.
     */
    public void close() throws IOException {
        cfcsSystem.close();
    }

}
