package android.volley;

import android.log.Log;
import android.support.annotation.WorkerThread;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.RequestFuture;

import java.util.concurrent.ExecutionException;

/**
 * <pre>
 * &lt;uses-permission android:name="android.permission.INTERNET" />
 * </pre>
 */
public class Net {
    public static final String EUCKR = "euc-kr";
    public static final String UTF8 = "UTF-8";
    public static final String COOKIE = "Cookie";
    public static final String SET_COOKIE = "set-cookie";
    public static final String USER_AGENT = "User-Agent";

    public static boolean LOG = false;
    public static boolean _OUT_1 = true;
    public static boolean _OUT_2 = false;
    public static boolean _OUT_3 = false;
    public static boolean _OUT_H = false;

    public static boolean _IN_1 = true;
    public static boolean _IN_2 = false;
    public static boolean _IN_3 = false;
    public static boolean _IN_H = false;

    private static final RequestQueue sRequestQueue = new RequestQueue(new NoCache(), new BasicNetwork(new HurlStack()));

    static {
        sRequestQueue.start();
    }

    /**
     * 동기기다림
     */
    @WorkerThread
    public static <T extends NetEnty> T block(T enty) throws InterruptedException, ExecutionException, VolleyError {
        return sync(enty).get();
    }

    /**
     * 동기
     */
    @WorkerThread
    public static <T extends NetEnty> RequestFuture<T> sync(T enty) {
        RequestFuture<T> future = RequestFuture.newFuture();
        sRequestQueue.add(new NetRequest<T>(enty, future));
        return future;
    }

    /**
     * 비동기
     */
    public static <T extends NetEnty> void sent(T enty) {
        async(enty, (OnNetResponse<T>) null);
    }

    /**
     * 비동기Callback
     */
    public static <T extends NetEnty> void async(T enty, OnNetResponse<T> onResponse) {
        if (enty.isDummy() && onResponse != null) {
            Log.w(enty.getClass(), " is dummy codeing? deliver to successed");
            onResponse.onResponse(enty);
            return;
        }
        sRequestQueue.add(new NetRequest<T>(enty, onResponse));
    }

    public static interface OnNetResponse<T> extends Response.Listener<T>, Response.ErrorListener {
        void onErrorResponse(VolleyError error);

        void onResponse(T response);
    }

    public static class OnSimpleNetResponse<T> implements OnNetResponse<T> {
        @Override
        public void onErrorResponse(VolleyError error) {
            try {
                Log.l("ErrorResponse", error.networkResponse.statusCode, new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers, Net.UTF8)));
            } catch (Exception e) {
            }

            try {
                if (error instanceof NoConnectionError) {
                    Log.l("NoConnectionError", error);
                } else if (error instanceof NetworkError) {
                    Log.l("NetworkError", error);
                } else if (error instanceof TimeoutError) {
                    Log.l("TimeoutError", error);
                } else if (error instanceof VolleyError) {
                    Log.l("VolleyError", error.getMessage());
                } else {
                    Log.l("EtcError");
                }
            } catch (Exception e) {
                Log.l("Exception");
            }
        }

        @Override
        public void onResponse(T response) {
            Log.pn(Log.INFO, 1, response);
        }
    }

//    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
//    public static void setCookieHandler() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
//            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
//    }

//    public static void setImageSource(NetworkImageView niv, String url, ImageCache imageCache) {
//        if (url.toLowerCase(Locale.getDefault()).matches("http(|s)://.*")) {
//            niv.setImageUrl(url, new ImageLoader(sRequestQueue, imageCache));
//        }
//    }

//    public static <T extends NetEnty> void async(Request<T> request) {
//        sRequestQueue.add(request);
//    }
}
