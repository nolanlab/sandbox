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

import java.io.BufferedWriter;
import java.io.IOException;
import javax.naming.ConfigurationException;
import org.flowcyt.cfcs.CFCSDatatype;
import net.sf.flowcyt.gp.module.csv2fcs.ConversionConfig.SuggestedConfiguration;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.FileSystem;
import java.util.Arrays;
import net.sf.flowcyt.gp.module.csv2fcs.ConversionConfig;
import net.sf.flowcyt.gp.module.csv2fcs.FCSException;
import net.sf.flowcyt.gp.module.csv2fcs.IO;
import util.logger;

/**
 * <b>Converts a Comma separated values (CSV) file to a Flow Cytometry Standard
 * (FCS) file</b><br>
 * A java command line tool designed as a GenePattern Module.
 * <p>
 * The CSV file shall contain a header with parameter names. Each name may
 * contain two parts separated by ":", e.g., "FL1-H:CD4". If two parts are
 * detected then these will be split into $PnN and $PnS in the resulting FCS
 * file. If only a single name is detected, it will be used as the $PnN name
 * leaving $PnS empty.
 * </p><p>
 * Range ($PnR) and data type ($DATATYPE, float or int) for the resulting FCS
 * file may specified. By default, this tool will try to set the best settings
 * based on the data in the file. This is the floating point data type with 32
 * bits if floating point numbers are detected. Otherwise, integer will be used
 * with the lowest number of bits (8, 16, 32) that allows to capture the data.
 * By default, the range will be set to the lowest power of 2 that is greater
 * than the largest number in the data set.
 * </p><p>
 * Additional parameters include the input and output file name and whether
 * debug mode should be used.
 * </p><p>
 * <b>Input:</b>
 * <ul>
 * <li>-InputFile - a CSV file to be converted (required)</li>
 * <li>-OutputFile - output file name (required)</li>
 * <li>-Range - What range (value of the $PnR keyword) shall be used (optional).
 * Supported values:<ul>
 * <li>auto - Use the lowest power of 2 that is greater than the largest number
 * in the data set (default).</li>
 * <li>auto-exact - Use the largest number in the data set increased by
 * one.</li>
 * <li>BD - Use 262144 (2^18), default used by modern BD instruments.</li>
 * <li>&lt;integer value&gt; - Use the explicitly specified &lt;integer
 * value&gt;.</li></ul></li>
 * <li>-DataType - What data type (value of the $DATATYPE keyword shall be used
 * (optional). Values can be stored as floating points (32 bits) or integers (8,
 * 16, or 32 bits). By default the floating point data type will be used if
 * floating point numbers are detected. Otherwise, integer will be used with the
 * lowest number of bits (8, 16, 32) that allows to capture the data. This can
 * be overwritten as follows:<ul>
 * <li>auto - use the best settings as specified</li>
 * <li>int - forces the integer data type. Floating point numbers will be
 * rounded to integers. Either 8, 16, or 32 bits will automatically be selected
 * to accommodate for all data.</li>
 * <li>float - forces floating point output (even for integer
 * data).</li></ul></li>
 * <li>-Debug - produces debug output if specified (optional)</li>
 * <li>-Version - output the version of the software and exit (no conversion is
 * done).</li>
 * <li>-Help - output the command line syntax and exit (no conversion is
 * done).</li>
 * </ul>
 * <p>
 * <b>Output:</b>
 * <ul>
 * <li>An FCS (3.0) file with events from the input CSV file.</li>
 * </ul>
 *
 * @author Josef Spidlen, Ph.D., BC Cancer Agency, jspidlen@bccrc.ca
 */
public class ExportFCS {

    /**
     * Parsed configuration from the command line
     */
    private ConversionConfig config = null;

    /**
     * Suggested configuration, includes datatype, range, and number of bits
     */
    private SuggestedConfiguration suggested = null;

    /**
     * Number of events converted, debug information only
     */
    private int eventsConvertedDgbInf = 0;

    public void writeFCSAsFloat(String out, float[][] events, String[] shortParamNames, String[] longParamNames) {

        try {
            File csvTmp = new File("temp.csv");
            csvTmp.createNewFile();
            BufferedWriter br = new BufferedWriter(new FileWriter(csvTmp));
            br.write(0);
            config = new ConversionConfig(new String[]{"-InputFile:temp.csv", "-Range:auto", "-DataType:float"});

            csvTmp.delete();

            config.validate();
            IO.debug("Conversion started.");

            config.setRange(262144);
            config.setCfcsDatatype(CFCSDatatype.FLOAT);
            config.setNeededBits(32);

            // Create the FCS data set and a parser to read the CSV file
            File outF = new File(out);
            outF.getParentFile().mkdirs();

            FCSDataset fcs = new FCSDataset(out, config.getCfcsDatatype(), config.getNeededBits(), config.getRange());

            float[] maxVal = new float[events[0].length];

            Arrays.fill(maxVal, 10.0f);
            
            int evtN = 0;

            for (float[] e : events) {
                for (int i = 0; i < maxVal.length; i++) {
                    if(Float.isFinite(e[i])){
                    maxVal[i] = Math.max(maxVal[i], e[i]);
                    }else{
                        logger.print("Invalid evt: row="+evtN + ", col="+i+ ", val=" + e[i]);
                    }
                }
                evtN++;
            }

            for (int i = 0; i < longParamNames.length; i++) {
                fcs.setRange((int) (maxVal[i] * 1.1));
                fcs.addParameter(shortParamNames[i], longParamNames[i]);
            }

            for (float[] evt : events) {
                fcs.addEvent(evt);
                eventsConvertedDgbInf++;
            }
            // Clean up, includes saving the FCS file
            fcs.close();

            IO.debug("Conversion finished, converted " + eventsConvertedDgbInf + " events.");

        } catch (IOException e) {
            IO.error("IO Error: " + e.getMessage());
        } catch (FCSException e) {
            IO.error("FCS Error: " + e.getMessage());
        } catch (ConfigurationException e) {
            IO.error("Configuration Error: " + e.getMessage());

        }
    }

}
