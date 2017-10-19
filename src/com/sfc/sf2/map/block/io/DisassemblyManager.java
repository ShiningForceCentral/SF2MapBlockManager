/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.io;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.graphics.compressed.BasicGraphicsDecoder;
import com.sfc.sf2.graphics.compressed.BasicGraphicsEncoder;
import com.sfc.sf2.map.block.MapBlock;
import com.sfc.sf2.graphics.compressed.StackGraphicsDecoder;
import com.sfc.sf2.graphics.compressed.StackGraphicsEncoder;
import com.sfc.sf2.palette.graphics.PaletteDecoder;
import com.sfc.sf2.palette.graphics.PaletteEncoder;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiz
 */
public class DisassemblyManager {

    private static final int MAPBLOCK_TILES_LENGTH = 128*5;
    
    private byte[] inputData;
    private short inputWord = 0;
    private int inputCursor = -2;
    private int inputBitCursor = 16;
    private List<Short> outputData = null;
    private Tile[] outputTiles = null;
    
    Color[] palette = null;
    Tile[] tiles = new Tile[128*5];
    private final Short[] rightTileHistory = new Short[0x1000];
    private final Short[] bottomTileHistory = new Short[0x1000];
    
    public MapBlock[] importDisassembly(String palettePath, String tileset1Path, String tileset2Path, String tileset3Path, String tileset4Path, String tileset5Path, String blocksPath){
        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassembly() - Importing disassembly ...");
        MapBlock[] blocks = null;

        try{
            Path palettepath = Paths.get(palettePath);
            Path tileset1path = Paths.get(tileset1Path);
            Path tileset2path = Paths.get(tileset2Path);
            Path tileset3path = Paths.get(tileset3Path);
            Path tileset4path = Paths.get(tileset4Path);
            Path tileset5path = Paths.get(tileset5Path);
            Path blockspath = Paths.get(blocksPath);
            if(palettepath.toFile().exists()){
                byte[] paletteData = Files.readAllBytes(palettepath);
                palette = PaletteDecoder.parsePalette(paletteData);
                palette[0] = new Color(255, 255, 255, 0);
                Tile emptyTile = new Tile();
                emptyTile.setPalette(palette);
                emptyTile.setPixels(new int[8][8]);
                Tile[] emptyTileset = new Tile[128];
                for(int i=0;i<emptyTileset.length;i++){
                    emptyTileset[i] = emptyTile;
                }
                if(tileset1path.toFile().exists()){
                    byte[] tileset1Data = Files.readAllBytes(tileset1path);
                    if(tileset1Data.length>2){
                        Tile[] tileset = new StackGraphicsDecoder().decodeStackGraphics(tileset1Data, palette);
                        System.arraycopy(tileset, 0, tiles, 0*128, tileset.length);
                    }else{
                        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - File ignored because of wrong length " + tileset1Data.length + " : " + tileset1Path);
                    }
                }else{
                    System.arraycopy(emptyTileset, 0, tiles, 0*128, emptyTileset.length);
                }
                if(tileset2path.toFile().exists()){
                    byte[] tileset2Data = Files.readAllBytes(tileset2path);
                    if(tileset2Data.length>2){
                        Tile[] tileset = new StackGraphicsDecoder().decodeStackGraphics(tileset2Data, palette);
                        System.arraycopy(tileset, 0, tiles, 1*128, tileset.length);
                    }else{
                        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - File ignored because of wrong length " + tileset2Data.length + " : " + tileset2Path);
                    }
                }else{
                    System.arraycopy(emptyTileset, 0, tiles, 1*128, emptyTileset.length);
                }
                if(tileset3path.toFile().exists()){
                    byte[] tileset3Data = Files.readAllBytes(tileset3path);
                    if(tileset3Data.length>2){
                        Tile[] tileset = new StackGraphicsDecoder().decodeStackGraphics(tileset3Data, palette);
                        System.arraycopy(tileset, 0, tiles, 2*128, tileset.length);
                    }else{
                        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - File ignored because of wrong length " + tileset3Data.length + " : " + tileset3Path);
                    }
                }else{
                    System.arraycopy(emptyTileset, 0, tiles, 2*128, emptyTileset.length);
                }
                if(tileset4path.toFile().exists()){
                    byte[] tileset4Data = Files.readAllBytes(tileset4path);
                    if(tileset4Data.length>2){
                        Tile[] tileset = new StackGraphicsDecoder().decodeStackGraphics(tileset4Data, palette);
                        System.arraycopy(tileset, 0, tiles, 3*128, tileset.length);
                    }else{
                        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - File ignored because of wrong length " + tileset4Data.length + " : " + tileset4Path);
                    }
                }else{
                    System.arraycopy(emptyTileset, 0, tiles, 3*128, emptyTileset.length);
                }
                if(tileset5path.toFile().exists()){
                    byte[] tileset5Data = Files.readAllBytes(tileset5path);
                    if(tileset5Data.length>2){
                        Tile[] tileset = new StackGraphicsDecoder().decodeStackGraphics(tileset5Data, palette);
                        System.arraycopy(tileset, 0, tiles, 4*128, tileset.length);
                    }else{
                        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - File ignored because of wrong length " + tileset5Data.length + " : " + tileset5Path);
                    }
                }else{
                    System.arraycopy(emptyTileset, 0, tiles, 4*128, emptyTileset.length);
                }
                if(blockspath.toFile().exists()){
                     inputData = Files.readAllBytes(blockspath);
                }
                
            }            

            if(tiles!=null && inputData!=null){
                if(tiles.length==MAPBLOCK_TILES_LENGTH){
                   blocks = parseBlockData();
                   System.out.println("Created MapBlocks with " + tiles.length + " tiles.");                       
                }else{
                    System.out.println("Could not create MapBlocks because of wrong length : tiles=" + tiles.length);
                }
            }
        }catch(Exception e){
             System.err.println("com.sfc.sf2.mapblock.io.PngManager.importPng() - Error while parsing graphics data : "+e);
             e.printStackTrace();
        }         
                
        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassembly() - Disassembly imported.");
        return blocks;
    }
    
