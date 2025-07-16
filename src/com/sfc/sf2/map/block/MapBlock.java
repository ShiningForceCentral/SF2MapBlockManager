/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block;

import com.sfc.sf2.graphics.Tile;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;

/**
 *
 * @author wiz
 */
public class MapBlock {
    
    public static final int TILE_WIDTH = 3;
    public static final int TILE_HEIGHT = 3;
    public static final int PIXEL_WIDTH = TILE_WIDTH*Tile.PIXEL_WIDTH;
    public static final int PIXEL_HEIGHT = TILE_HEIGHT*Tile.PIXEL_HEIGHT;
    
    private int index;
    
    private int flags;
    
    private Tile[] tiles;
    
    private BufferedImage image;
    private BufferedImage explorationFlagImage;
    private BufferedImage interactionFlagImage;
    private Color[] palette;
    private IndexColorModel icm;
    private int[][] pixels = new int[PIXEL_HEIGHT][PIXEL_WIDTH];
    private BufferedImage indexedColorImage = null;
    
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }  

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
    }

    public void setPalette(Color[] palette) {
        this.palette = palette;
        //generateIcm();
    }

    public IndexColorModel getIcm() {
        return icm;
    }

    public void setIcm(IndexColorModel icm) {
        this.icm = icm;
    }

    public int[][] getPixels() {
        return pixels;
    }

    public void setPixels(int[][] pixels) {
        this.pixels = pixels;
    }

    public void generateIcm(){
        byte[] reds = new byte[16];
        byte[] greens = new byte[16];
        byte[] blues = new byte[16];
        byte[] alphas = new byte[16];
        for(int i=0;i<16;i++){
            reds[i] = (byte)this.palette[i].getRed();
            greens[i] = (byte)this.palette[i].getGreen();
            blues[i] = (byte)this.palette[i].getBlue();
            alphas[i] = (byte)0xFF;
        }
        alphas[0] = 0;
        icm = new IndexColorModel(4,16,reds,greens,blues,alphas);       
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
    
    public void updatePixels(){
        for(int i=0;i<TILE_WIDTH;i++){
            for(int j=0;j<TILE_HEIGHT;j++){
                updateIndexedColorPixels(tiles[i*TILE_WIDTH+j].getPixels(), j*Tile.PIXEL_WIDTH, i*Tile.PIXEL_HEIGHT);
            }
        }
    }
    
    public void updateIndexedColorPixels(int[][] pixels, int x, int y){
        for(int i=0;i<pixels.length;i++){
            for(int j=0;j<pixels[i].length;j++){
                this.pixels[y+j][x+i] = pixels[i][j];
                //data[(y+j)*width+x+i] = (byte)(pixels[i][j]);
            }
        }
    }

    public BufferedImage getIndexedColorImage(){
        if(indexedColorImage==null){
            indexedColorImage = new BufferedImage(PIXEL_WIDTH, PIXEL_HEIGHT, BufferedImage.TYPE_BYTE_INDEXED, icm);
            byte[] data = ((DataBufferByte)(indexedColorImage.getRaster().getDataBuffer())).getData();
            int width = indexedColorImage.getWidth();
            for(int i=0;i<pixels.length;i++){
                for(int j=0;j<pixels[i].length;j++){
                    data[i*width+j] = (byte)(pixels[i][j]);
                }
            }
        }
        return indexedColorImage;        
    }  

    public void setIndexedColorImage(BufferedImage indexedColorImage) {
        this.indexedColorImage = indexedColorImage;
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
    
    public boolean equalsIgnoreTiles(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof MapBlock))return false;
        MapBlock block = (MapBlock)other;
        if(this.index == block.getIndex() && this.flags == block.getFlags()){
            return true;
        }else{
            return false;
        }
    }
    
    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj==null || obj.getClass() != this.getClass()){
            return false;
        }
        MapBlock mb = (MapBlock) obj;
        for(int i=0;i<this.tiles.length;i++){
            if(!this.tiles[i].equalsWithPriority(mb.getTiles()[i])){
                return false;
            }
        }
        return true;
    }
    
    @Override 
    public MapBlock clone(){
        MapBlock clone = new MapBlock();
        clone.setIndex(this.index);
        clone.setFlags(this.flags);
        clone.setTiles(this.tiles.clone());
        clone.setIcm(this.icm);
        clone.setImage(this.image);
        clone.setExplorationFlagImage(this.explorationFlagImage);
        clone.setInteractionFlagImage(this.interactionFlagImage);
        return clone;
    }

    public BufferedImage getExplorationFlagImage() {
        return explorationFlagImage;
    }

    public void setExplorationFlagImage(BufferedImage explorationFlagImage) {
        this.explorationFlagImage = explorationFlagImage;
    }

    public BufferedImage getInteractionFlagImage() {
        return interactionFlagImage;
    }

    public void setInteractionFlagImage(BufferedImage interactionFlagImage) {
        this.interactionFlagImage = interactionFlagImage;
    }

    
    
    
    
}
