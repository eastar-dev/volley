package android.volley;

import android.net.Uri;

import com.android.volley.Request.Method;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NetEnty {

    public boolean _OUT_H = false;
    public boolean _OUT_1 = false;
    public boolean _OUT_2 = false;
    public boolean _OUT_3 = false;

    public boolean _IN_H = false;
    public boolean _IN_1 = false;
    public boolean _IN_2 = false;
    public boolean _IN_3 = false;

    public NetEnty(int method) {
        this.method = method;
    }

    public boolean isDummy() {
        return false;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    protected void setParams(Object... key_value) {
        if (key_value.length % 2 == 1)
            throw new IllegalArgumentException("!!key value must pair");

        int N = (key_value.length) / 2;
        for (int i = 0; i < N; i++) {
            final String key = (String) key_value[i * 2];
            final Object value = key_value[i * 2 + 1];
//			Log.l(key, value);
            if (value == null) {
                params.put(key, null);
            } else if (value.getClass().isArray()) {
                final Object[] values = (Object[]) value;
                for (int j = 0; j < values.length; j++)
                    params.put(key + "[" + j + "]", values[j]);
            } else if (value.getClass().isAssignableFrom(List.class)) {
                final List<?> values = (List<?>) value;
                for (int j = 0; j < values.size(); j++)
                    params.put(key + "[" + j + "]", values.get(j));
            } else if (value instanceof Boolean) {
                params.put(key, (((Boolean) value) ? "Y" : "N"));
            } else {
                params.put(key, value);
            }
        }
    }

    protected void setHeaders(String... key_value) {
        if (key_value.length % 2 == 1)
            throw new IllegalArgumentException("!!key value must pair");

        int N = (key_value.length) / 2;
        for (int i = 0; i < N; i++) {
            final String key = (String) key_value[i * 2];
            final String value = key_value[i * 2 + 1];
            headers.put(key, value);
        }
    }

    String getUrl() {
        if (method == Method.GET)
            return encodeParametersUrl();
        return url;
    }

    int getMethod() {
        return method;
    }

    //req
    private final int method;
    protected String url;
    //req-header
    protected Map<String, String> headers = new HashMap<String, String>();
    //req-post
    private byte[] body;
    private String contentType = "Content-Type: text/plain; charset=utf-8";
    protected Map<String, Object> params = new HashMap<String, Object>();

    //res
    private Map<String, String> responseHeader = new HashMap<String, String>();
    protected String response;
    //res-parsed data
    protected boolean success;
    protected String errorMessage;
    protected Gson gson;

    protected void parseStatusCode(int statusCode) {
        success = (statusCode == HttpURLConnection.HTTP_OK);
    }

    protected void parseHeaders(Map<String, String> headers) {
        this.responseHeader = headers;
    }

    protected void parseData(String json) {
        gson = gsonCreate();
        this.response = json;
        try {
            final Field field = getClass().getDeclaredField("data");
            field.setAccessible(true);
            field.set(this, gson.fromJson(json, field.getType()));
            success = true;
        } catch (NoSuchFieldException e) {
            Log.w("NoSuchFieldException 'data' in " + getClass().getSimpleName());
            Log.w(json);
            success = false;
        } catch (Exception e) {
            Log.w(e);
            success = false;
        }

        if (success)
            return;

        try {
            Field field = this.getClass().getDeclaredField("errors");
            field.setAccessible(true);
            field.set(this, this.gson.fromJson(json, field.getType()));
        } catch (NoSuchFieldException e) {
            Log.w("NoSuchFieldException 'errors' in " + getClass().getSimpleName());
            Log.w(json);
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        Log.p(success ? Log.INFO : Log.ERROR, getClass().getSimpleName(), success);
    }

    protected Gson gsonCreate() {
        return new GsonBuilder().create();
    }

    protected void error(Exception error) {
        this.errorMessage = error.getMessage();
    }

    protected Object getQueryParameter(String key) {
        if (method == Method.GET)
            return Uri.parse(url).getQueryParameter(key);
        else
            return params.get(key);
    }

    protected Map<String, String> getHeaders() {
        return headers;
    }

    protected byte[] getBody() {
        if (body == null) {
            setBody();
        }
        return body;
    }

    protected String getBodyContentType() {
        return contentType;
    }

    protected String getResponse() {
        return response;
    }

    protected Map<String, String> getResponseHeader() {
        return responseHeader;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    void onDeliverResponse() {

    }

    void onDeliverError() {

    }

    //get
    private String encodeParametersUrl() {
        if (isEmpty(url))
            return url;

        final Uri uri = Uri.parse(url);

        final String scheme = uri.getScheme();
        final String authority = uri.getEncodedAuthority();
        final String path = uri.getEncodedPath();
        final String query = uri.getQuery();
        final String fragment = uri.getFragment();

        String param = getEncodedParams(query);

        String url = scheme + "://" + authority;
        if (!isEmpty(path))
            url += path;
        if (!isEmpty(param))
            url += "?" + param;
        if (!isEmpty(fragment))
            url += "#" + fragment;
        return url;
    }

    private String getEncodedParams(String query) {
        if (isEmpty(query))
            return "";

        final Map<String, Object> paramsMap = new HashMap<String, Object>();
        final String[] params = query.split("&");
        for (int i = 0; i < params.length; i++) {
            final String keyvalue = params[i];

            int po = keyvalue.indexOf("=");
            if (-1 == po)
                continue;

            final String key = keyvalue.substring(0, po);
            final String value = keyvalue.substring(po + 1);

            paramsMap.put(key, value);
        }

        final Set<Entry<String, Object>> set = paramsMap.entrySet();

        StringBuilder encodedParams = new StringBuilder();
        for (Entry<String, Object> entry : set) {
            if (!(entry.getValue() instanceof CharSequence)) {
                Log.w("! unsupported value ", entry.getKey(), entry.getValue().toString());
                continue;
            }

            try {
                encodedParams.append(URLEncoder.encode(entry.getKey(), Net.UTF8));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue().toString(), Net.UTF8));
                encodedParams.append('&');
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Encoding not supported: " + Net.UTF8, e);
            }
        }
        return encodedParams.toString();
    }

    //post
    private void setBody() {
        if (isStringParams())
            setParametersBody();
        else
            setMultipartBody();
    }

    private void setParametersBody() {
        Map<String, Object> stringParams = this.params;

        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Entry<String, ? extends Object> entry : stringParams.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                if (value == null)
                    continue;

                encodedParams.append(URLEncoder.encode(key, Net.UTF8));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(value.toString(), Net.UTF8));
                encodedParams.append('&');
            }
            this.body = encodedParams.toString().getBytes(Net.UTF8);
            this.contentType = "application/x-www-form-urlencoded; charset=" + Net.UTF8;
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + Net.UTF8, uee);
        }
    }

    private boolean isStringParams() {
        for (Object obj : params.values()) {
            if (obj instanceof File)
                return false;
        }
        return true;
    }

    private void setMultipartBody() {
        try {
            MultipartUtil builder = new MultipartUtil();
            final Set<Entry<String, Object>> entrys = params.entrySet();
            //text
            for (Entry<String, Object> entry : entrys) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                if (value == null)
                    continue;

                if (value instanceof File) {
                    ;
                } else {
                    builder.addText(key, value.toString());
                }
            }
            //file
            for (Entry<String, Object> entry : entrys) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                if (value == null)
                    continue;

                if (value instanceof File) {
                    builder.addFile(key, (File) value);
                } else {
                    ;
                }
            }
            builder.build();

            this.body = builder.toByteArray();
            this.contentType = builder.getContentType();
