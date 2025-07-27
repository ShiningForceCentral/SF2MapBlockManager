/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block;

import com.sfc.sf2.graphics.GraphicsManager;
import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.map.block.io.DisassemblyManager;
import com.sfc.sf2.map.block.io.MetaManager;
import com.sfc.sf2.map.block.io.RawImageManager;
import com.sfc.sf2.palette.PaletteManager;

/**
 *
 * @author wiz
 */
public class MapBlockManager {
       
    private final PaletteManager paletteManager = new PaletteManager();
    private final GraphicsManager graphicsManager = new GraphicsManager();
    private final DisassemblyManager disassemblyManager = new DisassemblyManager();
    private Tileset[] tilesets;
    private MapBlock[] blocks;
       
    public void importDisassembly(String incbinPath, String paletteEntriesPath, String tilesetEntriesPath, String tilesetsFilePath, String blocksPath) {
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Importing disassembly ...");
        paletteManager.importDisassembly(blocksPath);
        blocks = disassemblyManager.importDisassembly(incbinPath, paletteEntriesPath, tilesetEntriesPath, tilesetsFilePath, blocksPath);
        tilesets = disassemblyManager.getTilesets();
        //graphicsManager.setTiles(tiles);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Disassembly imported.");
    }
       
    public void importDisassembly(String palettePath, String[] tilesetPaths, String blocksPath){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Importing disassembly ...");
        blocks = disassemblyManager.importDisassembly(palettePath, tilesetPaths, blocksPath);
        tilesets = disassemblyManager.getTilesets();
        //graphicsManager.setTiles(tiles);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Disassembly imported.");
    }
       
    public void importDisassembly(String palettePath, String[] tilesetPaths, String blocksPath, String animTilesetPath, int animTilesetStart, int animTilesetLength, int animTilesetDest){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Importing disassembly ...");
        blocks = disassemblyManager.importDisassembly(palettePath, tilesetPaths, blocksPath, animTilesetPath, animTilesetStart, animTilesetLength, animTilesetDest);
        tilesets = disassemblyManager.getTilesets();
        //graphicsManager.setTiles(tiles);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Disassembly imported.");
    }
    
    public void exportDisassembly(String graphicsPath){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Exporting disassembly ...");
        disassemblyManager.exportDisassembly(blocks, graphicsPath);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importDisassembly() - Disassembly exported.");        
    }   
    
    public void importRom(String romFilePath, String paletteOffset, String paletteLength, String graphicsOffset, String graphicsLength){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importOriginalRom() - Importing original ROM ...");
        graphicsManager.importRom(romFilePath, paletteOffset, paletteLength, graphicsOffset, graphicsLength,GraphicsManager.COMPRESSION_BASIC);
        Tile[] tiles = graphicsManager.getTiles();
        tilesets = new Tileset[5];
        for (int i = 0; i < tilesets.length; i++) {
            if (tiles.length >= (i+1)*Tileset.TILESET_TILES) {
                //Tiles array is long enough so copy over all tiles
                Tile[] ts = new Tile[Tileset.TILESET_TILES];
                System.arraycopy(tiles, i*Tileset.TILESET_TILES, ts, 0, Tileset.TILESET_TILES);
                Tileset tileset = new Tileset();
                tilesets[i] = tileset;
            } else if (tiles.length <= i*Tileset.TILESET_TILES) {
                //Tiles arry is not long enough to staart. Make empty tiletset
                tilesets[i] = Tileset.EmptyTilset(tiles[0].getPalette());
            } else {
                //Tiles array is partial size so copy then build remainder
                Tile[] ts = new Tile[Tileset.TILESET_TILES];
                System.arraycopy(tiles, i*Tileset.TILESET_TILES, ts, 0, tiles.length%Tileset.TILESET_TILES);
                Tile emptyTile = Tile.EmptyTile(tiles[0].getPalette());
                for (int j = tiles.length%Tileset.TILESET_TILES; j < Tileset.TILESET_TILES; j++) {
                    ts[j] = emptyTile;
                }
                Tileset tileset = new Tileset();
                tilesets[i] = tileset;
            }
            tilesets[i].setName(String.format("%00d", i));
        }
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.importOriginalRom() - Original ROM imported.");
    }
    
    public void exportRom(String originalRomFilePath, String graphicsOffset){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportOriginalRom() - Exporting original ROM ...");
        graphicsManager.exportRom(originalRomFilePath, graphicsOffset, GraphicsManager.COMPRESSION_BASIC);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportOriginalRom() - Original ROM exported.");        
    }
    
    public void importPng(String filepath, String hpFilePath){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportGif() - Exporting GIF ...");
        blocks = RawImageManager.importImage(filepath);
        MetaManager.importBlockHpTilesFile(hpFilePath, blocks, RawImageManager.getImportedImageBlockWidth());
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportGif() - GIF exported.");       
    }
    
    public void importGif(String filepath, String hpFilePath){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportGif() - Exporting GIF ...");
        blocks = RawImageManager.importImage(filepath);
        MetaManager.importBlockHpTilesFile(hpFilePath, blocks, RawImageManager.getImportedImageBlockWidth());
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportGif() - GIF exported.");       
    }
    
    public void exportPng(String filepath, String hpFilePath, int blocksPerRow){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportPng() - Exporting PNG ...");
        RawImageManager.exportRawImage(blocks, filepath, blocksPerRow, com.sfc.sf2.graphics.io.RawImageManager.FILE_FORMAT_PNG);
        MetaManager.exportBlockHpTilesFile(blocks, blocksPerRow, hpFilePath);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportPng() - PNG exported.");       
    }
    
    public void exportGif(String filepath, String hpFilePath, int blocksPerRow){
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportGif() - Exporting GIF ...");
        RawImageManager.exportRawImage(blocks, filepath, blocksPerRow, com.sfc.sf2.graphics.io.RawImageManager.FILE_FORMAT_GIF);
        MetaManager.exportBlockHpTilesFile(blocks, blocksPerRow, hpFilePath);
        System.out.println("com.sfc.sf2.mapblock.MapBlockManager.exportGif() - GIF exported.");       
    }

    public MapBlock[] getBlocks() {
        return blocks;
    }

    public void setBlocks(MapBlock[] blocks) {
        this.blocks = blocks;
    }

    public Tileset[] getTilesets() {
        return tilesets;
    }

    public void setTilesets(Tileset[] tilesets) {
        this.tilesets = tilesets;
    }
}
