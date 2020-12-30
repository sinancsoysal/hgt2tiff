package hgt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HGTReader {
    public enum SRTMFormat{ SRTM3, SRTM1, Unknown }
    private String path;
    private SRTMFormat _format;
    private int _tileSize;
    private short[][] _heightData;
    public HGTReader(String path){ this.path = path; }
    private void readHeightDataFromHGTFile(String path){
        generateHeightMap( readBytesFromFile(path) );
    }
    private byte[] readBytesFromFile(String path){
        InputStream file;
        byte[] bytes = null;
        try {
            file = new FileInputStream(path);
            bytes = file.readAllBytes();
        } catch (FileNotFoundException e) {
            System.out.print("[ ERROR ] File not Found!!");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert bytes != null;
        setFormatandAllocate(bytes.length);
        return bytes;
    }

    private void setFormatandAllocate(int length){
        this._format = SRTMFormat.Unknown;
        if(length == Math.pow(1201, 2) * 2){ _format = SRTMFormat.SRTM3; _tileSize = 1201; }
        else if(length == Math.pow(3601, 2) * 2){ _format = SRTMFormat.SRTM1; _tileSize = 3601; }
        this._heightData = new short[_tileSize][_tileSize];
    }

    private void generateHeightMap(byte[] bytes){
        int currPos = 0;
        for(int y = 0; y < this._tileSize; y++){
            for(int x = 0; x < this._tileSize; x++){
                ByteBuffer byteBuffer = ByteBuffer.allocate(2);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

                short height;

                if(java.nio.ByteOrder.nativeOrder().toString().equals("LITTLE_ENDIAN")){
                    byteBuffer.put(bytes[currPos + 1]);
                    byteBuffer.put(bytes[currPos]);
                    height = byteBuffer.getShort();
                } else{
                    byteBuffer.put(bytes[currPos]);
                    byteBuffer.put(bytes[currPos + 1]);
                    height = byteBuffer.getShort();
                }
                byteBuffer.clear();

                // Voids are flagged with the value -32768
                if(height == -32768){ height = 0; }

                _heightData[y][x] = height;
                currPos += 2;
            }
        }
    }

    public short[][] getElevationDataMatrix(){ return _heightData; }
    public short getElevationAt(int x, int y){ return _heightData[y][x]; }
    public int getTileSize(){ return _tileSize; }
    public SRTMFormat getFileFormat(){ return _format; }
}