    private MapBlock[] parseBlockData(){
        MapBlock[] blocks = null;
        try{
            
            int initialCommandNumber = getCommandNumber();
            int remainingCommandNumber = initialCommandNumber;
            outputData = new ArrayList(initialCommandNumber);
            outputInitialBlocks();
            while(remainingCommandNumber>0){
                if(getNextBit()==0){
                    if(getNextBit()==0){
                        /* 00 */
                        //System.out.println("commandNumber=$" + Integer.toHexString(initialCommandNumber-remainingCommandNumber)+" - repeatLastOutputTile");
                        repeatLastOutputTile();
                        //System.out.println("outputData="+outputData);
                    }else{
                        /* 01 */
                        //System.out.println("commandNumber=$" + Integer.toHexString(initialCommandNumber-remainingCommandNumber)+" - outputNextTileFromTileset");
                        outputNextTileFromTileset();
                        //System.out.println("outputData="+outputData);
                    }
                }else{
                    if(getNextBit()==0){
                        if(getNextBit()==0){
                            /* 100 */
                            //System.out.println("commandNumber=$" + Integer.toHexString(initialCommandNumber-remainingCommandNumber)+" - outputRightTileFromHistory");
                            outputRightTileFromHistory();
                            //System.out.println("outputData="+outputData);
                        }else{
                            /* 101 */
                            //System.out.println("commandNumber=$" + Integer.toHexString(initialCommandNumber-remainingCommandNumber)+" - outputBottomTileFromHistory");
                            outputBottomTileFromHistory();
                            //System.out.println("outputData="+outputData);
                        }
                    }else{
                        if(getNextBit()==0){
                            /* 110 */
                            //System.out.println("commandNumber=$" + Integer.toHexString(initialCommandNumber-remainingCommandNumber)+" - outputNextTileWithSameFlags");
                            outputNextTileWithSameFlags();
                            //System.out.println("outputData="+outputData);
                        }else{
                            /* 111 */
                            //System.out.println("commandNumber=$" + Integer.toHexString(initialCommandNumber-remainingCommandNumber)+" - outputNextTileWithNewFlags");
                            outputNextTileWithNewFlags();
                            //System.out.println("outputData="+outputData);
                        }
                    }
                }
                remainingCommandNumber--;
            }   
                      
        }catch(Exception e){
             System.err.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - Error while parsing block data : "+e);
             e.printStackTrace();
        } 
        blocks = produceBlocks(); 
        return blocks;
    }   
    
