/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.algorithms;

import java.io.File;

/**
 *
 * @author Nikolay
 */
public class massRename {

    public static void main(String[] args) {
            File dir = new File("D:\\ABSeek\\20_normal_mrl\\11");
            for (File f : dir.listFiles()) {
                f.renameTo(new File(f.getPath().replaceAll("_1_", "_11_")));
            }

    }
}
