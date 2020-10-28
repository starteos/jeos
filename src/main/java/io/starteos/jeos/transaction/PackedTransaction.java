package io.starteos.jeos.transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import io.starteos.jeos.crypto.util.HexUtils;
import io.starteos.jeos.raw.Writer;


/**
 * Created by swapnibble on 2018-03-19.
 */

public class PackedTransaction {
    public enum CompressType{ none, zlib }

    final List<String> signatures ;

    final String compression;

    private String packed_context_free_data;

    private String packed_trx;

    public PackedTransaction(SignedTransaction stxn, CompressType compressType){
        compression = compressType.name();
        signatures = stxn.getSignatures();

        packed_trx = HexUtils.toHex( packTransaction( stxn, compressType) );

        byte[] packed_ctx_free_bytes = packContextFreeData( stxn.getCtxFreeData(), compressType );
        packed_context_free_data = ( packed_ctx_free_bytes.length == 0 ) ? "" : HexUtils.toHex( packed_ctx_free_bytes  );
    }

    private byte[] packTransaction( Transaction transaction, CompressType compressType ) {
        Writer byteWriter = new Writer(512);
        transaction.pack(byteWriter);
        // pack -> compress
        return compress( byteWriter.toBytes(), compressType ) ;
    }


    private byte[] packContextFreeData(List<String> ctxFreeData, CompressType compressType ){
        Writer byteWriter = new Writer(64);

        int ctxFreeDataCount = ( ctxFreeData == null ) ? 0 : ctxFreeData.size();
        if ( ctxFreeDataCount == 0 ){
            return byteWriter.toBytes();
        }

        // context free data is vector<vector<char>> !

        byteWriter.putUint( ctxFreeDataCount);

        for ( String hexData : ctxFreeData ) {
            byte [] dataBytes = HexUtils.toBytes( hexData);
            byteWriter.putUint( dataBytes.length );
            byteWriter.putBytes( dataBytes);
        }

        return  compress( byteWriter.toBytes(), compressType ) ;
    }


    public PackedTransaction(SignedTransaction stxn){
        this( stxn, CompressType.none);
    }

//    public long getDataSize() {
//        return data.length() / 2; // hex -> raw bytes
//    }

    private byte[] compress( byte[] uncompressedBytes, CompressType compressType) {
        if ( compressType == null || !CompressType.zlib.equals( compressType)) {
            return uncompressedBytes;
        }

        // zip!
        Deflater deflater = new Deflater( Deflater.BEST_COMPRESSION );
        deflater.setInput( uncompressedBytes );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( uncompressedBytes.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated core... index
            outputStream.write(buffer, 0, count);
        }

        try {
            outputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return uncompressedBytes;
        }

        return outputStream.toByteArray();
    }

    private byte[] decompress( byte [] compressedBytes ) {
        Inflater inflater = new Inflater();
        inflater.setInput( compressedBytes );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( compressedBytes.length);
        byte[] buffer = new byte[1024];

        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        }
        catch (DataFormatException | IOException e) {
            e.printStackTrace();
            return compressedBytes;
        }


        return outputStream.toByteArray();
    }

    public List<String> getSignatures() {
        return signatures;
    }

    public String getCompression() {
        return compression;
    }

    public String getPacked_context_free_data() {
        return packed_context_free_data;
    }

    public String getPacked_trx() {
        return packed_trx;
    }
}
