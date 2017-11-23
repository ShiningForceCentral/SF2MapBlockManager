/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.io;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.graphics.layout.DefaultLayout;
import com.sfc.sf2.map.block.MapBlock;
import com.sfc.sf2.map.block.layout.MapBlockLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author wiz
 */
public class PngManager {
    
    
    public static void exportPng(MapBlock[] blocks, String filepath, int blocksPerRow){
        try {
            //System.out.println("com.sfc.sf2.mapblock.io.PngManager.exportPng() - Exporting PNG files ...");
            writePngFile(blocks,filepath,blocksPerRow);    
            //System.out.println("com.sfc.sf2.mapblock.io.PngManager.exportPng() - PNG files exported.");
        } catch (Exception ex) {
            Logger.getLogger(PngManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
                
    }    
    
    public static void writePngFile(MapBlock[] blocks, String filepath, int blocksPerRow){
        try {
            //System.out.println("com.sfc.sf2.mapblock.io.PngManager.exportPng() - Exporting PNG file ...");
            BufferedImage image = new MapBlockLayout().buildImage(blocks, blocksPerRow, true);
            File outputfile = new File(filepath);
            //System.out.println("File path : "+outputfile.getAbsolutePath());
            ImageIO.write(image, "png", outputfile);
            System.out.println("PNG file exported : " + outputfile.getAbsolutePath());
        } catch (Exception ex) {
            Logger.getLogger(PngManager.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
       
    
}
