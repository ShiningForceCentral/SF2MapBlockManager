/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.gui;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.map.block.MapBlock;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author wiz
 */
public class BlockSlotPanel extends JPanel {
    
    MapBlock block;
    boolean showPriority;
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (block != null) {
            g.drawImage(block.getImage(), 0, 0, this.getWidth(), this.getHeight(), null);
            if (showPriority) {
                Tile[] tiles = block.getTiles();
                for (int i = 0; i < tiles.length; i++) {
                    if (tiles[i].isHighPriority()) {
                        g.setColor(Color.YELLOW);
                        g.drawOval((i%3)*8+3, (i/3)*3*8+3, 2, 2);
                        g.setColor(Color.BLACK);
                        g.drawOval((i%3)*8+2, (i/3)*3*8+2, 4, 4);
                    }
                }
            }
        }        
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }
    
    public MapBlock getBlock() {
        return block;
    }

    public void setBlock(MapBlock blockImage) {
        this.block = block;
        this.validate();
        this.repaint();
    }
    
    public boolean getShowPriority() {
        return showPriority;
    }

    public void setShowPriority(boolean showPriority) {
        this.showPriority = showPriority;
        this.validate();
        this.repaint();
    }
}
