package android.volley;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

public class Log {

    public static void w(StackTraceElement stack, Object... args) {
    }
    public static void e(StackTraceElement stack, Object... args) {
    }
    public static void i(StackTraceElement stack, Object... args) {
    }
    public static void printStackTrace(Exception e) {
    }

    //debug//////////////////////////////////////////////////////////////////////////////////////////////
    public static StackTraceElement getStackOveride(String method_name) {
        return null;
    }
    static StackTraceElement getStack() {
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
    static void _NETLOG_OUT(NetEnty mEnty) {
        if (!Net.LOG)
            return;

        try {
            final StackTraceElement stack = mEnty.mCallerStack;
            if (mEnty._OUT_1 || mEnty._OUT_2 || mEnty._OUT_3 || Net._OUT_1 || Net._OUT_2 || Net._OUT_3) {
                Log.e(stack, "나가:", mEnty.toString());
            }

            if (mEnty._OUT_2 || mEnty._OUT_3 || Net._OUT_2 || Net._OUT_3) {
                if (mEnty.params != null) {
                    final StringBuilder sb = new StringBuilder("\n");
                    final Set<Map.Entry<String, Object>> set = mEnty.params.entrySet();
                    for (Map.Entry<String, Object> entry : set) {
                        if (entry.getValue() == null)
                            continue;
                        sb.append("  ").append(entry.getKey()).append(SPACE.substring(Math.min(SPACE.length(), entry.getKey().length())));
                        sb.append(" = ").append(entry.getValue());
                        sb.append("\n");
                    }
                    Log.e(stack, sb);
                }
            }

            if (mEnty._OUT_3 || Net._OUT_3) {
                Log.e(stack, new String(mEnty.getBody()));
            }

            if (mEnty._OUT_H || Net._OUT_H) {

                StringBuilder sb = new StringBuilder("\n");
                final Set<Map.Entry<String, String>> set = mEnty.headers.entrySet();
                for (Map.Entry<String, String> entry : set) {
                    sb.append("\t").append(entry.getKey()).append(": ").append(prettyJson(entry.getValue())).append("\n");
                }

                Log.e(stack, "나해:", mEnty.getClass().getSimpleName(), sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void _NETLOG_IN(NetEnty mEnty, Response.Listener<?> listener, NetworkResponse response) {
        if (!Net.LOG)
            return;

        try {
            StackTraceElement stack = null;
            if (mEnty._IN_H || Net._IN_H) {
                StringBuilder sb = new StringBuilder("\n");
                final Set<Map.Entry<String, String>> set = response.headers.entrySet();
                for (Map.Entry<String, String> entry : set) {
                    sb.append("\t").append(entry.getKey()).append(": ").append(prettyJson(entry.getValue())).append("\n");
                }
                Log.w(stack, "들해:", mEnty.getClass().getSimpleName(), sb.toString());
            }

            if (mEnty._IN_1 || Net._IN_1 || mEnty._IN_2 || Net._IN_2) {
                Log.i(stack, "들와:", response.statusCode, mEnty);
            }
            if (mEnty._IN_2 || Net._IN_2) {
                Log.i(stack, "들와:", response.statusCode, new String(response.data, HttpHeaderParser.parseCharset(response.headers, Net.UTF8)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void _NETLOG_IN(NetEnty mEnty, Response.Listener<?> listener, VolleyError volleyError) {
        if (!Net.LOG)
            return;
        try {
            StackTraceElement stack = null;
            if (mEnty._IN_1 || Net._IN_1 || mEnty._IN_2 || Net._IN_2 || mEnty._IN_3 || Net._IN_3) {
                Object statusCode = null;
                try {
                    statusCode = volleyError.networkResponse.statusCode;
                } catch (Exception e) {
                    statusCode = volleyError.getMessage();
                }
//                Log.e("!들와:", mEnty, "" + statusCode);
                Log.e(stack, "!들와:", mEnty, "" + statusCode);
            }
            if (mEnty._IN_2 || Net._IN_2 || mEnty._IN_3 || Net._IN_3) {
                Log.e(stack, "!들와1/5:", "" + volleyError.getClass().getSimpleName());
                Log.e(stack, "!들와2/5:", "" + volleyError.getMessage());
                Log.e(stack, "!들와3/5:", "" + volleyError.networkResponse);
                try {
                    Log.e(stack, "!들와4/5:", "" + volleyError.networkResponse.statusCode);
                } catch (Exception e) {
                }
                try {
                    Log.e(stack, "!들와5/5:", prettyJson(new String(volleyError.networkResponse.data, HttpHeaderParser.parseCharset(volleyError.networkResponse.headers, Net.UTF8))));
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