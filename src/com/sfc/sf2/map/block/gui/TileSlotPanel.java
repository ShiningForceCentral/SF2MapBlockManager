/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.gui;

import com.sfc.sf2.graphics.Tile;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author TiMMy
 */
public class TileSlotPanel extends JPanel {
    
    Tile tile;
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (tile != null) {
            g.drawImage(tile.getIndexedColorImage(), 0, 0, this.getWidth(), this.getHeight(), null);
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }
    
    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
        this.validate();
        this.repaint();
    }
}
