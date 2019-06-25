/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block;

import com.sfc.sf2.graphics.Tile;
import java.awt.image.BufferedImage;

/**
 *
 * @author wiz
 */
public class MapBlock {
    
    private int index;
    
    private int flags;
    
    private Tile[] tiles;
    
    private BufferedImage image;
    private BufferedImage explorationFlagImage;
    private BufferedImage interactionFlagImage;
    
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

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
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
