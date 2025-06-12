/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.gui;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.map.block.MapBlock;
import com.sfc.sf2.map.block.layout.MapBlockLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

/**
 *
 * @author TiMMy
 */
public class EditableBlockSlotPanel extends BlockSlotPanel implements MouseListener, MouseMotionListener {
    
    private MapBlockLayout mapBlockLayout;
    private TileSlotPanel leftTileSlotPanel;
    private TileSlotPanel rightTileSlotPanel;
    
    private boolean drawGrid;
    private boolean showPriority;
    
    BufferedImage image;
    
    public EditableBlockSlotPanel() {
       addMouseListener(this);
       addMouseMotionListener(this);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) {
            image = new BufferedImage(3*8, 3*8, BufferedImage.TYPE_INT_ARGB);
            Graphics g2 = image.getGraphics();
            if (block != null) {
                if(block.getImage() == null) {
                    IndexColorModel icm = buildIndexColorModel(block.getTiles()[0].getPalette());
                    BufferedImage blockImage = new BufferedImage(3*8, 3*8 , BufferedImage.TYPE_BYTE_INDEXED, icm);
                    Graphics blockGraphics = blockImage.getGraphics();                    
                    blockGraphics.drawImage(block.getTiles()[0].getImage(), 0*8, 0*8, null);
                    blockGraphics.drawImage(block.getTiles()[1].getImage(), 1*8, 0*8, null);
                    blockGraphics.drawImage(block.getTiles()[2].getImage(), 2*8, 0*8, null);
                    blockGraphics.drawImage(block.getTiles()[3].getImage(), 0*8, 1*8, null);
                    blockGraphics.drawImage(block.getTiles()[4].getImage(), 1*8, 1*8, null);
                    blockGraphics.drawImage(block.getTiles()[5].getImage(), 2*8, 1*8, null);
                    blockGraphics.drawImage(block.getTiles()[6].getImage(), 0*8, 2*8, null);
                    blockGraphics.drawImage(block.getTiles()[7].getImage(), 1*8, 2*8, null);
                    blockGraphics.drawImage(block.getTiles()[8].getImage(), 2*8, 2*8, null);
                    block.setImage(blockImage);
                    blockGraphics.dispose();
                }
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
            }
            if (drawGrid) {
                g2.setColor(Color.BLACK);
                for (int i = 0; i <= 4; i++) {
                    g2.drawLine(i*8, 0, i*8, 4*8);
                    g2.drawLine(0, i*8, 4*8, i*8);
                }
            }
            g2.dispose();
            g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
        }
    }
    
    private IndexColorModel buildIndexColorModel(Color[] colors){
        byte[] reds = new byte[16];
        byte[] greens = new byte[16];
        byte[] blues = new byte[16];
        byte[] alphas = new byte[16];
        //reds[0] = (byte)0xFF;
        //greens[0] = (byte)0xFF;
        //blues[0] = (byte)0xFF;
        for(int i=0;i<16;i++){
            reds[i] = (byte)colors[i].getRed();
            greens[i] = (byte)colors[i].getGreen();
            blues[i] = (byte)colors[i].getBlue();
            alphas[i] = (byte)0xFF;
        }
        alphas[0] = 0;
        IndexColorModel icm = new IndexColorModel(4,16,reds,greens,blues,0);
        return icm;
    }
    
    public void setMapBlockLayout(MapBlockLayout mapBlockLayout) {
        this.mapBlockLayout = mapBlockLayout;
    }
    
    public boolean getDrawGrid() {
        return drawGrid;
    }

    public void setDrawGrid(boolean drawGrid) {
        this.drawGrid = drawGrid;
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
    
    public TileSlotPanel getLeftTileSlotPanel() {
        return leftTileSlotPanel;
    }

    public void setLeftTileSlotPanel(TileSlotPanel leftTileSlotPanel) {
        this.leftTileSlotPanel = leftTileSlotPanel;
    }
    
    public TileSlotPanel getRightTileSlotPanel() {
        return rightTileSlotPanel;
    }

    public void setRightTileSlotPanel(TileSlotPanel rightTileSlotPanel) {
        this.rightTileSlotPanel = rightTileSlotPanel;
    }

    @Override
    public void setBlock(MapBlock block) {
        super.setBlock(block);
        image = null;
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (block == null)
            return;
        
        int x = e.getX() / (getWidth() / 3);
        int y = e.getY() / (getHeight() / 3);
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                Tile leftSlotTile = leftTileSlotPanel.getTile();
                if (leftSlotTile != null) {
                    Tile[] tiles = block.getTiles();
                    tiles[x + y*3] = cloneTile(leftSlotTile, tiles[x + y*3].isHighPriority());
                    onBlockUpdated();
                }   break;
            case MouseEvent.BUTTON2:
                block.getTiles()[x + y*3].setHighPriority(!block.getTiles()[x + y*3].isHighPriority());
                onBlockUpdated();
                break;
            case MouseEvent.BUTTON3:
                Tile rightSlotTile = rightTileSlotPanel.getTile();
                if (rightSlotTile != null) {
                    Tile[] tiles = block.getTiles();
                    tiles[x + y*3] = cloneTile(rightSlotTile, tiles[x + y*3].isHighPriority());
                    onBlockUpdated();
                }   break;
            default:
                break;
        }
    }
    
    Tile cloneTile(Tile tile, boolean isHighPriority) {
        Tile newTile = new Tile();
        newTile.setId(tile.getId());
        newTile.setPalette(tile.getPalette());
        newTile.setPixels(tile.getPixels());
        newTile.setHighPriority(isHighPriority);
        newTile.sethFlip(tile.ishFlip());
        newTile.setvFlip(tile.isvFlip());
        return newTile;
    }
    
    private void onBlockUpdated() {
        block.setImage(null);
        mapBlockLayout.mapBlocksChanged();
        mapBlockLayout.revalidate();
        mapBlockLayout.repaint();
        this.image = null;
        this.revalidate();
        this.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
