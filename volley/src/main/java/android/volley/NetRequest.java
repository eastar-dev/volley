/*
 * Copyright (C) 2012 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.volley;

import android.volley.Net.OnNetResponse;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NetRequest<T extends NetEnty> extends Request<T> {
    private T mEnty;

    // req//////////////////////////////////////////////////////////////////////////////////////////////
    public NetRequest(T enty, OnNetResponse<T> netResponse) {
        this(enty, netResponse, netResponse);
    }
    public NetRequest(T enty, Response.Listener<T> onResponse, Response.ErrorListener onError) {
        super(enty.getMethod(), enty.getUrl(), onError);//Error Listener
        mStack = getStack();
        mEnty = enty;
        this.listener = onResponse;
        setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 1, 1f));
        _NETLOG_OUT();
    }

    public NetRequest(T enty, RequestFuture<T> future) {
        super(enty.getMethod(), enty.getUrl(), future);
        mStack = getStack();
        mEnty = enty;
        this.listener = future;
        setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 1, 1f));
        _NETLOG_OUT();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        _NETLOG_OUT(mEnty.headers);
        return mEnty.getHeaders();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return mEnty.getBody();
//		final byte[] body = mEnty.getBody();
//		if (body != null && body.length > 0)
//			return mEnty.getBody();
//		return super.getBody();
    }

    public String getBodyContentType() {
        return mEnty.getBodyContentType();
    }

    // res//////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        _NETLOG_IN(response.headers);
        _NETLOG_IN(response);

        try {
            mEnty.parseStatusCode(response.statusCode);
        } catch (Exception e) {
            Log.printStackTrace(e);
        }

        try {
            mEnty.parseHeaders(response.headers);
        } catch (Exception e) {
            Log.printStackTrace(e);
        }

        try {
            mEnty.parseData(new String(response.data, HttpHeaderParser.parseCharset(response.headers, Net.UTF8)));
        } catch (Exception e) {
            Log.printStackTrace(e);
            return Response.error(new ParseError(response));
        }

        if (mEnty.isSuccess())
            return Response.success(mEnty, HttpHeaderParser.parseCacheHeaders(response));
        else
            return Response.error(new VolleyError(mEnty.errorMessage));
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        _NETLOG_IN(volleyError);// log
        mEnty.error(volleyError);
        return super.parseNetworkError(volleyError);
    }

    //callback//////////////////////////////////////////////////////////////////////////////////////////////
    private Response.Listener<T> listener;

    @Override
    protected void deliverResponse(T response) {
        mEnty.onDeliverResponse();
        if (listener != null)
            listener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mEnty.onDeliverError();
        super.deliverError(error);
    }

    //debug//////////////////////////////////////////////////////////////////////////////////////////////
    private final StackTraceElement mStack;

    private StackTraceElement getStack() {
        StackTraceElement[] stackTraceElements = (new Exception()).getStackTrace();
        int i = 0;
        int N = stackTraceElements.length;
        StackTraceElement stackTraceElement;
        for (stackTraceElement = stackTraceElements[i]; i < N; ++i) {
            stackTraceElement = stackTraceElements[i];
            String className = stackTraceElement.getClassName();
            if (!className.matches("^android\\.volley\\..+")) {
                break;
            }
        }
        while (i >= 0) {
            stackTraceElement = stackTraceElements[i];
            int lineNumber = stackTraceElement.getLineNumber();
            if (lineNumber >= 0) {
                break;
            }
            --i;
        }
        return stackTraceElement;
    }

    public static final String SPACE = "          ";

    //log res//
    private void _NETLOG_OUT() {
        if (!Net.LOG)
            return;
        try {
            if (mEnty._OUT_1 || mEnty._OUT_2 || mEnty._OUT_3 || Net._OUT_1 || Net._OUT_2 || Net._OUT_3) {
                Log.ps(Log.ERROR, mStack, "나가:", mEnty.toString());
            }

            if (mEnty._OUT_2 || mEnty._OUT_3 || Net._OUT_2 || Net._OUT_3) {
                if (mEnty.params != null) {
                    final StringBuilder sb = new StringBuilder("\n");
                    final Set<Entry<String, Object>> set = mEnty.params.entrySet();
                    for (Entry<String, Object> entry : set) {
                        if (entry.getValue() == null)
                            continue;
                        sb.append("  ").append(entry.getKey()).append(SPACE.substring(Math.min(SPACE.length(), entry.getKey().length())));
                        sb.append(" = ").append(entry.getValue());
                        sb.append("\n");
                    }
                    Log.ps(Log.ERROR, mStack, sb);
                }
            }

            if (mEnty._OUT_3 || Net._OUT_3) {
                Log.ps(Log.ERROR, mStack, new String(mEnty.getBody()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private void _NETLOG_OUT(Map<String, String> headers) {
        if (!Net.LOG)
            return;
        if (!mEnty._OUT_H && !Net._OUT_H)
            return;
        try {
            StringBuilder sb = new StringBuilder("\n");
            final Set<Entry<String, String>> set = mEnty.headers.entrySet();
            for (Entry<String, String> entry : set) {
                sb.append("\t").append(entry.getKey()).append(": ").append(prettyJson(entry.getValue())).append("\n");
            }

            Log.ps(Log.ERROR, mStack, "나해:", mEnty.getClass().getSimpleName(), sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private void _NETLOG_IN(Map<String, String> headers) {
        if (!Net.LOG)
            return;
        if (!mEnty._IN_H && !Net._IN_H)
            return;
        try {
            StringBuilder sb = new StringBuilder("\n");
            final Set<Entry<String, String>> set = headers.entrySet();
            for (Entry<String, String> entry : set) {
                sb.append("\t").append(entry.getKey()).append(": ").append(prettyJson(entry.getValue())).append("\n");
            }
            Log.ps(Log.WARN, mStack, "들해:", mEnty.getClass().getSimpleName(), sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void _NETLOG_IN(NetworkResponse response) {
        if (!Net.LOG)
            return;

        try {
            if (mEnty._IN_1 || Net._IN_1 || mEnty._IN_2 || Net._IN_2) {
                Log.ps(Log.INFO, mStack, "들와:", response.statusCode, mEnty);
            }
            if (mEnty._IN_2 || Net._IN_2) {
                Log.ps(Log.INFO, mStack, "들와:", response.statusCode, new String(response.data, HttpHeaderParser.parseCharset(response.headers, Net.UTF8)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void _NETLOG_IN(VolleyError volleyError) {
        if (!Net.LOG)
            return;
        try {
            if (mEnty._IN_1 || Net._IN_1 || mEnty._IN_2 || Net._IN_2 || mEnty._IN_3 || Net._IN_3) {
                Object statusCode = null;
                try {
                    statusCode = volleyError.networkResponse.statusCode;
                } catch (Exception e) {
                    statusCode = volleyError.getMessage();
                }
//                Log.e("!들와:", mEnty, "" + statusCode);
                Log.ps(Log.ERROR, mStack, "!들와:", mEnty, "" + statusCode);
            }
            if (mEnty._IN_2 || Net._IN_2 || mEnty._IN_3 || Net._IN_3) {
//                Log.e("!들와1/5:", "" + volleyError.getClass().getSimpleName());
//                Log.e("!들와2/5:", "" + volleyError.getMessage());
//                Log.e("!들와3/5:", "" + volleyError.networkResponse);
                Log.ps(Log.ERROR, mStack, "!들와1/5:", "" + volleyError.getClass().getSimpleName());
                Log.ps(Log.ERROR, mStack, "!들와2/5:", "" + volleyError.getMessage());
                Log.ps(Log.ERROR, mStack, "!들와3/5:", "" + volleyError.networkResponse);
                try {
//                    Log.e("!들와4/5:", "" + volleyError.networkResponse.statusCode);
                    Log.ps(Log.ERROR, mStack, "!들와4/5:", "" + volleyError.networkResponse.statusCode);
                } catch (Exception e) {
                }
                try {
//                    Log.e("!들와5/5:", prettyJson(new String(volleyError.networkResponse.data, HttpHeaderParser.parseCharset(volleyError.networkResponse.headers, Net.UTF8))));
                    Log.ps(Log.ERROR, mStack, "!들와5/5:", prettyJson(new String(volleyError.networkResponse.data, HttpHeaderParser.parseCharset(volleyError.networkResponse.headers, Net.UTF8))));
                } catch (Exception e) {
                }
            }
            if (mEnty._IN_3 || Net._IN_3) {
                volleyError.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String prettyJson(String json) throws JSONException {
        String j = json.trim();
        if (j.length() > 0) {
            if (j.charAt(0) == '{') {
                return new JSONObject(j).toString(4);
            }
            if (j.charAt(0) == '[') {
                return new JSONArray(j).toString(4);
            }
        }
        return json;
    }
}