    private void outputInitialBlocks(){
        Tile emptyTile = new Tile();
        emptyTile.setPalette(palette);
        outputData.add((short)0);
        outputData.add((short)0);
        outputData.add((short)0);
        outputData.add((short)0);
        outputData.add((short)0);
        outputData.add((short)0);
        outputData.add((short)0);
        outputData.add((short)0);
        outputData.add((short)0);
        
        outputData.add((short)0x22E);
        outputData.add((short)0x22F);
        outputData.add((short)0xA2E);
        outputData.add((short)0x23E);
        outputData.add((short)0x23F);
        outputData.add((short)0xA3E);
        outputData.add((short)0x24E);
        outputData.add((short)0x24F);
        outputData.add((short)0xA4E);
        
        outputData.add((short)0x22C);
        outputData.add((short)0x22D);
        outputData.add((short)0xA2C);
        outputData.add((short)0x23C);
        outputData.add((short)0x23D);
        outputData.add((short)0xA3C);
        outputData.add((short)0x24E);
        outputData.add((short)0x24F);
        outputData.add((short)0xA4E);
                
    }
    
    private int getCommandNumber(){
        int commandNumber = (getNextWord(inputData, 0) >> 2) & 0x3FFF;
        for(int i=0;i<14;i++){
            getNextBit();
        }
        return commandNumber;
    }
    
    private void repeatLastOutputTile(){
        outputData.add(outputData.get(outputData.size()-1));
    }
    
    private void outputNextTileFromTileset(){
        short previousTile = outputData.get(outputData.size()-1);
        if((previousTile & 0x0800) == 0){
            outputData.add((short)(outputData.get(outputData.size()-1)+1));
        }else{
            outputData.add((short)(outputData.get(outputData.size()-1)-1));
        }
    }
    
    private void outputRightTileFromHistory(){
        short leftTile = outputData.get(outputData.size()-1);
        int rightTileHistoryOffset = (leftTile & 0x3FF) * 2 + (leftTile & 0x0800);
        Short val = rightTileHistory[rightTileHistoryOffset];
        if(val!=null){
            outputData.add(val);
        }else{
            outputData.add((short)0);
        }
    }
    
    private void outputBottomTileFromHistory(){
        short upperTile = outputData.get(outputData.size()-3);
        int bottomTileHistoryOffset = (upperTile & 0x3FF) * 2 + (upperTile & 0x0800);
        outputData.add(bottomTileHistory[bottomTileHistoryOffset]);
    }    
    
    private void outputNextTileWithSameFlags(){
        Short previousTile = outputData.get(outputData.size()-1);
        boolean highPriority = ((previousTile&0x8000)!=0);
        boolean vFlip = ((previousTile&0x1000)!=0);
        boolean hFlip = ((previousTile&0x0800)!=0);
        outputNextTile(highPriority, vFlip, hFlip);
    }
    
    private void outputNextTileWithNewFlags(){
        boolean highPriority = (getNextBit()!=0);
        boolean vFlip = (getNextBit()!=0);
        boolean hFlip = (getNextBit()!=0);
        outputNextTile(highPriority, vFlip, hFlip);
    }
    
