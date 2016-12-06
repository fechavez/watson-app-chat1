package com.sample.chatservice;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;


public class MainActivity extends Activity {
    private static final String TAG = "ChatActivity";

    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private String user;
    private String mensajeActivo = "";

    private boolean iniciado = false;
    private boolean side = true;
    private String respuesta = "";

    private String conversation_id;
    private String dialog_turn_counter;
    private String dialog_request_counter;
    private String node_visited;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toPost test = new toPost();
        test.execute();

        setContentView(R.layout.activity_main);

        buttonSend = (Button) findViewById(R.id.send);

        listView = (ListView) findViewById(R.id.msgview);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);
        listView.setAdapter(chatArrayAdapter);

        chatText = (EditText) findViewById(R.id.msg);


        //test.doInBackground();

        //chatArrayAdapter.add(new ChatMessage(!side, respuesta));
        //Log.i("Revisar","Se muestra la respuesta");


        chatText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    clearChatMessage();
            }
        });

        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
    }

    private boolean sendChatMessage() {
        String mensaje = chatText.getText().toString();

        if (!mensaje.equals("")) {
            mensajeActivo = mensaje;
            chatArrayAdapter.add(new ChatMessage(side, chatText.getText().toString()));
            toPost test = new toPost();
            test.execute();
            Log.i("REvisar",respuesta);
            //chatArrayAdapter.add(new ChatMessage(!side, respuesta));
        }

        chatText.setText("");
        //side = !side;
        return true;
    }

    private void clearChatMessage() {
        chatText.setText("");
    }

    private class toPost extends AsyncTask<String, Void, Void>{

        //@Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            Log.i("Watson : ", "\tLoading...");

        }

        @Override
        protected Void doInBackground(String... strings) {
            HttpURLConnection conn = null;
            String requestBody;
            if (iniciado) {
                requestBody = "{\"input\":{\"text\":\""+mensajeActivo+"\"},\"context\":{\"conversation_id\":\""
                        +conversation_id+"\",\"system\":{\"dialog_stack\":[\""+node_visited+"\"],\"dialog_turn_counter\":"
                        +dialog_turn_counter+",\"dialog_request_counter\":"
                        +dialog_request_counter+"},\"default_counter\":0,\"reprompt\": true}}";


            } else {
                requestBody = "";
            }

            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL("http://rimacasesor.mybluemix.net/api/message");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type","application/json");
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();
                OutputStream outputStream = new BufferedOutputStream(conn.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
                Log.i("Request ",requestBody);
                writer.write(requestBody);
                writer.flush();
                writer.close();
                outputStream.close();

                //Create JSONObject here
                JSONObject jsonParam = new JSONObject();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }

                br.close();

                Log.i("Response", "" + sb.toString());

                String responseText = sb.toString();


                //This is needed
                // Could alternatively use conn.getResponseMessage() or conn.getInputStream()
                Log.i("Response : ", "" + conn.getContentType());
                Log.i("Response : ", "" + conn.getResponseCode());
                respuesta = parserJSonResp(responseText);


                dialog_turn_counter = parserJSonTurnCounter(responseText);
                dialog_request_counter = parserJSonDiagReqCounter(responseText);

                iniciado = true;
                Log.i("JSonDiagReqCounter : ", "" + dialog_turn_counter);
                Log.i("JSonTurnCounter : ", "" + dialog_request_counter);
                conversation_id = parserJSonConversationId(responseText);
                Log.i("ConversationId : ", "" + conversation_id);
                node_visited=parserJSonNode(responseText);
                Log.i("Node Visited : ", "" + node_visited);

                //return parserJSon(responseText);
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            chatArrayAdapter.add(new ChatMessage(!side, respuesta));
            Log.i("Revisar","Se muestra la respuesta");
        }

        public String parserJSonResp(String input) {
            try {
                // Convert String to json object
                JSONObject json = new JSONObject(input);

                // get output json object
                JSONObject output = json.getJSONObject("output");

                // get value from LL Json Object
                //String text = output.getString("text"); //<< get value here

                JSONArray texts = output.getJSONArray("text"); //<< get value here
                String response;
                int i =0;
                do {
                    response = texts.getString(i);
                    Log.i("Revisar json - ",response );
                    i++;
                }while(response.equals("") && i<texts.length());
                return response;

            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }


         public String parserJSonConversationId(String input) {
            try {
                // Convert String to json object
                JSONObject json = new JSONObject(input);

                // get output json object
                JSONObject context = json.getJSONObject("context");
                String conversation_id;
                conversation_id = context.getString("conversation_id");
                Log.i("Revisar json - ", conversation_id);
                return conversation_id;

            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }

        public String parserJSonTurnCounter(String input) {
            try {
                // Convert String to json object
                JSONObject json = new JSONObject(input);

                // get output json object
                JSONObject context = json.getJSONObject("context");
                JSONObject system = context.getJSONObject("system");
                String dialog_turn_counter;
                dialog_turn_counter = system.getString("dialog_turn_counter");
                return dialog_turn_counter;

            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }

        public String parserJSonDiagReqCounter(String input) {
            try {
                // Convert String to json object
                JSONObject json = new JSONObject(input);

                // get output json object
                JSONObject context = json.getJSONObject("context");
                JSONObject system = context.getJSONObject("system");
                String dialog_turn_counter;
                dialog_turn_counter = system.getString("dialog_turn_counter");
                return dialog_turn_counter;

            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }

        public String parserJSonNode(String input) {
            try {
                // Convert String to json object
                JSONObject json = new JSONObject(input);

                // get output json object
                JSONObject output = json.getJSONObject("output");
                JSONArray nodes_visited;
                nodes_visited = output.getJSONArray("nodes_visited");
                String node_visited;
                int i =0;

                do {
                    node_visited = nodes_visited.getString(i);
                    Log.i("Revisar json - ",node_visited );
                    i++;
                }while(node_visited.equals("") && i<nodes_visited.length());


                return node_visited;

            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }
    }
}
