/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.gui;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.map.block.Tileset;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import javax.swing.JPanel;

/**
 *
 * @author TiMMy
 */
public class TilesetsPanel extends JPanel implements MouseListener, MouseMotionListener {
    
    public static int selectedTileIndex0;
    public static int selectedTileIndex1;
    
    private EditableBlockSlotPanel blockSlotPanel;
    private TileSlotPanel leftSlotTilePanel;
    private TileSlotPanel rightSlotTilePanel;
    
    private static final int DEFAULT_TILES_PER_ROW = 24;
    
    private int tilesPerRow = DEFAULT_TILES_PER_ROW;
    private Tileset[] tilesets;
    private int selectedTileset = 0;
    private int currentDisplaySize = 1;
    private boolean drawGrid = true;
    
    private BufferedImage currentImage;
    private boolean redraw = true;
    private int renderCounter = 0;  
    
    public TilesetsPanel() {
       addMouseListener(this);
       addMouseMotionListener(this);
    }
   
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);   
        g.drawImage(buildImage(), 0, 0, this);       
    }
    
    public BufferedImage buildImage() {
        if(redraw){
            currentImage = buildImage(this.tilesets[selectedTileset], this.tilesPerRow);
            setSize(currentImage.getWidth(), currentImage.getHeight());
        }
        return currentImage;
    }
    
    public BufferedImage buildImage(Tileset tileset, int tilesPerRow) { 
        renderCounter++;
        System.out.println("Tileset render "+renderCounter);
        if (redraw && tileset != null) {
            Tile[] tiles = tileset.getTiles();
            int tileHeight = tiles.length/tilesPerRow + ((tiles.length%tilesPerRow!=0)?1:0);
            Color[] palette = tiles[0].getPalette();
            IndexColorModel icm = buildIndexColorModel(palette);
            currentImage = new BufferedImage(tilesPerRow*8+1, tileHeight*8+1, BufferedImage.TYPE_BYTE_INDEXED, icm);
            Graphics2D graphics = (Graphics2D)currentImage.getGraphics(); 
            for(int i=0; i<tiles.length; i++) {
                int baseX = (i % tilesPerRow)*8;
                int baseY = (i / tilesPerRow)*8;
                Tile tile = tiles[i];
                BufferedImage tileImage = tile.getImage();
                if(tileImage != null) {
                    graphics.drawImage(tileImage, baseX, baseY, null);
                }
            }
            if (drawGrid) {
                int width = tilesPerRow+1;
                int height = tiles.length/tilesPerRow+1;
                graphics.setColor(Color.BLACK);
                graphics.setStroke(new BasicStroke(1));
                for (int i = 0; i <= width; i++) {
                    graphics.drawLine(i*8, 0, i*8, height*8);
                }
                for (int j = 0; j <= height; j++) {
                    graphics.drawLine(0, j*8, width*8, j*8);
                }
            }
            graphics.dispose();
        }
        currentImage = resize(currentImage);
        redraw = false;
        return currentImage;
    }
    
    private IndexColorModel buildIndexColorModel(Color[] colors) {
        byte[] reds = new byte[16];
        byte[] greens = new byte[16];
        byte[] blues = new byte[16];
        byte[] alphas = new byte[16];
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
    
    private BufferedImage resize(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth()*currentDisplaySize, image.getHeight()*currentDisplaySize, BufferedImage.TYPE_BYTE_INDEXED, (IndexColorModel)image.getColorModel());
        Graphics g = newImage.getGraphics();
        g.drawImage(image, 0, 0, image.getWidth()*currentDisplaySize, image.getHeight()*currentDisplaySize, null);
        g.dispose();
        return newImage;
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }

    public Tileset[] getTilesets() {
        return tilesets;
    }

    public void setTilesets(Tileset[] tilesets) {
        this.tilesets = tilesets;
    }
    
    public int getSelectedTileset() {
        return selectedTileset;
    }

    public void setSelectedTileset(int selectedTileset) {
        this.selectedTileset = selectedTileset;
        this.redraw = true;
    }
    
    public int getTilesPerRow() {
        return tilesPerRow;
    }

    public void setTilesPerRow(int tilesPerRow) {
        this.tilesPerRow = tilesPerRow;
        this.redraw = true;
    }

    public int getCurrentDisplaySize() {
        return currentDisplaySize;
    }

    public void setCurrentDisplaySize(int currentDisplaySize) {
        this.currentDisplaySize = currentDisplaySize;
        this.redraw = true;
    }

    public boolean getDrawGrid() {
        return drawGrid;
    }

    public void setDrawGrid(boolean drawGrid) {
        this.drawGrid = drawGrid;
        this.redraw = true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX() / (currentDisplaySize*8);
        int y = e.getY() / (currentDisplaySize*8);
        int tileIndex = y*(tilesPerRow)+x;
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (leftSlotTilePanel != null) {
                leftSlotTilePanel.setTile(tilesets[selectedTileset].getTiles()[tileIndex]);
                leftSlotTilePanel.revalidate();
                leftSlotTilePanel.repaint();
            }
        } else if(e.getButton() == MouseEvent.BUTTON3){
            if (rightSlotTilePanel != null) {
                rightSlotTilePanel.setTile(tilesets[selectedTileset].getTiles()[tileIndex]);
                rightSlotTilePanel.revalidate();
                rightSlotTilePanel.repaint();
            }
        }
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

    public EditableBlockSlotPanel getBlockSlotPanel() {
        return blockSlotPanel;
    }

    public void setBlockSlotPanel(EditableBlockSlotPanel blockSlotPanel) {
        this.blockSlotPanel = blockSlotPanel;
    }

    public TileSlotPanel getLeftSlotBlockPanel() {
        return leftSlotTilePanel;
    }

    public void setLeftSlotTilePanel(TileSlotPanel leftSlotTilePanel) {
        this.leftSlotTilePanel = leftSlotTilePanel;
    }

    public TileSlotPanel getRightSlotBlockPanel() {
        return rightSlotTilePanel;
    }

    public void setRightSlotBlockPanel(TileSlotPanel rightSlotTilePanel) {
        this.rightSlotTilePanel = rightSlotTilePanel;
    }
}
