////////////////////////////////////////////////////////////////////////////////
//
//  Print - print text and HTML
//
//  Copyright (C) 2019	Bill Farmer
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
////////////////////////////////////////////////////////////////////////////////

package org.billthefarmer.print;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.autolink.AutolinkExtension;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.lang.ref.WeakReference;

import java.nio.charset.Charset;

import java.text.DateFormat;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Print extends Activity
{
    public static final String TAG = "Print";

    public static final String HTML_HEAD =
        "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"utf-8\">\n" +
        "<meta name=\"viewport\" content=\"width=device-width, " +
        "initial-scale=1.0\">\n</head>\n<body>\n";
    public static final String HTML_TAIL = "\n</body>\n</html>\n";
    public static final String ANDROID_ASSET = "file:///android_asset/";
    public static final String UTF_8 = "utf-8";

    public static final String TEXT_PLAIN = "text/plain";
    public static final String TEXT_HTML = "text/html";
    public static final String TEXT_WILD = "text/*";

    public static final String ASSET_URL =
        "file:///android_asset/print.html";

    private static final int OPEN_DOCUMENT = 1;

    private WebView webView;
    private ProgressBar progress;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        webView = findViewById(R.id.webview);
        progress = findViewById(R.id.progress);

        if (webView != null)
        {
            // Enable javascript, web sites don't work unless JavaScript
            // is enabled
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);

            // Enable zoom
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);

            // Follow links and set title
            webView.setWebViewClient(new WebViewClient()
            {
                // onPageFinished
                @Override
                public void onPageFinished(WebView view, String url)
                {
                    // Remove progress
                    progress.setVisibility(View.GONE);

                    // Get page title
                    if (URLUtil.isNetworkUrl(url) && view.getTitle() != null)
                        setTitle(view.getTitle());

                    else
                        setTitle(R.string.appName);

                    if (view.canGoBack())
                        getActionBar().setDisplayHomeAsUpEnabled(true);

                    else
                        getActionBar().setDisplayHomeAsUpEnabled(false);
                }
            });

            if (savedInstanceState != null)
                // Restore state
                webView.restoreState(savedInstanceState);

            else
            {
                Intent intent = getIntent();
                switch (intent.getAction())
                {
                case Intent.ACTION_VIEW:
                {
                    // Get uri
                    Uri uri = intent.getData();
                    if (uri != null)
                        readFile(uri);
                    break;
                }

                case Intent.ACTION_SEND:
                {
                    // Get uri
                    Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    String html = intent.getStringExtra(Intent.EXTRA_HTML_TEXT);
                    String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if (uri != null)
                        readFile(uri);

                    else if (html != null)
                    {
                        if (URLUtil.isNetworkUrl(html))
                            webView.loadUrl(html);

                        else
                            loadText(html);
                    }

                    else if (text != null)
                    {
                        if (URLUtil.isNetworkUrl(text))
                            webView.loadUrl(text);

                        else
                            loadText(text);
                    }
                    break;
                }
                    default:
                        webView.loadUrl(ASSET_URL);
                }
            }
        }
    }

    // On save instance state
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        if (webView != null)
            // Save state
            webView.saveState(outState);
    }

    // On create option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it
        // is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // On options item
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Get id
        int id = item.getItemId();
        switch (id)
        {
            // Home
        case android.R.id.home:
            // Back navigation
            if (webView != null && webView.canGoBack())
                webView.goBack();

            else
                finish();
            break;

            // Print
        case R.id.action_print:
            print();
            break;

            // Open
        case R.id.action_open:
            open();
            break;

            // About
        case R.id.action_about:
            about();
            break;

        default:
            return false;
        }

        return true;
    }

    // On back pressed
    @Override
    public void onBackPressed()
    {
        // Back navigation
        if (webView != null && webView.canGoBack())
            webView.goBack();

        else
            finish();
    }

    // onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        if (resultCode == RESULT_CANCELED)
            return;

        switch (requestCode)
        {
        case OPEN_DOCUMENT:
            Uri uri = data.getData();
            readFile(uri);
            break;
        }
    }

    // dispatchKeyEvent
    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        // Check Ctrl key
        if (event.isCtrlPressed())
        {
            switch (event.getAction())
            {
            case KeyEvent.ACTION_DOWN:
                switch (event.getKeyCode())
                {
                    // Open
                case KeyEvent.KEYCODE_O:
                    open();
                    break;

                    // Print
                case KeyEvent.KEYCODE_P:
                    print();
                    break;
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    // readFile
    private void readFile(Uri uri)
    {
        // Show progress
        progress.setVisibility(View.VISIBLE);

        String url = uri.toString();
        if (URLUtil.isContentUrl(url))
        {
            ReadTask readTask = new ReadTask(this);
            readTask.execute(uri);
        }

        else
            webView.loadUrl(url);
    }

    private void open()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType(TEXT_WILD);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, OPEN_DOCUMENT);
    }

    // loadText
    private void loadText(String text)
    {
        // Use commonmark
        List<Extension> extensions =
            Arrays.asList(AutolinkExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        Node document = parser.parse(text);
        HtmlRenderer renderer = HtmlRenderer.builder()
            .extensions(extensions).build();

        String html = renderer.render(document);

        webView.loadDataWithBaseURL(ANDROID_ASSET,
                                    HTML_HEAD + html + HTML_TAIL,
                                    TEXT_HTML, UTF_8, null);
    }

    // print
    private void print()
    {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager)
            getSystemService(PRINT_SERVICE);

        String jobName = getString(R.string.appName) + " Document";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter =
            webView.createPrintDocumentAdapter(jobName);

        // Create a print job with name and adapter instance
        printManager.print(jobName, printAdapter,
                           new PrintAttributes.Builder()
                           .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                           .build());
    }

    // about
    private void about()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.appName);

        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        SpannableStringBuilder spannable =
            new SpannableStringBuilder(getText(R.string.version));
        Pattern pattern = Pattern.compile("%s");
        Matcher matcher = pattern.matcher(spannable);
        if (matcher.find())
            spannable.replace(matcher.start(), matcher.end(),
                              BuildConfig.VERSION_NAME);
        matcher.reset(spannable);
        if (matcher.find())
            spannable.replace(matcher.start(), matcher.end(),
                              dateFormat.format(BuildConfig.BUILT));
        builder.setMessage(spannable);

        // Add the button
        builder.setPositiveButton(android.R.string.ok, null);

        // Create the AlertDialog
        Dialog dialog = builder.show();

        // Set movement method
        TextView text = dialog.findViewById(android.R.id.message);
        if (text != null)
            text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // alertDialog
    private void alertDialog(int title, String message, int neutralButton)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        // Add the buttons
        builder.setNeutralButton(neutralButton, null);

        // Create the AlertDialog
        builder.show();
    }

    // ReadTask
    private static class ReadTask extends AsyncTask<Uri, Void, CharSequence>
    {
        WeakReference<Print> printWeakReference;

        ReadTask(Print print)
        {
            printWeakReference = new WeakReference<Print>(print);
        }

        @Override
        protected CharSequence doInBackground(Uri uris[])
        {
            StringBuilder stringBuilder = new StringBuilder();
            final Print print = printWeakReference.get();
            if (print == null)
                return stringBuilder;

            try (BufferedInputStream in = new BufferedInputStream
                 (print.getContentResolver().openInputStream(uris[0])))
            {
                BufferedReader reader = new
                    BufferedReader(new InputStreamReader(in));

                CharsetMatch match = new CharsetDetector().setText(in).detect();
                if (match != null)
                    reader = new BufferedReader(match.getReader());

                if (BuildConfig.DEBUG && match != null)
                    Log.d(TAG, "Charset " + match.getName());

                String line;
                while ((line = reader.readLine()) != null)
                {
                    stringBuilder.append(line);
                    stringBuilder.append(System.getProperty("line.separator"));
                }
            }

            catch (Exception e)
            {
                print.runOnUiThread(() ->
                    print.alertDialog(R.string.appName,
                                      e.getMessage(),
                                      android.R.string.ok));
                e.printStackTrace();
            }

            return stringBuilder;
        }

        // onPostExecute
        @Override
        protected void onPostExecute(CharSequence result)
        {
            final Print print = printWeakReference.get();
            if (print == null)
                return;

            print.loadText(result.toString());
        }
    }
}
