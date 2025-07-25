/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block;

import com.sfc.sf2.graphics.GraphicsManager;
import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.map.block.io.DisassemblyManager;
import com.sfc.sf2.map.block.io.PngManager;
import com.sfc.sf2.palette.PaletteManager;

/**
 *
 * @author wiz
 */
public class MapBlockManager {
       
    private PaletteManager paletteManager = new PaletteManager();
    private GraphicsManager graphicsManager = new GraphicsManager();
    private DisassemblyManager disassemblyManager = new DisassemblyManager();
    private Tile[] tiles;
    private Tileset[] tilesets;
    private MapBlock[] blocks;
       
    public void importDisassembly(String incbinPath, String paletteEntriesPath, String tilesetEntriesPath, String tilesetsFilePath, String blocksPath) {
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Importing disassembly ...");
        paletteManager.importDisassembly(blocksPath);
        blocks = disassemblyManager.importDisassembly(incbinPath, paletteEntriesPath, tilesetEntriesPath, tilesetsFilePath, blocksPath);
        tiles = disassemblyManager.getTileset();
        tilesets = disassemblyManager.getTilesets();
        //graphicsManager.setTiles(tiles);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Disassembly imported.");
    }
       
    public void importDisassembly(String palettePath, String[] tilesetPaths, String blocksPath){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Importing disassembly ...");
        blocks = disassemblyManager.importDisassembly(palettePath, tilesetPaths, blocksPath);
        tiles = disassemblyManager.getTileset();
        tilesets = disassemblyManager.getTilesets();
        //graphicsManager.setTiles(tiles);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Disassembly imported.");
    }
       
    public void importDisassembly(String palettePath, String[] tilesetPaths, String blocksPath, String animTilesetPath, int animTilesetStart, int animTilesetLength, int animTilesetDest){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Importing disassembly ...");
        blocks = disassemblyManager.importDisassembly(palettePath, tilesetPaths, blocksPath, animTilesetPath, animTilesetStart, animTilesetLength, animTilesetDest);
        tiles = disassemblyManager.getTileset();
        tilesets = disassemblyManager.getTilesets();
        //graphicsManager.setTiles(tiles);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Disassembly imported.");
    }
    
    public void exportDisassembly(String graphicsPath){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Exporting disassembly ...");
        disassemblyManager.setTileset(tiles);
        disassemblyManager.exportDisassembly(blocks, graphicsPath);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Disassembly exported.");        
    }   
    
    public void importRom(String romFilePath, String paletteOffset, String paletteLength, String graphicsOffset, String graphicsLength){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importOriginalRom() - Importing original ROM ...");
        graphicsManager.importRom(romFilePath, paletteOffset, paletteLength, graphicsOffset, graphicsLength,GraphicsManager.COMPRESSION_BASIC);
        tiles = graphicsManager.getTiles();
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importOriginalRom() - Original ROM imported.");
    }
    
    public void exportRom(String originalRomFilePath, String graphicsOffset){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportOriginalRom() - Exporting original ROM ...");
        graphicsManager.exportRom(originalRomFilePath, graphicsOffset, GraphicsManager.COMPRESSION_BASIC);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportOriginalRom() - Original ROM exported.");        
    }      
    
    public void exportPng(String filepath, int blocksPerRow){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportPng() - Exporting PNG ...");
        PngManager.exportPng(blocks, filepath, blocksPerRow);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportPng() - PNG exported.");       
    }

    public MapBlock[] getBlocks() {
        return blocks;
    }

    public void setBlocks(MapBlock[] blocks) {
        this.blocks = blocks;
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
    }

    public Tileset[] getTilesets() {
        return tilesets;
    }

    public void setTilesets(Tileset[] tilesets) {
        this.tilesets = tilesets;
    }
}
