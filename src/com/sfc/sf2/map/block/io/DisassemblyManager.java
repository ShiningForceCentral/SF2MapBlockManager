/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.io;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.map.block.MapBlock;
import com.sfc.sf2.graphics.compressed.StackGraphicsDecoder;
import com.sfc.sf2.map.block.Tileset;
import com.sfc.sf2.palette.Palette;
import com.sfc.sf2.palette.graphics.PaletteDecoder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
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
    private StringBuilder debugSb = null;
    
    private Palette palette = null;
    private Tile[] tileset = new Tile[128*5];
    private Tileset[] tilesets = new Tileset[5];
    
    private Short[] rightTileHistory = new Short[0x800];
    private Short[] bottomTileHistory = new Short[0x800];
    
    public MapBlock[] importDisassembly(String palettePath, String[] tilesetPaths, String blocksPath){
        return DisassemblyManager.this.importDisassembly(palettePath, tilesetPaths, blocksPath, null, 0, 0, 0);
    }
    
    public MapBlock[] importDisassembly(String palettePath, String[] tilesetPaths, String blocksPath, String animTilesetPath, int animTilesetStart, int animTilesetLength, int animTilesetDest){
        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassembly() - Importing disassembly ...");
        MapBlock[] blocks = null;
        inputData = null;
        inputCursor = -2;
        inputBitCursor = 16;
        inputWord = 0;

        try{
            palette = com.sfc.sf2.palette.io.DisassemblyManager.importDisassembly(palettePath);
            if (palette == null) {
                throw new IOException("ERROR - Could not load palette : " + palettePath);
            }
            
            Path animtilesetpath = null;
            if(animTilesetPath!=null){
                animtilesetpath = Paths.get(animTilesetPath);
            }
            Path blockspath = Paths.get(blocksPath);
            Tile emptyTile = new Tile();
            emptyTile.setPalette(palette);
            emptyTile.setPixels(new int[8][8]);
            Tile[] emptyTileset = new Tile[128];
            for(int i=0;i<emptyTileset.length;i++){
                emptyTileset[i] = emptyTile;
            }
            for (int i = 0; i < tilesetPaths.length; i++) {
                if (i >= 5) {
                    System.out.println("Too many tilesets defined. Ignoring excess. Count = " + tilesetPaths.length);
                    break;
                }
                Path tilesetPath = Paths.get(tilesetPaths[i]);
                if(tilesetPath.toFile().exists()){
                    byte[] tilesetData = Files.readAllBytes(tilesetPath);
                    if(tilesetData.length>2){
                        Tile[] tileset = new StackGraphicsDecoder().decodeStackGraphics(tilesetData, palette);
                        System.arraycopy(tileset, 0, this.tileset, i*128, tileset.length);
                        Tileset set = new Tileset();
                        set.setName(tilesetPath.getFileName().toString());
                        set.setTiles(tileset);
                        tilesets[i] = set;
                    }else{
                        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - File ignored because of wrong length " + tilesetData.length + " : " + tilesetPath);
                    }
                }else{
                    System.arraycopy(emptyTileset, 0, tileset, i*128, emptyTileset.length);
                }
            }
            if(animtilesetpath!=null && animtilesetpath.toFile().exists()){
                byte[] animTilesetData = Files.readAllBytes(animtilesetpath);
                if(animTilesetData.length>2){
                    Tile[] tileset = new StackGraphicsDecoder().decodeStackGraphics(animTilesetData, palette);
                    int dest = animTilesetDest-256;
                    System.arraycopy(tileset, animTilesetStart, this.tileset, dest, animTilesetLength);
                }else{
                    System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - File ignored because of wrong length " + animTilesetData.length + " : " + animTilesetPath);
                }
            }
            for(int i=0;i<tileset.length;i++){
                if(tileset[i]!=null){
                    tileset[i].setId(i);
                }
            }
            if(blockspath.toFile().exists()){
                 inputData = Files.readAllBytes(blockspath);
            }else{
                System.err.println("ERROR - File not found : "+blocksPath);
            }       

            if(tileset!=null && inputData!=null){
                if(tileset.length==MAPBLOCK_TILES_LENGTH){
                   blocks = parseBlockData();
                   System.out.println("Created MapBlocks with " + tileset.length + " tiles.");                       
                }else{
                    System.out.println("Could not create MapBlocks because of wrong length : tiles=" + tileset.length);
                }
            }
        }catch(Exception e){
             System.err.println("com.sfc.sf2.mapblock.io.PngManager.importPng() - Error while parsing graphics data : "+e);
             e.printStackTrace();
        }         
                
        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassembly() - Disassembly imported.");
        return blocks;
    }

    public MapBlock[] importDisassembly(String incbinPath, String paletteEntriesPath, String tilesetEntriesPath, String tilesetsFilePath, String blocksPath) {
        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassemblyFromEntryFiles() - Importing disassembly ...");
        MapBlock[] mapBlocks = null;
        String palettePath = "";
        String[] tilesetPaths = {"","","","",""};
        
        int[] indexes;
        try {
            indexes = parseTilesetsFile(tilesetsFilePath); 
            int paletteIndex = indexes[0];
            
            List<String> paletteFilenames = loadPaletteEntryFile(paletteEntriesPath);
            if(paletteFilenames.isEmpty()){
                System.err.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassemblyFromEntryFiles() - ERROR : no palette file imported. Wrong path or filename prefix ?");
            } else if(paletteIndex > paletteFilenames.size()){
                System.err.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassemblyFromEntryFiles() - ERROR : Index "+paletteIndex+" id superior to number of palette files found : "+paletteFilenames.size());
            } else {
                palettePath = incbinPath + System.getProperty("file.separator") + paletteFilenames.get(paletteIndex);
                System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassembly() - Selected palette file : "+palettePath);
            }

            List<String> tilesetFilenames = loadTilesetEntryFile(tilesetEntriesPath);
            if(tilesetFilenames.isEmpty()){
                System.err.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassembly() - ERROR : no tileset file imported. Wrong path or filename prefix ?");
            } else {
                for(int i=0;i<tilesetPaths.length;i++){
                    if(indexes[i+1] > tilesetFilenames.size()){
                        System.err.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassembly() - ERROR for tileset "+(i+1)+" : Index "+indexes[i+1]+" id superior to unmber of tileset files found : "+tilesetFilenames.size());
                    } else if(indexes[i+1]!=-1){
                        tilesetPaths[i] = incbinPath + System.getProperty("file.separator") + tilesetFilenames.get(indexes[i+1]);
                        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassembly() - Selected tileset "+(i+1)+" : "+tilesetPaths[i]);
                    } else{
                        System.err.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassembly() - Tileset "+(i+1)+" is declared empty.");
                    }
                }
            }
            
            mapBlocks = DisassemblyManager.this.importDisassembly(palettePath, tilesetPaths, blocksPath);
        } catch(Exception e) {
             System.err.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassemblyFromEntryFiles() - Error while parsing map data : "+e);
             e.printStackTrace();
        }                
        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.importDisassemblyFromEntryFiles() - Disassembly imported.");
        return mapBlocks;
    }
    
    public List<String> loadPaletteEntryFile(String filePath) {
        
        List<String> filepaths = new ArrayList();
        try {
            File entryFile = new File(filePath);
            Scanner scan = new Scanner(entryFile);
            while(scan.hasNext()){
                String line = scan.nextLine();
                if(line.contains("dc.l")){
                    String pointer = line.substring(line.indexOf("dc.l")+5).trim();
                    String filepath = null;
                    Scanner filescan = new Scanner(entryFile);
                    while(filescan.hasNext()){
                        String pathline = filescan.nextLine();
                        if(pathline.startsWith(pointer)){
                            filepath = pathline.substring(pathline.indexOf("\"")+1, pathline.lastIndexOf("\""));
                        }
                    }
                    filepaths.add(filepath);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DisassemblyManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return filepaths;
    }
    
    public List<String> loadTilesetEntryFile(String filePath){
        List<String> filepaths = new ArrayList();
        try {
            File entryFile = new File(filePath);
            Scanner scan = new Scanner(entryFile);
            while(scan.hasNext()){
                String line = scan.nextLine();
                if(line.contains("dc.l")){
                    String pointer = line.substring(line.indexOf("dc.l")+5).trim();
                    String filepath = null;
                    Scanner filescan = new Scanner(entryFile);
                    while(filescan.hasNext()){
                        String pathline = filescan.nextLine();
                        if(pathline.startsWith(pointer)){
                            filepath = pathline.substring(pathline.indexOf("\"")+1, pathline.lastIndexOf("\""));
                        }
                    }
                    filepaths.add(filepath);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DisassemblyManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return filepaths;
    }
    
    
    private int[] parseTilesetsFile(String tilesetsFilePath) {
        int[] indexes = new int[6];
        try {
            if(tilesetsFilePath.endsWith(".asm")){
                Path tilesetspath = Paths.get(tilesetsFilePath);
                File file = tilesetspath.toFile();
//                if(!file.exists()){
//                     System.err.println("ERROR - File not found : "+tilesetsFilePath);
//                }else{                    
                    Scanner scan = new Scanner(file);
                    boolean inHeader = true;
                    while(scan.hasNext()){
                        String line = scan.nextLine();
                        if(line.trim().startsWith("mapPalette")){
                            indexes[0] = Integer.parseInt(line.trim().substring("mapPalette".length()).trim());
                        }else if(line.trim().startsWith("mapTileset1")){
                            indexes[1] = Integer.parseInt(line.trim().substring("mapTileset1".length()).trim());
                        }else if(line.trim().startsWith("mapTileset2")){
                            indexes[2] = Integer.parseInt(line.trim().substring("mapTileset2".length()).trim());
                        }else if(line.trim().startsWith("mapTileset3")){
                            indexes[3] = Integer.parseInt(line.trim().substring("mapTileset3".length()).trim());
                        }else if(line.trim().startsWith("mapTileset4")){
                            indexes[4] = Integer.parseInt(line.trim().substring("mapTileset4".length()).trim());
                        }else if(line.trim().startsWith("mapTileset5")){
                            indexes[5] = Integer.parseInt(line.trim().substring("mapTileset5".length()).trim());
                        }
                        
//                    }                      
                }
            } else {
                Path tilesetspath = Paths.get(tilesetsFilePath);

                byte[] data = Files.readAllBytes(tilesetspath);
                indexes[0] = (int)(data[0]);
                indexes[1] = (int)(data[1]);
                indexes[2] = (int)(data[2]);
                indexes[3] = (int)(data[3]);
                indexes[4] = (int)(data[4]);
                indexes[5] = (int)(data[5]);                
            }        
        } catch (IOException ex) {
            Logger.getLogger(DisassemblyManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return indexes;
    }
    
    private MapBlock[] parseBlockData(){
        MapBlock[] blocks = null;
        try{
            //debugSb = new StringBuilder(inputData.length*8);
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
                        //System.out.println(debugSb.substring(debugSb.length()-1-2));
                        //System.out.println("outputData="+outputData);
                    }else{
                        /* 01 */
                        //System.out.println("commandNumber=$" + Integer.toHexString(initialCommandNumber-remainingCommandNumber)+" - outputNextTileFromTileset");
                        outputNextTileFromTileset();
                        //System.out.println(debugSb.substring(debugSb.length()-1-2));
                        //System.out.println("outputData="+outputData);
                    }
                }else{
                    if(getNextBit()==0){
                        //System.out.println("-1=$"+Integer.toString(outputData.get(outputData.size()-1),16)+", -2=$"+Integer.toString(outputData.get(outputData.size()-2),16)+", -3=$"+Integer.toString(outputData.get(outputData.size()-3),16));
                        if(getNextBit()==0){
                            /* 100 */
                            //System.out.println("commandNumber=$" + Integer.toHexString(initialCommandNumber-remainingCommandNumber)+" - outputRightTileFromHistory");
                            outputRightTileFromHistory();
                            //System.out.println(debugSb.substring(debugSb.length()-1-3));
                            //System.out.println("outputData="+outputData);
                        }else{
                            /* 101 */
                            //System.out.println("commandNumber=$" + Integer.toHexString(initialCommandNumber-remainingCommandNumber)+" - outputBottomTileFromHistory");
                            outputBottomTileFromHistory();
                            //System.out.println(debugSb.substring(debugSb.length()-1-3));
                            //System.out.println("outputData="+outputData);
                        }
                    }else{
                        if(getNextBit()==0){
                            /* 110 */
                            //System.out.println("commandNumber=$" + Integer.toHexString(initialCommandNumber-remainingCommandNumber)+" - outputNextTileWithSameFlags");
                            outputNextTileWithSameFlags();
                            //System.out.println(debugSb.substring(debugSb.length()-1-14));
                            //System.out.println("outputData="+outputData);
                        }else{
                            /* 111 */
                            //System.out.println("commandNumber=$" + Integer.toHexString(initialCommandNumber-remainingCommandNumber)+" - outputNextTileWithNewFlags");
                            outputNextTileWithNewFlags();
                            //System.out.println(debugSb.substring(debugSb.length()-1-17));
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
        short nextTile = 0;
        if((previousTile & 0x0800) == 0){
            nextTile = (short)(outputData.get(outputData.size()-1)+1);
        }else{
            nextTile = (short)(outputData.get(outputData.size()-1)-1);
        }
        nextTile = (short)((nextTile & 0x3FF) | (previousTile & 0xFC00)); 
        outputData.add(nextTile);
    }
    
    private void outputRightTileFromHistory(){
        short leftTile = outputData.get(outputData.size()-1);
        int rightTileHistoryOffset = (leftTile & 0x3FF) + (leftTile & 0x0800)/2;
        Short val = rightTileHistory[rightTileHistoryOffset];
        //System.out.println("Applied value $" + Integer.toString(val,16) + " from rightTileHistory[$" + Integer.toString(rightTileHistoryOffset,16) + "]");
        if(val!=null){
            outputData.add(val);
        }else{
            outputData.add((short)0);
        }
    }
    
    private void outputBottomTileFromHistory(){
        short upperTile = outputData.get(outputData.size()-3);
        int bottomTileHistoryOffset = (upperTile & 0x3FF) + (upperTile & 0x0800)/2;
        Short val = bottomTileHistory[bottomTileHistoryOffset];
        //System.out.println("Applied value $" + Integer.toString(val,16) + " from bottomTileHistory[$" + Integer.toString(bottomTileHistoryOffset,16) + "]");
        outputData.add(val);
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
        int rightTileHistoryOffset = (leftTile & 0x3FF) + (leftTile & 0x0800)/2;
        int bottomTileHistoryOffset = (upperTile & 0x3FF) + (upperTile & 0x0800)/2;
        rightTileHistory[rightTileHistoryOffset] = tileValue;
        //System.out.println("rightTileHistory[$"+Integer.toString(rightTileHistoryOffset,16)+"]=$"+Integer.toString(tileValue,16));
        bottomTileHistory[bottomTileHistoryOffset] = tileValue;
        //System.out.println("bottomTileHistory[$"+Integer.toString(bottomTileHistoryOffset,16)+"]=$"+Integer.toString(tileValue,16));
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
        //System.out.println("Relative value = $" + Integer.toString(relativeValue,16));
        short result = (short)((previousTile&0x3FF) + relativeValue);
        return result;
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
        if(value>=0x180){
            value = (short)((value * 2) + getNextBit() - 0x180);
        }
        //System.out.println("Absolute value = $" + Integer.toString(value,16));
        return value;
    }
    
    private MapBlock[] produceBlocks(){
        outputTiles = new Tile[outputData.size()];
        for(int i=0;i<outputData.size();i++){
            short value = outputData.get(i);
            Tile origTile = tileset[value&0x3FF];
            Tile tile = new Tile();
            tile.setPalette(origTile.getPalette());
            tile.setPixels(origTile.getPixels());
            if((value&0x8000)!=0){
                tile.setHighPriority(true);
            }
            if((value&0x1000)!=0){
                tile = Tile.vFlip(tile);
            }
            if((value&0x0800)!=0){
                tile = Tile.hFlip(tile);
            }
            tile.setId(value&0x3FF);
            outputTiles[i] = tile;
            //System.out.println(i+"="+tile.getId()+", "+tile.isHighPriority()+" "+tile.ishFlip()+" "+tile.isvFlip());
        }
        MapBlock[] blocks = new MapBlock[outputTiles.length/9];
        for(int i=0;i<blocks.length;i++){
            MapBlock block = new MapBlock();
            block.setIndex(i);
            block.setTiles(Arrays.copyOfRange(outputTiles,i*9, i*9+9));
            block.setPalette(block.getTiles()[0].getPalette());
            block.setIcm(block.getTiles()[0].getIcm());
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
        //debugSb.append(bit);
        return bit;
    }

    public void exportDisassembly(MapBlock[] blocks, String filePath){
        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.exportDisassembly() - Exporting disassembly ...");
        try {
            byte[] blockBytes = produceBlockBytes(blocks);
            Path graphicsFilePath = Paths.get(filePath);
            Files.write(graphicsFilePath,blockBytes);
            System.out.println(blocks.length+" Blocks / "+ blockBytes.length + " bytes into " + graphicsFilePath);
        } catch (Exception ex) {
            Logger.getLogger(DisassemblyManager.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            System.out.println(ex);
        }            
        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.exportDisassembly() - Disassembly exported.");        
    }     
    
    private byte[] produceBlockBytes(MapBlock[] blocks){
        rightTileHistory = new Short[0x800];
        bottomTileHistory = new Short[0x800];
        int commandNumber = 0;
        StringBuilder outputSb = new StringBuilder();
        byte[] output;
        Tile[] tiles = new Tile[blocks.length*9-3*9];
        for(int i=0;i<blocks.length-3;i++){
            System.arraycopy(blocks[i+3].getTiles(), 0, tiles, i*9, 9);
        }
        
        for(int i=0;i<(blocks.length-3)*9;i++){
            Tile tile = tiles[i];   
            short tileValue = (short)tile.getId();
            if(tile.isHighPriority()){
                tileValue = (short)(0x8000 | tileValue);
            }
            if(tile.isvFlip()){
                tileValue = (short)(0x1000 | tileValue);
            }
            if(tile.ishFlip()){
                tileValue = (short)(0x0800 | tileValue);
            }
            //System.out.println("Next tile=$"+Integer.toString(tileValue,16)); 
            String command = null;
            if(i==0){
                /* First command */
                command = produceCommand111(tile, null);
                //System.out.println(i + " - Produce value with new flags : " + command);
                updateHistoryMaps(tiles, i);
            }else{
                Tile previousTile = tiles[i-1];
                if(isSameTile(tile, previousTile)){
                    /* Copy last output tile */
                    command = "00";
                    //System.out.println(i + " - Copy last output tile : 00");
                }else {
                    Tile nextTilesetTile = null;
                    if(previousTile.ishFlip()){
                        int index = previousTile.getId()-1;
                        if(index<0){
                            //System.err.println("WARNING - While pointing to previous tile from tileset, had to put tile value 0 instead of this one : "+index);
                            index = 0;
                        }
                        nextTilesetTile = tileset[index];
                    }else{
                        int index = previousTile.getId()+1;
                        if(index>=tileset.length){
                            //System.err.println("WARNING - While pointing to previous tile from tileset, had to put tile value "+(tileset.length-1)+" instead of this one : "+index);
                            index = tileset.length-1;
                        }                        
                        nextTilesetTile = tileset[index];
                    }
                    if(nextTilesetTile.getId() == (tile.getId())
                            && previousTile.isHighPriority() == tile.isHighPriority()
                            && previousTile.ishFlip() == tile.ishFlip()
                            && previousTile.isvFlip() == tile.isvFlip()){
                        /* Copy next tile from tileset with previous tile HFlip direction */
                        command = "01";
                        //System.out.println(i + " - Copy next tile from tileset : 01");
                    }else{
                        int rightTileHistoryOffset = (previousTile.getId() & 0x3FF) + (previousTile.ishFlip()?0x0400:0);
                        Short rightTileValue = rightTileHistory[rightTileHistoryOffset];
                        //System.out.println("rightTileHistory[$"+Integer.toString(rightTileHistoryOffset,16)+"]=$"+((rightTileValue!=null)?Integer.toString(rightTileValue,16):"null")+", tileValue=$"+Integer.toString(tileValue,16));
                        if(rightTileValue!=null && rightTileValue==tileValue){
                            /* Copy mapped tile from right tile history map */
                            command = "100";
                            //System.out.println(i + " - Copy mapped tile from right tile history map : 100");
                        }else{
                            Short bottomTileValue = null;
                            if(i>=3){
                               Tile thirdPreviousTile = tiles[i-3];
                               int bottomTileHistoryOffset = (thirdPreviousTile.getId() & 0x3FF) + (thirdPreviousTile.ishFlip()?0x0400:0);
                               bottomTileValue = bottomTileHistory[bottomTileHistoryOffset];
                            }
                            if(bottomTileValue!=null && bottomTileValue==tileValue){
                                /* Copy mapped tile from bottom tile history map */
                                command = "101";
                                //System.out.println(i + " - Copy mapped tile from bottom tile history map : 101");
                            }else{
                                //System.out.println("highPriority tile="+tile.isHighPriority()+",previous="+previousTile.isHighPriority()
                                //                    + "\nhFlip tile="+tile.ishFlip()+",previous="+previousTile.ishFlip()
                                //                    + "\nvFlip tile="+tile.isvFlip()+",previous="+previousTile.isvFlip());
                                if(tile.isHighPriority()==previousTile.isHighPriority()
                                        && tile.ishFlip()==previousTile.ishFlip()
                                        && tile.isvFlip()==previousTile.isvFlip()){
                                    /* Produce value with same flags */
                                    command = produceCommand110(tile, previousTile);
                                    //System.out.println(i + " - Produce value with same flags : " + command);
                                    updateHistoryMaps(tiles, i);
                                }else{
                                    /* Produce value with new flags */
                                    command = produceCommand111(tile, previousTile);
                                    //System.out.println(i + " - Produce value with new flags : " + command);
                                    updateHistoryMaps(tiles, i);
                                }
                            }
                            
                        }
                        
                    }
                }
            }

            outputSb.append(command);
            commandNumber++;
        }
        
        String commandNumberString = Integer.toString(commandNumber, 2);
        while(commandNumberString.length()<14){
            commandNumberString = "0" + commandNumberString;
        }
        outputSb.insert(0, commandNumberString);
        while(outputSb.length()%16 != 0){
            outputSb.append("0");
        }
        //System.out.println("output = " + outputSb.toString());
        
        /* Byte array conversion */
        output = new byte[outputSb.length()/8];
        for(int i=0;i<output.length;i++){
            Byte b = (byte)(Integer.valueOf(outputSb.substring(i*8, i*8+8),2)&0xFF);
            output[i] = b;
        }
        //System.out.println("output bytes length = " + output.length);
        //System.out.println("output = " + bytesToHex(output));
        
        return output;
    }
    
    private void updateHistoryMaps(Tile[] tiles, int cursor){
        Tile tile = tiles[cursor];
        short tileValue = (short)tile.getId();
        if(tile.isHighPriority()){
            tileValue = (short)(0x8000 | tileValue);
        }
        if(tile.isvFlip()){
            tileValue = (short)(0x1000 | tileValue);
        }
        if(tile.ishFlip()){
            tileValue = (short)(0x0800 | tileValue);
        }
        if(cursor>=1){
            Tile leftTile = tiles[cursor-1];
            int rightTileHistoryOffset = (leftTile.getId() & 0x3FF) + (leftTile.ishFlip()?0x0400:0);
            rightTileHistory[rightTileHistoryOffset] = tileValue;
        }
        if(cursor>=3){
            Tile upperTile = tiles[cursor-3];
            int bottomTileHistoryOffset = (upperTile.getId() & 0x3FF) + (upperTile.ishFlip()?0x0400:0);
            bottomTileHistory[bottomTileHistoryOffset] = tileValue;
        }
    }
    
    private String produceCommand110(Tile tile, Tile previousTile){
        String command = null;
        String value = produceValue(tile, previousTile);
        command = "110" + value;
        return command;
    }   
    
    private String produceCommand111(Tile tile, Tile previousTile){
        String command = null;
        String highPriority = tile.isHighPriority()?"1":"0";
        String vFlip = tile.isvFlip()?"1":"0";
        String hFlip = tile.ishFlip()?"1":"0";
        String flags = highPriority + vFlip + hFlip;
        String value = produceValue(tile, previousTile);
        command = "111" + flags + value;
        return command;
    }   
    
    private String produceValue(Tile tile, Tile previousTile){
        String value = null;
        if(previousTile!=null){
            value = produceRelativeValue(tile, previousTile);
        }
        if(value==null){
            value = produceAbsoluteValue(tile);
        }
        return value;
    }
    
    private String produceRelativeValue(Tile tile, Tile previousTile){
        String value = null;
        for(int i=0;i<32;i++){
            int index = previousTile.getId() - i;
            if(index>0 && index<tileset.length){
                Tile relativeTile = tileset[index];
                if(tile.getId() == relativeTile.getId()){
                    String val = Integer.toString(i, 2);
                    while(val.length()<5){
                        val = "0" + val;
                    }
                    String sign = "1";
                    //System.out.println("Relative value = $-" + Integer.toString(Integer.parseInt(val, 2),16));
                    value = "0" + val + sign;
                    return value;
                }
            }
        }
        for(int i=0;i<32;i++){
            int index = previousTile.getId() + i;
            if(index>0 && index<tileset.length){
                Tile relativeTile = tileset[index];
                if(tile.getId() == relativeTile.getId()){
                    String val = Integer.toString(i, 2);
                    while(val.length()<5){
                        val = "0" + val;
                    }
                    String sign = "0";
                    //System.out.println("Relative value = $" + Integer.toString(Integer.parseInt(val, 2),16));
                    value = "0" + val + sign;
                    return value;
                }
            }
        }
        return value;
    }   
    
    private String produceAbsoluteValue(Tile tile){
        String value = null;
        int id = tile.getId();
        int length = 9;
        if(id>=0x180){
            value = Integer.toString((id + 0x180) / 2,2);
            value += ((id&1)==0)?"0":"1";
            length = 10;
        }else{
            value = Integer.toString(id,2);
        }
        while(value.length()<length){
            value = "0" + value;
        }
        //System.out.println("Absolute value = $" + Integer.toString(id,16));
        value = "1" + value;
        return value;
    }
    
    private static boolean isSameTile(Tile a, Tile b){
        if(a==null || b==null){
            return false;
        }
        return (a.getId() == b.getId()
                && a.isHighPriority() == b.isHighPriority()
                && a.ishFlip() == b.ishFlip()
                && a.isvFlip() == b.isvFlip());
    }

    public Tileset[] getTilesets() {
        return tilesets;
    }

    public void setTilesets(Tileset[] tilesets) {
        this.tilesets = tilesets;
    }

    public Tile[] getTileset() {
        return tileset;
    }

    public void setTileset(Tile[] tileset) {
        this.tileset = tileset;
    }
}
