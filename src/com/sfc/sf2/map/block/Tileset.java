/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block;

import com.sfc.sf2.graphics.Tile;

/**
 *
 * @author TiMMy
 */
public class Tileset {
    String name;
    Tile[] tiles;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Tile[] getTiles() {
        return tiles;
    }
    
    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
    }
}
