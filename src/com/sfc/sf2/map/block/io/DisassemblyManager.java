/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.map.block.io;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.map.block.MapBlock;
import com.sfc.sf2.graphics.compressed.StackGraphicsDecoder;
import static com.sfc.sf2.graphics.compressed.StackGraphicsEncoder.bytesToHex;
import com.sfc.sf2.palette.graphics.PaletteDecoder;
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
    private StringBuilder debugSb = null;
    
    Color[] palette = null;
    Tile[] tileset = new Tile[128*5];
    private Short[] rightTileHistory = new Short[0x800];
    private Short[] bottomTileHistory = new Short[0x800];
    
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
                        System.arraycopy(tileset, 0, this.tileset, 0*128, tileset.length);
                    }else{
                        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - File ignored because of wrong length " + tileset1Data.length + " : " + tileset1Path);
                    }
                }else{
                    System.arraycopy(emptyTileset, 0, tileset, 0*128, emptyTileset.length);
                }
                if(tileset2path.toFile().exists()){
                    byte[] tileset2Data = Files.readAllBytes(tileset2path);
                    if(tileset2Data.length>2){
                        Tile[] tileset = new StackGraphicsDecoder().decodeStackGraphics(tileset2Data, palette);
                        System.arraycopy(tileset, 0, this.tileset, 1*128, tileset.length);
                    }else{
                        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - File ignored because of wrong length " + tileset2Data.length + " : " + tileset2Path);
                    }
                }else{
                    System.arraycopy(emptyTileset, 0, tileset, 1*128, emptyTileset.length);
                }
                if(tileset3path.toFile().exists()){
                    byte[] tileset3Data = Files.readAllBytes(tileset3path);
                    if(tileset3Data.length>2){
                        Tile[] tileset = new StackGraphicsDecoder().decodeStackGraphics(tileset3Data, palette);
                        System.arraycopy(tileset, 0, this.tileset, 2*128, tileset.length);
                    }else{
                        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - File ignored because of wrong length " + tileset3Data.length + " : " + tileset3Path);
                    }
                }else{
                    System.arraycopy(emptyTileset, 0, tileset, 2*128, emptyTileset.length);
                }
                if(tileset4path.toFile().exists()){
                    byte[] tileset4Data = Files.readAllBytes(tileset4path);
                    if(tileset4Data.length>2){
                        Tile[] tileset = new StackGraphicsDecoder().decodeStackGraphics(tileset4Data, palette);
                        System.arraycopy(tileset, 0, this.tileset, 3*128, tileset.length);
                    }else{
                        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - File ignored because of wrong length " + tileset4Data.length + " : " + tileset4Path);
                    }
                }else{
                    System.arraycopy(emptyTileset, 0, tileset, 3*128, emptyTileset.length);
                }
                if(tileset5path.toFile().exists()){
                    byte[] tileset5Data = Files.readAllBytes(tileset5path);
                    if(tileset5Data.length>2){
                        Tile[] tileset = new StackGraphicsDecoder().decodeStackGraphics(tileset5Data, palette);
                        System.arraycopy(tileset, 0, this.tileset, 4*128, tileset.length);
                    }else{
                        System.out.println("com.sfc.sf2.mapblock.io.DisassemblyManager.parseGraphics() - File ignored because of wrong length " + tileset5Data.length + " : " + tileset5Path);
                    }
                }else{
                    System.arraycopy(emptyTileset, 0, tileset, 4*128, emptyTileset.length);
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
            System.out.println(blockBytes.length + " bytes into " + graphicsFilePath);
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
                            System.err.println("WARNING - While pointing to previous tile from tileset, had to put tile value 0 instead of this one : "+index);
                            index = 0;
                        }
                        nextTilesetTile = tileset[index];
                    }else{
                        nextTilesetTile = tileset[previousTile.getId()+1];
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

    public Tile[] getTileset() {
        return tileset;
    }

    public void setTileset(Tile[] tileset) {
        this.tileset = tileset;
    }
    
}
