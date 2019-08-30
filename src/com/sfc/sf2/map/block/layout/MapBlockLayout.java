/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.layout;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.map.block.MapBlock;
import com.sfc.sf2.map.block.gui.BlockSlotPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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
    
    private static final int DEFAULT_TILES_PER_ROW = 3;
    
    private int tilesPerRow = DEFAULT_TILES_PER_ROW;
    private MapBlock[] blocks;
    private int currentDisplaySize = 1;

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
            currentImage = buildImage(this.blocks,this.tilesPerRow, false);
            setSize(currentImage.getWidth(), currentImage.getHeight());
        }
        return currentImage;
    }
    
    public BufferedImage buildImage(MapBlock[] blocks, int tilesPerRow, boolean pngExport){ 
        renderCounter++;
        System.out.println("Blockset render "+renderCounter);      
        this.blocks = blocks;
        if(pngExport){
            redraw = true;
        }
        if(redraw){
            int blocksPerRow = tilesPerRow / 3;
            int blockHeight = blocks.length/blocksPerRow + ((blocks.length%blocksPerRow!=0)?1:0);
            int imageHeight = blockHeight*3*8;
            Color[] palette = blocks[0].getTiles()[0].getPalette();
            //palette[0] = new Color(255, 255, 255, 0);
            IndexColorModel icm = buildIndexColorModel(palette);
            currentImage = new BufferedImage(tilesPerRow*8, imageHeight , BufferedImage.TYPE_BYTE_INDEXED, icm);
            Graphics graphics = currentImage.getGraphics(); 
            for(int i=0;i<blocks.length;i++){
                int baseX = i%blocksPerRow;
                int baseY = i/blocksPerRow;
                MapBlock block = blocks[i];
                BufferedImage blockImage = block.getImage();
                if(blockImage==null){
                    blockImage = new BufferedImage(3*8, 3*8 , BufferedImage.TYPE_BYTE_INDEXED, icm);
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
                }
                graphics.drawImage(blockImage, baseX*3*8, baseY*3*8, null);
            }
            if(!pngExport){
                currentImage = resize(currentImage);
                redraw = false;
            }
        }          
        return currentImage;
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
    
    private BufferedImage resize(BufferedImage image){
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

    public MapBlock[] getBlocks() {
        return blocks;
    }

    public void setBlocks(MapBlock[] blocks) {
        this.blocks = blocks;
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

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX() / (currentDisplaySize * 3*8);
        int y = e.getY() / (currentDisplaySize * 3*8);
        int blockIndex = y*(tilesPerRow/3) + x;
        if(e.getButton()==MouseEvent.BUTTON1){
            MapBlockLayout.selectedBlockIndex0 = blockIndex;
            if(leftSlotBlockPanel!=null){
                leftSlotBlockPanel.setBlockImage(blocks[blockIndex].getImage());
                leftSlotBlockPanel.revalidate();
                leftSlotBlockPanel.repaint();
            }
        }else if(e.getButton()==MouseEvent.BUTTON3){
            MapBlockLayout.selectedBlockIndex1 = blockIndex;
            if(rightSlotBlockPanel!=null){
                rightSlotBlockPanel.setBlockImage(blocks[blockIndex].getImage());
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
