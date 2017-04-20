package org.jaudiotagger.tag.images;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Image Handler
 */
public interface ImageHandler
{
    public void reduceQuality(Artwork artwork, int maxSize) throws IOException;
    public Bitmap makeSmaller(Artwork artwork, int size) throws IOException;
    public boolean isMimeTypeWritable(String mimeType);
    public byte[] writeImage(OutputStream bi, String mimeType) throws IOException;
    public byte[] writeImageAsPng(OutputStream bi) throws IOException;
    public void showReadFormats();
    public void showWriteFormats();
}
