/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.gui;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.map.block.MapBlock;
import com.sfc.sf2.map.block.layout.MapBlockLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;

/**
 *
 * @author TiMMy
 */
public class EditableBlockSlotPanel extends BlockSlotPanel implements MouseListener, MouseMotionListener {
    
    private MapBlockLayout mapBlockLayout;
    private TileSlotPanel leftTileSlotPanel;
    private TileSlotPanel rightTileSlotPanel;
    
    public static final int MODE_PAINT_TILE = 0;
    public static final int MODE_TOGGLE_PRIORITY = 1;
    public static final int MODE_TOGGLE_FLIP = 2;
    private int currentMode = 0;
    
    private boolean drawGrid;
    private boolean showPriorityFlag;
    
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
            Graphics2D g2 = (Graphics2D)image.getGraphics();
            if (block != null) {
                if(block.getImage() == null) {
                    IndexColorModel icm = buildIndexColorModel(block.getTiles()[0].getPalette());
                    BufferedImage blockImage = new BufferedImage(3*8, 3*8 , BufferedImage.TYPE_BYTE_INDEXED, icm);
                    drawIndexedColorPixels(blockImage, block.getTiles()[0].getPixels(), 0*8, 0*8);
                    drawIndexedColorPixels(blockImage, block.getTiles()[1].getPixels(), 1*8, 0*8);
                    drawIndexedColorPixels(blockImage, block.getTiles()[2].getPixels(), 2*8, 0*8);
                    drawIndexedColorPixels(blockImage, block.getTiles()[3].getPixels(), 0*8, 1*8);
                    drawIndexedColorPixels(blockImage, block.getTiles()[4].getPixels(), 1*8, 1*8);
                    drawIndexedColorPixels(blockImage, block.getTiles()[5].getPixels(), 2*8, 1*8);
                    drawIndexedColorPixels(blockImage, block.getTiles()[6].getPixels(), 0*8, 2*8);
                    drawIndexedColorPixels(blockImage, block.getTiles()[7].getPixels(), 1*8, 2*8);
                    drawIndexedColorPixels(blockImage, block.getTiles()[8].getPixels(), 2*8, 2*8);
                    block.setImage(blockImage);
                }
                g2.drawImage(block.getImage(), 0, 0, 3*8, 3*8, null);
                if (showPriorityFlag) {
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
    
    public void drawIndexedColorPixels(BufferedImage image, int[][] pixels, int x, int y){
        byte[] data = ((DataBufferByte)(image.getRaster().getDataBuffer())).getData();
        int width = image.getWidth();
        for(int i=0;i<pixels.length;i++){
            for(int j=0;j<pixels[i].length;j++){
                data[(y+j)*width+x+i] = (byte)(pixels[i][j]);
            }
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
    
    public int getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(int currentMode) {
        this.currentMode = currentMode;
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
        return showPriorityFlag;
    }

    public void setShowPriority(boolean showPriorityFlag) {
        this.showPriorityFlag = showPriorityFlag;
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
        switch (currentMode) {
            case MODE_PAINT_TILE:
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Tile leftSlotTile = leftTileSlotPanel.getTile();
                    if (leftSlotTile != null) {
                        Tile[] tiles = block.getTiles();
                        tiles[x + y*3] = cloneTile(leftSlotTile, tiles[x + y*3].isHighPriority());
                        onBlockUpdated();
                    }
                }
                else if (e.getButton() == MouseEvent.BUTTON3) {
                    Tile rightSlotTile = rightTileSlotPanel.getTile();
                    if (rightSlotTile != null) {
                        Tile[] tiles = block.getTiles();
                        tiles[x + y*3] = cloneTile(rightSlotTile, tiles[x + y*3].isHighPriority());
                        onBlockUpdated();
                    }
                }
                break;
            case MODE_TOGGLE_FLIP:
                if (e.getButton() == MouseEvent.BUTTON1) {
                    block.getTiles()[x + y*3] = Tile.hFlip(block.getTiles()[x + y*3]);
                    onBlockUpdated();
                }
                else if (e.getButton() == MouseEvent.BUTTON2) {
                    if (block.getTiles()[x + y*3].ishFlip()) {
                        block.getTiles()[x + y*3] = Tile.hFlip(block.getTiles()[x + y*3]);
                    }
                    else if (block.getTiles()[x + y*3].ishFlip()) {
                        block.getTiles()[x + y*3] = Tile.vFlip(block.getTiles()[x + y*3]);
                    }
                    onBlockUpdated();
                }
                else if (e.getButton() == MouseEvent.BUTTON3) {
                    block.getTiles()[x + y*3] = Tile.vFlip(block.getTiles()[x + y*3]);
                    onBlockUpdated();
                }
                break;
            case MODE_TOGGLE_PRIORITY:
                if (e.getButton() == MouseEvent.BUTTON1) {
                    block.getTiles()[x + y*3].setHighPriority(!block.getTiles()[x + y*3].isHighPriority());
                    onBlockUpdated();
                }
                break;
        }
    }
    
    private Tile cloneTile(Tile tile, boolean isHighPriority) {
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
        block.setIndexedColorImage(null);
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
