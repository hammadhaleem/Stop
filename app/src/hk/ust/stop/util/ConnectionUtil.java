package hk.ust.stop.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class ConnectionUtil {
    
	/**
	 * Connect Client with Server, send json to server and get response from server
	 * @param staticUrl : complete Connect Url
	 * @param sendJsonMsg : json sending to Server
	 * @return
	 */
	public static String post2Server(String staticUrl, String sendJsonMsg) {

		URL url = null;
		
		try {
			url = new URL(staticUrl);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();// open HttpURLConnection
			// set http header
			urlConn.setDoInput(true); // set iostream
			urlConn.setDoOutput(true); // set iostream
			urlConn.setRequestMethod("POST"); // Post not use cache
			urlConn.setConnectTimeout(10000); // timeout
			urlConn.setUseCaches(false);
			urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			urlConn.setRequestProperty("Charset", "utf-8"); // character encoding
			urlConn.connect(); // build up connection while not sending request
			// write http content
			OutputStream outputStream = urlConn.getOutputStream();
			String content = sendJsonMsg;
			outputStream.write(content.getBytes());
			outputStream.flush(); // flush
			outputStream.close(); // close iostream

			if (urlConn.getResponseCode() == 200) {
				// read server feedback
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));// push Http request
				String retData = null;
				String responseData = "";
				while ((retData = in.readLine()) != null) {
					responseData += retData;
				}

				in.close();// close iostream
				urlConn.disconnect();// disconnect

				return responseData;

			} else {
				Log.i("json", "Network Error");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "NoMessage";

	}

	/**
	 * Connect Client with Server, get json from server
	 * @param staticUrl : complete url
	 * @return Json response
	 */
	public static String getFromServer(String staticUrl) {
		URL url = null;
		try {
			url = new URL(staticUrl);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();// open connection with HttpURLConnection
			// read server feedback
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));// push Http request
			String retData = null;
			String responseData = "";
			while ((retData = in.readLine()) != null) {
				responseData += retData;
			}
			in.close();// close iostream
			urlConn.disconnect();// disconnect

			return responseData;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "NoMessage";
	}
	
	
	@SuppressWarnings("deprecation")
	public static void uploadFile(Bitmap bm,String fileName)
    {
		try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bm.compress(CompressFormat.JPEG, 100, bos);
            byte[] data = bos.toByteArray();
            HttpClient httpClient = new DefaultHttpClient();
            
            // Set the url for uploading picture
            HttpPost postRequest = new HttpPost(
                    "http://demo.engineerinme.com:5000/upload");
            
            ByteArrayBody bab = new ByteArrayBody(data,"image/png",fileName+".jpg");
            MultipartEntity reqEntity = new MultipartEntity(
            HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("file", bab);
            
            postRequest.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();
 
            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }
            System.out.println("Response: " + s);
        } catch (Exception e) {
            // handle exception here
            Log.e(e.getClass().getName(), e.getMessage());
        }
    }
	
	
	private static DefaultHttpClient getMultithreadClient(){
		HttpParams params = new BasicHttpParams();
        // Set some basic parameter
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params,"UTF-8");
        HttpProtocolParams.setUseExpectContinue(params, true);
        HttpProtocolParams
                .setUserAgent(
                        params,
                        "Mozilla/5.0(Linux;U;Android 2.3.1;en-us;Nexus One Build.FRG83) "
                                + "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
        
        /* Set timeout for connection pool */
        ConnManagerParams.setTimeout(params, 10000);
        // Set the maximum number of connection
        ConnManagerParams.setMaxTotalConnections(params, 200);
        HttpConnectionParams.setConnectionTimeout(params, 2000);
        /* Set timeout for request */
        HttpConnectionParams.setSoTimeout(params, 15000);
      
        // Set the HttpClient to support HTTP and HTTPS
        SchemeRegistry schReg = new SchemeRegistry();
        // Since the server use port 5000, so that set the port to 5000
        schReg.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 5000));
        schReg.register(new Scheme("https", SSLSocketFactory
                .getSocketFactory(), 443));

        // Use a thread safe manager to create HttpClient
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
                params, schReg);
        return new DefaultHttpClient(conMgr, params);
	}
	
	
	/**
	 * Download all the pictures in the list
	 * @param urls
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Bitmap> getBitmaps(ArrayList<String> urls) throws Exception {
		// This list would store the bitmap that received from server
		ArrayList<Bitmap> pictures = new ArrayList<Bitmap>();
		HttpClient httpclient = getMultithreadClient();
		
		for(int i = 0; i < urls.size(); i++){
			HttpGet httpRequest = new HttpGet(urls.get(i));
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			// If the operation is execute successfully, 
			// then get the bitmaps from stream
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){    
                HttpEntity httpEntity = httpResponse.getEntity();    
                InputStream is = httpEntity.getContent(); 
                // Change the inputstream to bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(is);    
                is.close();   
                pictures.add(bitmap);
            }else{    
            	new Exception("Fail to connect!");        
            }
		}
		return pictures;
	}
}
