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
    
    BufferedImage image;
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (block != null) {
            if (image == null) {
                image = new BufferedImage(3*8, 3*8, BufferedImage.TYPE_INT_ARGB);
                Graphics g2 = image.getGraphics();
                g2.drawImage(block.getImage(), 0, 0, 3*8, 3*8, null);
                if (showPriority) {
                    Tile[] tiles = block.getTiles();
                    for (int t = 0; t < tiles.length; t++) {
                        if (tiles[t].isHighPriority()) {
                            g2.setColor(Color.BLACK);
                            g2.fillRect((t%3)*8+2, (t/3)*8+2, 4, 4);
                            g2.setColor(Color.YELLOW);
                            g2.fillRect((t%3)*8+3, (t/3)*8+3, 2, 2);
                        }
                    }
                }
                g2.dispose();
            }
            g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }
    
    public MapBlock getBlock() {
        return block;
    }

    public void setBlock(MapBlock block) {
        this.block = block;
        image = null;
        this.validate();
        this.repaint();
    }
    
    public boolean getShowPriority() {
        return showPriority;
    }

    public void setShowPriority(boolean showPriority) {
        this.showPriority = showPriority;
        image = null;
        this.validate();
        this.repaint();
    }
}
