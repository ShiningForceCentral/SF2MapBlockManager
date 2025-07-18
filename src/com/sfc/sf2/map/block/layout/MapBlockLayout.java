/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.layout;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.map.block.MapBlock;
import com.sfc.sf2.map.block.gui.BlockSlotPanel;
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
 * @author wiz
 */
public class MapBlockLayout extends JPanel implements MouseListener, MouseMotionListener {
    
    public static int selectedBlockIndex0;
    public static int selectedBlockIndex1;
    
    private BlockSlotPanel leftSlotBlockPanel;
    private BlockSlotPanel rightSlotBlockPanel;
    
    private static final int DEFAULT_BLOCKS_PER_ROW = 8;
    
    private int blocksPerRow = DEFAULT_BLOCKS_PER_ROW;
    private MapBlock[] blocks;
    private int currentDisplaySize = 1;
    private boolean drawGrid = true;
    private boolean showPriority = false;

    private BufferedImage currentImage;
    private boolean redraw = true;
    private int renderCounter = 0;  
    

    public MapBlockLayout() {
       addMouseListener(this);
       addMouseMotionListener(this);
    }
   
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);   
        g.drawImage(buildImage(), 0, 0, this);       
    }
    
    public BufferedImage buildImage(){
        if(redraw){
            currentImage = buildImage(this.blocks, this.blocksPerRow, false);
            setSize(currentImage.getWidth(), currentImage.getHeight());
        }
        return currentImage;
    }
    
    public BufferedImage buildImage(MapBlock[] blocks, int blocksPerRow, boolean pngExport){ 
        renderCounter++;
        System.out.println("Blockset render "+renderCounter);      
        this.blocks = blocks;
        if(pngExport){
            redraw = true;
        }
        if(redraw){
            int blockHeight = blocks.length/blocksPerRow + ((blocks.length%blocksPerRow!=0)?1:0);
            currentImage = new BufferedImage(blocksPerRow*3*8+1, blockHeight*3*8+1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = (Graphics2D)currentImage.getGraphics(); 
            for(int i=0;i<blocks.length;i++){
                int baseX = (i%blocksPerRow)*3*8;
                int baseY = (i/blocksPerRow)*3*8;
                MapBlock block = blocks[i];
                block.updatePixels();
                BufferedImage blockImage = block.getIndexedColorImage();
                graphics.drawImage(blockImage, baseX, baseY, null);
                
                if (showPriority) {
                    Tile[] tiles = block.getTiles();
                    for (int t = 0; t < tiles.length; t++) {
                        if (tiles[t].isHighPriority()) {
                            graphics.setColor(Color.BLACK);
                            graphics.fillRect(baseX+(t%3)*8+2, baseY+(t/3)*8+2, 4, 4);
                            graphics.setColor(Color.YELLOW);
                            graphics.fillRect(baseX+(t%3)*8+3, baseY+(t/3)*8+3, 2, 2);
                        }
                    }
                }
            }
            if (drawGrid) {
                int width = blocksPerRow+1;
                int height = blocks.length/blocksPerRow+1;
                graphics.setColor(Color.BLACK);
                graphics.setStroke(new BasicStroke(1));
                for (int i = 0; i <= width; i++) {
                    graphics.drawLine(i*3*8, 0, i*3*8, height*3*8);
                }
                for (int j = 0; j <= height; j++) {
                    graphics.drawLine(0, j*3*8, width*3*8, j*3*8);
                }
            }
            graphics.dispose();
            if(!pngExport){
                currentImage = resize(currentImage);
                redraw = false;
            }
        }          
        return currentImage;
    }
    
    private BufferedImage resize(BufferedImage image){
        BufferedImage newImage = new BufferedImage(image.getWidth()*currentDisplaySize, image.getHeight()*currentDisplaySize, BufferedImage.TYPE_INT_ARGB);
        Graphics g = newImage.getGraphics();
        g.drawImage(image, 0, 0, image.getWidth()*currentDisplaySize, image.getHeight()*currentDisplaySize, null);
        g.dispose();
        return newImage;
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }

    public MapBlock[] getBlocks() {
        return blocks;
    }

    public void setBlocks(MapBlock[] blocks) {
        this.blocks = blocks;
        this.redraw = true;
    }
    
    public int getBlocksPerRow() {
        return blocksPerRow;
    }

    public void setBlocksPerRow(int blocksPerRow) {
        this.blocksPerRow = blocksPerRow;
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
    
    public boolean getShowPriority() {
        return showPriority;
    }

    public void setShowPriority(boolean showPriority) {
        this.showPriority = showPriority;
        this.redraw = true;
    }
    
    public void mapBlocksChanged() {
        this.redraw = true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX() / (currentDisplaySize*3*8);
        int y = e.getY() / (currentDisplaySize*3*8);
        int blockIndex = y*blocksPerRow+x;
        if(e.getButton()==MouseEvent.BUTTON1){
            MapBlockLayout.selectedBlockIndex0 = blockIndex;
            if(leftSlotBlockPanel!=null){
                leftSlotBlockPanel.setBlock(blocks[blockIndex]);
                leftSlotBlockPanel.revalidate();
                leftSlotBlockPanel.repaint(); 
            }
        }else if(e.getButton()==MouseEvent.BUTTON3){
            MapBlockLayout.selectedBlockIndex1 = blockIndex;
            if(rightSlotBlockPanel!=null){
                rightSlotBlockPanel.setBlock(blocks[blockIndex]);
                rightSlotBlockPanel.revalidate();
                rightSlotBlockPanel.repaint();
            }
        }
        //System.out.println("Blockset press "+e.getButton()+" "+x+" - "+y);
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

    public BlockSlotPanel getLeftSlotBlockPanel() {
        return leftSlotBlockPanel;
    }

    public void setLeftSlotBlockPanel(BlockSlotPanel leftSlotBlockPanel) {
        this.leftSlotBlockPanel = leftSlotBlockPanel;
    }

    public BlockSlotPanel getRightSlotBlockPanel() {
        return rightSlotBlockPanel;
    }

    public void setRightSlotBlockPanel(BlockSlotPanel rightSlotBlockPanel) {
        this.rightSlotBlockPanel = rightSlotBlockPanel;
    }
}
