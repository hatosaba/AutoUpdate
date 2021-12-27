package si.f5.hatosaba.autoupdate.utils;

import com.github.kevinsawicki.http.HttpRequest;
import si.f5.hatosaba.autoupdate.AutoUpdate;

import java.io.File;
import java.util.concurrent.TimeUnit;

public abstract class HttpUtil {

    private static String accessToken = AutoUpdate.getInstance().getMainConfig().getAccessToken();

    private static HttpRequest setTimeout(HttpRequest httpRequest) {
        return httpRequest
                .connectTimeout(Math.toIntExact(TimeUnit.SECONDS.toMillis(30)))
                .readTimeout(Math.toIntExact(TimeUnit.SECONDS.toMillis(30)));
    }

    public static String requestHttp(String requestUrl, boolean isPrivate) {
        try {
            String body;
            if(isPrivate) {
                body = setTimeout(HttpRequest.get(requestUrl).authorization(String.format("token %s", accessToken))).body();
            }else {
                body = setTimeout(HttpRequest.get(requestUrl)).body();
            }
            return body;
        } catch (HttpRequest.HttpRequestException e) {
            return "";
        }
    }

    public static void downloadFile(String requestUrl, File destination, boolean isPrivate) {
        try {
            if(isPrivate) {
                setTimeout(HttpRequest.get(requestUrl).authorization(String.format("token %s", accessToken)).accept("application/octet-stream")).receive(destination);
            }else {
                setTimeout(HttpRequest.get(requestUrl)).accept("application/octet-stream").receive(destination);
            }
        } catch (HttpRequest.HttpRequestException e) { }
    }

    public static boolean exists(String url) {
        try {
            HttpRequest request = setTimeout(HttpRequest.head(url));
            return request.code() / 100 == 2;
        } catch (Exception e) {
            return false;
        }
    }

}
