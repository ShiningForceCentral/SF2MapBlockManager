/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.palette.Palette;

/**
 *
 * @author TiMMy
 */
public class Tileset {
    public static final int TILESET_TILES = 128;
    public static final int MAP_TILESETS_TILES = TILESET_TILES*5;
    
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
    
    public Tileset clone() {
        Tileset newTileset = new Tileset();
        newTileset.setName(this.name);
        newTileset.setTiles(this.tiles.clone());
        return newTileset;
    }
    
    public boolean isTilesetEmpty() {
        if (tiles == null || tiles.length == 0) {
            return true;
        }
        for (int i = 0; i < tiles.length; i++) {
            if (!tiles[i].isTileEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public static Tileset EmptyTilset(Palette palette) {
        Tile emptyTile = Tile.EmptyTile(palette);
        Tile[] tiles = new Tile[128];
        for(int i=0;i<tiles.length;i++) {
            tiles[i] = emptyTile;
        }
        Tileset emptyTileset = new Tileset();
        emptyTileset.setName("EMPTY");
        emptyTileset.setTiles(tiles);
        return emptyTileset;
    }
}
