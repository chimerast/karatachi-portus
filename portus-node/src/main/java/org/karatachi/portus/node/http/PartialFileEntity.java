package org.karatachi.portus.node.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.AbstractHttpEntity;

public class PartialFileEntity extends AbstractHttpEntity {
    private final File file;
    private final long from;
    private final long len;

    public PartialFileEntity(final File file, final String contentType,
            long from, long to) {
        super();
        if (file == null) {
            throw new IllegalArgumentException("File may not be null");
        }
        this.file = file;
        this.from = from;
        this.len = to - from + 1;
        setContentType(contentType);
    }

    public boolean isRepeatable() {
        return true;
    }

    public long getContentLength() {
        return this.len;
    }

    public InputStream getContent() throws IOException {
        return new FileInputStream(this.file);
    }

    public void writeTo(final OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }

        InputStream instream = new FileInputStream(this.file);
        try {
            instream.skip(from);
            long rest = len;

            byte[] tmp = new byte[4096];
            int l;
            while ((l = instream.read(tmp)) != -1) {
                if (rest < l) {
                    outstream.write(tmp, 0, (int) rest);
                    break;
                } else {
                    outstream.write(tmp, 0, l);
                }
                rest -= l;
            }
            outstream.flush();
        } finally {
            instream.close();
        }
    }

    public boolean isStreaming() {
        return false;
    }
}
