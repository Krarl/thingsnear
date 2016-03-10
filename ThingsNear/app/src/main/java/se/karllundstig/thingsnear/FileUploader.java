package se.karllundstig.thingsnear;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class FileUploader {
    private FileUploader() {}

    //Hämtad från http://stackoverflow.com/a/11826317
    public static String sendFile(String url, String file, String token, String name, String mimetype) throws Exception
    {
        //Små strängar vi kommer behöva
        String fileName = file.substring(file.lastIndexOf('/') + 1);
        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        //Setup the request
        HttpURLConnection httpUrlConnection;
        URL remoteUrl = new URL(url);
        httpUrlConnection = (HttpURLConnection)remoteUrl.openConnection();
        httpUrlConnection.setUseCaches(false);
        httpUrlConnection.setDoOutput(true);

        httpUrlConnection.setRequestMethod("POST");
        httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
        httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
        httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        httpUrlConnection.setRequestProperty("x-access-token", token);

        DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());

        //Start content wrapper
        request.writeBytes(twoHyphens + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"" + name + "\";filename=\"" + fileName + "\"" + crlf);
        request.writeBytes("Content-Type: " + mimetype + crlf);
        request.writeBytes(crlf);

        //Läser in filen och skickar den
        FileInputStream fileStream = new FileInputStream(file);
        request.write(fileStream.read());

        final FileInputStream inputStream = new FileInputStream(file);
        final byte[] buffer = new byte[4096];
        int bytesRead;
        inputStream.read(); //annars skickas en byte för mycket, don't know why
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            request.write(buffer, 0, bytesRead);
        }

        //End content wrapper
        request.writeBytes(crlf);
        request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

        //Flush
        request.flush();
        request.close();

        //Get response
        InputStream responseStream = new BufferedInputStream(httpUrlConnection.getInputStream());
        BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

        String line;
        StringBuilder stringBuilder = new StringBuilder();

        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();

        String response = stringBuilder.toString();
        responseStream.close();
        httpUrlConnection.disconnect();

        return response;
    }
}
