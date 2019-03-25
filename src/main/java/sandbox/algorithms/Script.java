/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.algorithms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.nio.file.FileSystems;
import java.nio.file.Files;

/**
 *
 * @author Nikolay
 */
public class Script {
    public static void main (String [] args) throws Exception{
        File dir = new File ("C:\\Users\\Nikolay\\Documents\\Local Working Folder\\TwoXar\\CMAP\\cmap_build02.volume1of7\\Batch2");
        
        fil: for(File f: dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
               return name.endsWith("CEL");
            }
        })){
            
            BufferedReader br = new BufferedReader(new FileReader(f));
            String s;
            while((s=br.readLine())!=null){
                if(s.contains("HT_HGU133A")){
                    br.close();
                    Files.move(FileSystems.getDefault().getPath(f.getPath()), FileSystems.getDefault().getPath("C:\\Users\\Nikolay\\Documents\\Local Working Folder\\TwoXar\\CMAP\\cmap_build02.volume1of7\\C:\\Users\\Nikolay\\Documents\\Local Working Folder\\TwoXar\\CMAP\\cmap_build02.volume1of7\\HT_HGU133A\\"+f.getName()));
                    continue fil;
                }
            }
        }
        
    }
}