    private void outputNextTile(boolean highPriority, boolean vFlip, boolean hFlip){
        short tileValue;
        if(getNextBit()==0){
            tileValue = readRelativeTileValue();
        }else{
            tileValue = readAbsoluteTileValue();
        }
        if(highPriority){
            tileValue = (short)(0x8000 | tileValue);
        }
        if(vFlip){
            tileValue = (short)(0x1000 | tileValue);
        }
        if(hFlip){
            tileValue = (short)(0x0800 | tileValue);
        }
        short leftTile = outputData.get(outputData.size()-1);
        short upperTile = outputData.get(outputData.size()-3);
        int rightTileHistoryOffset = (leftTile & 0x3FF) * 2 + (leftTile & 0x0800);
        int bottomTileHistoryOffset = (upperTile & 0x3FF) * 2 + (upperTile & 0x0800);
        rightTileHistory[rightTileHistoryOffset] = tileValue;
        bottomTileHistory[bottomTileHistoryOffset] = tileValue;
        outputData.add(tileValue);        
    }
    
    private short readRelativeTileValue(){
        Short previousTile = outputData.get(outputData.size()-1);
        short relativeValue = (short)(getNextBit() * 0x0010
                    + getNextBit() * 0x0008
                    + getNextBit() * 0x0004
                    + getNextBit() * 0x0002
                    + getNextBit() * 0x0001);
        if(getNextBit()==1){
            relativeValue = (short)(relativeValue * -1);
        }
        return (short)((previousTile&0x3FF) + relativeValue);
    }
    
    private short readAbsoluteTileValue(){
        short value = (short)(getNextBit() * 0x0100
                    + getNextBit() * 0x0080
                    + getNextBit() * 0x0040
                    + getNextBit() * 0x0020
                    + getNextBit() * 0x0010
                    + getNextBit() * 0x0008
                    + getNextBit() * 0x0004
                    + getNextBit() * 0x0002
                    + getNextBit() * 0x0001);
        if(value>0x180){
            value = (short)((value * 2) + getNextBit() - 0x180);
        }
        return value;
    }
    
    private MapBlock[] produceBlocks(){
        outputTiles = new Tile[outputData.size()];
        for(int i=0;i<outputData.size();i++){
            short value = outputData.get(i);
            Tile tile = tiles[value&0x3FF];
            if((value&0x8000)!=0){
                tile.setHighPriority(true);
            }
            if((value&0x1000)!=0){
                tile = Tile.vFlip(tile);
            }
            if((value&0x0800)!=0){
                tile = Tile.hFlip(tile);
            }
            outputTiles[i] = tile;
        }
        MapBlock[] blocks = new MapBlock[outputTiles.length/9];
        for(int i=0;i<blocks.length;i++){
            MapBlock block = new MapBlock();
            block.setIndex(i+3);
            block.setTiles(Arrays.copyOfRange(outputTiles,i*9, i*9+9));
            blocks[i] = block;
        }
        return blocks;
    }
    
    private static short getNextWord(byte[] data, int cursor){
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(data[cursor+1]);
        bb.put(data[cursor]);
        short s = bb.getShort(0);
        return s;
    }    
    
    private int getNextBit(){
        int bit = 0;
        if(inputBitCursor>=16){
            inputBitCursor = 0;
            inputCursor+=2;
            inputWord = getNextWord(inputData, inputCursor);
        } 
        bit = (inputWord>>(15-inputBitCursor)) & 1;
        inputBitCursor++;
        return bit;
    }

    public static void exportDisassembly(MapBlock[] blocks, String graphicsPath){
        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.exportDisassembly() - Exporting disassembly ...");
        try {
            /* Tile[] tileset = mapblock.getTiles();
            StackGraphicsEncoder.produceGraphics(tileset);
            byte[] newMapBlockFileBytes = StackGraphicsEncoder.getNewGraphicsFileBytes();
            Path graphicsFilePath = Paths.get(graphicsPath);
            Files.write(graphicsFilePath,newMapBlockFileBytes);
            System.out.println(newMapBlockFileBytes.length + " bytes into " + graphicsFilePath);                 */
        } catch (Exception ex) {
            Logger.getLogger(DisassemblyManager.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            System.out.println(ex);
        }            
        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.exportDisassembly() - Disassembly exported.");        
    }      
    
}
