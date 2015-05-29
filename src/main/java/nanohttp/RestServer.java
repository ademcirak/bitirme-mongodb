package nanohttp;

import clustering.Clustering;
import nanohttp.NanoHTTPD;

import java.util.Map;

/**
 * Created by Adem on 20/5/2015.
 */
public class RestServer extends NanoHTTPD {

    public RestServer() {
        super(9999);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();

        String msg = Clustering.getInstance().getJSON().toString();
        Response res =  newFixedLengthResponse(msg);
        res.addHeader("Content-Type","application/json; charset=utf-8");
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        return res;
    }
}
