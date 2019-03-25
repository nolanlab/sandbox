package net.sf.flowcyt.gp.modules.fcs2csv;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.flowcyt.cfcs.CFCSDataSet;
import org.flowcyt.cfcs.CFCSError;
import org.flowcyt.cfcs.CFCSKeyword;
import org.flowcyt.cfcs.CFCSKeywords;
import org.flowcyt.cfcs.CFCSListModeData;
import org.flowcyt.cfcs.CFCSParameter;
import org.flowcyt.cfcs.CFCSParameters;
import org.flowcyt.cfcs.CFCSSystem;

public class FCSInputDataset
  implements Iterable<String[]>, Closeable
{
  private CFCSSystem readSystem;
  private CFCSDataSet dataSet;
  private List<CFCSParameter> parameters;
  private CFCSListModeData data;
  private Boolean channelToScaleDefault = false;
  private Boolean doublePrecisionDefault = false;
  private char nameSeparatorDefault = ':';
  
  public FCSInputDataset(String uri)
    throws IOException, URISyntaxException, Exception
  {
    this(new URI(uri));
  }
  
  public FCSInputDataset(String uri, int datasetNo)
    throws IOException, URISyntaxException, Exception
  {
    this(new URI(uri), datasetNo);
  }
  
  public FCSInputDataset(File file)
    throws IOException, Exception
  {
    this(file.toURI());
  }
  
  public FCSInputDataset(File file, int datasetNo)
    throws IOException, Exception
  {
    this(file.toURI(), datasetNo);
  }
  
  public FCSInputDataset(URI uri)
    throws IOException, Exception
  {
    this(uri, 0);
  }
  
  public FCSInputDataset(URI uri, int datasetNo)
    throws IOException, Exception
  {
    this.readSystem = new CFCSSystem();
    try
    {
      this.readSystem.open(uri.toURL());
    }
    catch (CFCSError e)
    {
      if (e.errorNumber == -14) {
        throw new IOException(e.toString());
      }
      throw e;
    }
    catch (IllegalArgumentException ex)
    {
      throw new IOException("Cannot open non-absolute URIs (" + uri.toString() + ")");
    }
    this.dataSet = this.readSystem.getDataSet(datasetNo);
    this.parameters = new ArrayList();
    CFCSParameters pars = this.dataSet.getParameters();
    for (int i = 0; i < pars.getCount(); i++) {
      this.parameters.add(pars.getParameter(i));
    }
    if (this.dataSet.getData().getType() != 1) {
      throw new Exception("Not a list mode data set.");
    }
    this.data = ((CFCSListModeData)this.dataSet.getData());
  }
  
  public String[] getParameterNames()
  {
    String[] names = new String[this.parameters.size()];
    for (int i = 0; i < this.parameters.size(); i++) {
      names[i] = ((CFCSParameter)this.parameters.get(i)).getShortName();
    }
    return names;
  }
  
  public String[] getParameterLongNames()
  {
    String[] names = new String[this.parameters.size()];
    for (int i = 0; i < this.parameters.size(); i++) {
      try
      {
        names[i] = ((CFCSParameter)this.parameters.get(i)).getFullName();
      }
      catch (Exception e)
      {
        names[i] = null;
      }
    }
    return names;
  }
  
  public String[] getParameterFullNames()
  {
    return getParameterFullNames(this.nameSeparatorDefault);
  }
  
  public String[] getParameterFullNames(char sepCharacter)
  {
    String[] names = new String[this.parameters.size()];
    for (int i = 0; i < this.parameters.size(); i++)
    {
      String longName;
      try
      {
        longName = ((CFCSParameter)this.parameters.get(i)).getFullName();
      }
      catch (Exception e)
      {
        longName = null;
      }
      if ((longName != null) && (longName.length() > 0)) {
        names[i] = (((CFCSParameter)this.parameters.get(i)).getShortName() + sepCharacter + ((CFCSParameter)this.parameters.get(i)).getFullName());
      } else {
        names[i] = ((CFCSParameter)this.parameters.get(i)).getShortName();
      }
    }
    return names;
  }
  
  public int getEventCount()
  {
    return this.data.getCount();
  }
  
  public double[] getEventAsDoubles(int index)
  {
    return getEventAsDoubles(index, this.channelToScaleDefault);
  }
  
  public double[] getEventAsDoubles(int index, Boolean channelToScaleConversion)
  {
    double[] arrayD = new double[this.parameters.size()];
    if (channelToScaleConversion.booleanValue()) {
      this.data.getEvent(index, arrayD);
    } else {
      this.data.getEventAsInTheFile(index, arrayD);
    }
    return arrayD;
  }
  
  public float[] getEventAsFloats(int index)
  {
    return getEventAsFloats(index, this.channelToScaleDefault);
  }
  
  public float[] getEventAsFloats(int index, Boolean channelToScaleConversion)
  {
    double[] arrayD = new double[this.parameters.size()];
    float[] arrayF = new float[this.parameters.size()];
    if (channelToScaleConversion.booleanValue()) {
      this.data.getEvent(index, arrayD);
    } else {
      this.data.getEventAsInTheFile(index, arrayD);
    }
    for (int i = 0; i < this.parameters.size(); i++) {
      arrayF[i] = ((float)arrayD[i]);
    }
    return arrayF;
  }
  
  public String[] getEventAsStrings(int index)
  {
    return getEventAsStrings(index, this.channelToScaleDefault, this.doublePrecisionDefault);
  }
  
  public String[] getEventAsStrings(int index, Boolean channelToScaleConversion, Boolean doublePrecision)
  {
    String[] array = new String[this.parameters.size()];
    double[] arrayD = new double[this.parameters.size()];
    if (channelToScaleConversion.booleanValue()) {
      this.data.getEvent(index, arrayD);
    } else {
      this.data.getEventAsInTheFile(index, arrayD);
    }
    for (int i = 0; i < this.parameters.size(); i++) {
      if (isInteger(arrayD[i]).booleanValue()) {
        array[i] = Integer.toString((int)arrayD[i]);
      } else if (doublePrecision.booleanValue()) {
        array[i] = Double.toString(arrayD[i]);
      } else {
        array[i] = Float.toString((float)arrayD[i]);
      }
    }
    return array;
  }
  
  public static Boolean isInteger(double d)
  {
    if (Double.compare(d, (int)d) == 0) {
      return Boolean.valueOf(true);
    }
    return Boolean.valueOf(false);
  }
  
  public Iterator<String[]> iterator(){
   return new Iterator()
    {
      private int iteratorIndex = 0;
      
      public boolean hasNext()
      {
        return this.iteratorIndex < FCSInputDataset.this.getEventCount();
      }
      
      public String[] next()
      {
        if (this.iteratorIndex >= FCSInputDataset.this.getEventCount()) {
          this.iteratorIndex = 0;
        }
        return FCSInputDataset.this.getEventAsStrings(this.iteratorIndex++);
      }
      
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }
  
  public String[] getKeywordValuePairs(){
    CFCSKeywords keywords = this.dataSet.getKeywords();
    
    int count = keywords.getCount();
    String[] ret = new String[2 * count];
    for (int i = 0; i < count; i++)
    {
      CFCSKeyword keyword = keywords.getKeyword(i);
      ret[(2 * i)] = keyword.getKeywordName();
      ret[(2 * i + 1)] = keyword.getKeywordValue();
    }
    return ret;
  }
  
  public void close()
    throws IOException
  {
    this.readSystem.close();
  }
  
  public Boolean getChannelToScaleDefault()
  {
    return this.channelToScaleDefault;
  }
  
  public void setChannelToScaleDefault(Boolean channelToScaleDefault)
  {
    this.channelToScaleDefault = channelToScaleDefault;
  }
  
  public Boolean getDoublePrecisionDefault()
  {
    return this.doublePrecisionDefault;
  }
  
  public void setDoublePrecisionDefault(Boolean doublePrecisionDefault)
  {
    this.doublePrecisionDefault = doublePrecisionDefault;
  }
  
  public char getNameSeparatorDefault()
  {
    return this.nameSeparatorDefault;
  }
  
  public void setNameSeparatorDefault(char nameSeparatorDefault)
  {
    this.nameSeparatorDefault = nameSeparatorDefault;
  }
}
