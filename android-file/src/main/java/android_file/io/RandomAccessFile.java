/*
 * Copyright 1994-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package android_file.io;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Rahul Verma on 02/12/16 <rv@videoder.com>
 */
/**
 * Replacement for java.io.RandomAccessFile for supporting read/write operations on external SD card on android 4.4+
 * Android removed the read/write capability for applications on external SD card on android 4.4 and
 * later introduced Storage Access Framework to indirectly access it that too with users' consent
 * <p>
 * <p>This class wraps all the workarounds and SAF operations and provides you standard RandomAccessFile api.</p>
 */
public class RandomAccessFile implements Closeable {

    private File file;
    private FileChannel fileChannel;
    private long position;
    private java.io.RandomAccessFile wrappedFileForPreKitkat;
    private boolean isInputChannel;
    private boolean closed;
    private FileOutputStream fileOutputStream;
    private FileInputStream fileInputStream;
    private static final String TAG = "RandomAccessFile";
    private boolean writableNormally;


    public RandomAccessFile(File file, String mode) throws IOException {
        if (file.isWritableNormally()) {
            writableNormally = true;
            this.wrappedFileForPreKitkat = new java.io.RandomAccessFile(file.getWrappedFile(), mode);
        } else {
            writableNormally = false;
            this.file = file;
            fileOutputStream = file.getOutputStream(true);
            if (fileOutputStream != null) {
                fileChannel = fileOutputStream.getChannel();
                isInputChannel = false;
            } else
                throw new IOException("got null FileOutputStream");
        }
    }

    public long readBytes(byte[] b) throws IOException {

        if (writableNormally) {
            return wrappedFileForPreKitkat.read(b);
        } else {
            if (!isInputChannel) {
                try {
                    closeFileStreams();
                    fileChannel.close();
                    fileInputStream = file.getInputStream();
                    fileChannel = fileInputStream.getChannel();
                    isInputChannel = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                int bytesRead = fileChannel.read(ByteBuffer.wrap(b), position);
                position = position + bytesRead;
                return bytesRead;
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }

    }

    public void write(byte b[], int off, int len) throws IOException {
        if (writableNormally) {
            wrappedFileForPreKitkat.write(b, off, len);
        } else {
            if (isInputChannel) {
                try {
                    closeFileStreams();
                    fileChannel.close();
                    fileOutputStream = file.getOutputStream(true);
                    fileChannel = fileOutputStream.getChannel();
                    isInputChannel = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                position = position + fileChannel.write(ByteBuffer.wrap(b, off, len), position);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public long position() throws IOException {
        if (writableNormally) {
            return wrappedFileForPreKitkat.getFilePointer();
        } else {
            return position;
        }
    }


    public void seek(long pos) throws IOException {
        if (writableNormally) {
            wrappedFileForPreKitkat.seek(pos);
        } else {
            position = pos;
        }

    }


    public void close() throws IOException {
        if (writableNormally) {
            wrappedFileForPreKitkat.close();
        } else {
            if (closed) {
                return;
            }
            closed = true;
            closeFileStreams();
            if (fileChannel != null) {
                fileChannel.close();
            }
        }


    }

    private void closeFileStreams() {
        try {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } catch (IOException ignored) {
        }
        try {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IOException ignored) {
        }
    }

}