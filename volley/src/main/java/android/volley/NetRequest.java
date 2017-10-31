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

import java.util.Map;

public class NetRequest<T extends NetEnty> extends Request<T> {
    private T mEnty;

    // req//////////////////////////////////////////////////////////////////////////////////////////////
    public NetRequest(T enty, OnNetResponse<T> netResponse) {
        super(enty.getMethod(), enty.getUrl(), netResponse);//Error Listener
        mEnty = enty;
        this.listener = netResponse;
        setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 1, 1f));
        Log._NETLOG_OUT(enty);
    }

    public NetRequest(T enty, RequestFuture<T> future) {
        super(enty.getMethod(), enty.getUrl(), future);
        mEnty = enty;
        this.listener = future;
        setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 1, 1f));
        Log._NETLOG_OUT(enty);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mEnty.getHeaders();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return mEnty.getBody();
    }

    public String getBodyContentType() {
        return mEnty.getBodyContentType();
    }

    // res//////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        Log._NETLOG_IN(mEnty, listener, response);

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
        Log._NETLOG_IN(mEnty, listener, volleyError);// log
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

}
