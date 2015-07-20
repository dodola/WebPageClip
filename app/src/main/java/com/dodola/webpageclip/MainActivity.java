package com.dodola.webpageclip;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FloatingActionButton mOpenLinkBtn;
    private WebView mWebview;
    private LoadToast lt;
    public static final String DODO_FOLDER_PATH = (Environment.getExternalStorageDirectory() + "/dodo-clip");
    private Rect webViewRect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lt = new LoadToast(this);
        mOpenLinkBtn = (FloatingActionButton) this.findViewById(R.id.open_link_btn);
        mOpenLinkBtn.setOnClickListener(this);
        mWebview = (WebView) findViewById(R.id.h5_web);
        WebSettings webSettings = mWebview.getSettings();
        webSettings.setSupportZoom(false);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        mWebview.setWebChromeClient(new WebChromeClient());
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                lt.success();
            }
        });
        Intent receivedIntent = getIntent();
        String receivedAction = receivedIntent.getAction();
        if (receivedAction.equals(Intent.ACTION_SEND)) {
            String filePathFromActivity = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);


            //提取url
            if (!TextUtils.isEmpty(filePathFromActivity)) {

                final Matcher matcher = Patterns.WEB_URL.matcher(filePathFromActivity);
                while (matcher.find()) {
                    final String group = matcher.group();
                    lt.show();
                    mWebview.loadUrl(group);
                    break;
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            save();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_link_btn: {
                OpenCategroyDialogBox();
            }
            break;
        }
    }

    private void OpenCategroyDialogBox() {

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_input, null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Open url");
        alert.setView(promptView);

        final EditText input = (EditText) promptView
                .findViewById(R.id.dialog_input);

        input.requestFocus();
        input.setHint("Enter web url");
        input.setTextColor(Color.BLACK);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String webUrl = input.getText().toString();
                mWebview.loadUrl(webUrl);
                lt.show();
            }
        });


        alert.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }
        );
        AlertDialog alert1 = alert.create();
        alert1.show();
    }


    private final String saveToFile(Bitmap bitmap, String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        String str = file.getAbsolutePath() + "/" + formatDate(System.currentTimeMillis(), "yyMMdd-HHmmss-SSSS") + ".jpg";
        try {
            OutputStream fileOutputStream = new FileOutputStream(str);
            if (fileOutputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                return str;
            }
            return null;
        } catch (Exception e) {
        }
        return null;
    }

    public static String formatDate(long date, String str) {
        return new SimpleDateFormat(str).format(new Date(date));
    }


    private void save() {

        AsyncTask<Bitmap, Void, String> saveTask = new AsyncTask<Bitmap, Void, String>() {
            @Override
            protected void onPostExecute(String resultFile) {
                super.onPostExecute(resultFile);
                calcLayout(false);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected String doInBackground(Bitmap... params) {
                Bitmap bitmap = params[0];
                String resultFile = null;
                if (bitmap != null)
                    resultFile = saveToFile(bitmap, DODO_FOLDER_PATH);
                return resultFile;
            }
        };
        calcLayout(true);
        int measuredWidth = mWebview.getMeasuredWidth();
        int measuredHeight = mWebview.getMeasuredHeight();
        if (measuredWidth > 0 && measuredHeight > 0) {
            Paint paint = new Paint();
            Bitmap createBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(createBitmap);
            canvas.drawBitmap(createBitmap, 0.0f, (float) createBitmap.getHeight(), paint);
            mWebview.draw(canvas);
            saveTask.execute(createBitmap);

        }

    }


    private void calcLayout(boolean enableDrawingCache) {
        if (this.webViewRect == null || this.webViewRect.isEmpty()) {
            this.webViewRect = new Rect(this.mWebview.getLeft(), this.mWebview.getTop(), this.mWebview.getRight(), this.mWebview.getBottom());
        }
        if (enableDrawingCache) {
            this.mWebview.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            this.mWebview.setDrawingCacheEnabled(true);
            this.mWebview.buildDrawingCache();
        } else {
            this.mWebview.setDrawingCacheEnabled(false);
        }
        this.mWebview.layout(this.webViewRect.left, this.webViewRect.top, this.webViewRect.right, enableDrawingCache ? this.mWebview.getMeasuredHeight() : this.webViewRect.bottom);
    }

    public final void showImage(Context context, String str) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(new File(str)), "image/*");
        context.startActivity(intent);
    }

}
