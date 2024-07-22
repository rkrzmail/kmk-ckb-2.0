package com.kmkbe.core.middleware;

import com.kmkbe.core.utils.ObjectUtils;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpServletRequestCopier extends HttpServletRequestWrapper {

    private final MyServletInputStream inputStream;

    public HttpServletRequestCopier(HttpServletRequest request) throws IOException {
        super(request);
        inputStream = new MyServletInputStream(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.inputStream;
    }

    public byte[] getContentAsByteArray() {
        return inputStream.toByteArray();
    }

    public Map<String, Object> getPayload() {
        try {
            final byte[] bytes = getContentAsByteArray();
            final int read = inputStream.read(bytes, 0, bytes.length);
            return ObjectUtils.strToJson(new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return null;
        }
    }


    private static class MyServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream in;
        private final ByteArrayOutputStream out;

        public MyServletInputStream(ServletInputStream input) throws IOException {

            // copy request stream to output stream
            out = new ByteArrayOutputStream();
            InputStreamReader reader = new InputStreamReader(input);
            int b = reader.read();
            while (b >= 0) {
                out.write(b);
                b = reader.read();
            }

            // provide input stream from output stream
            in = new ByteArrayInputStream(out.toByteArray());
        }

        public byte[] toByteArray() {
            return out.toByteArray();
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            // no action
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }
    }
}
