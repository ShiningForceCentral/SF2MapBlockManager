/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.gui;

import com.sfc.sf2.map.block.MapBlock;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author wiz
 */
public class BlockSlotPanel extends JPanel {
    
    MapBlock block;
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (block != null) {
            g.drawImage(block.getIndexedColorImage(), 0, 0, this.getWidth(), this.getHeight(), null);
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
        this.validate();
        this.repaint();
    }
}
