/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.gui;

import com.sfc.sf2.map.block.MapBlock;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author wiz
 */
public class BlockSlotPanel extends JPanel{
    
    BufferedImage blockImage;
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        if(blockImage!=null){
            g.drawImage(blockImage,0,0,null);
        }        
    }    
    
    public BufferedImage getBlockImage() {
        return blockImage;
    }

    public void setBlockImage(BufferedImage blockImage) {
        this.blockImage = blockImage;
    }
    
}