//			headers.put("Content-Length", Long.toString(body.length));
        } catch (IOException e) {
            throw new RuntimeException("multipartEntity not supported: ", e);
        }
    }

    //etc
    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + (getMethod() == Method.POST ? "POST" : "GET") + ">" + getUrl();
    }

    private static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private class MultipartUtil {
        public static final String TWO_HYPHENS = "--";
        public static final String BOUNDARY = "==WebKitFormBoundaryIT3nXTYf3AEmugh4==";
        public static final String CRLF = "\r\n";
        public static final int MAX_BUFFER_SIZE = 1024 * 1024;

        private ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private DataOutputStream os = new DataOutputStream(baos);

        public String getContentType() {
            return "multipart/form-data; boundary=" + BOUNDARY;
//			return "multipart/form-data; boundary=" + BOUNDARY + "; charset=EUC-KR";
        }

        public byte[] toByteArray() {
            return baos.toByteArray();
        }

        public void build() throws IOException {
            os.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + CRLF);
            os.close();
        }

        public void addText(String key, String value) throws IOException {
            os.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
            os.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + CRLF);
//			os.writeBytes("Content-Disposition: form-data; name=\"+key+\", Content-Type: text/plain; charset=EUC-KR, Content-Transfer-Encoding: 8bit" + CRLF);
//			os.writeBytes("Content-Type: text/plain; charset=EUC-KR" + CRLF);

            os.writeBytes(CRLF);
            os.writeBytes(value);
            os.writeBytes(CRLF);
        }

        public void addFile(String key, File file) throws IOException {
            String fileName = file.getName();
            os.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
            os.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"" + CRLF);
//			os.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(fileName) + CRLF);
            os.writeBytes("Content-Transfer-Encoding: binary" + CRLF);
            os.writeBytes(CRLF);

            FileInputStream is = new FileInputStream(file);
            byte[] buffer = new byte[MAX_BUFFER_SIZE];
            int bytesRead = is.read(buffer, 0, MAX_BUFFER_SIZE);
            while (bytesRead > 0) {
                os.write(buffer, 0, bytesRead);
                bytesRead = is.read(buffer, 0, MAX_BUFFER_SIZE);
            }
            is.close();
            os.writeBytes(CRLF);
        }

    }

}