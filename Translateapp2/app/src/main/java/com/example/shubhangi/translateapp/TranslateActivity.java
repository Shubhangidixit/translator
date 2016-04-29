package com.example.shubhangi.translateapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import com.example.shubhangi.translateapp.R;

import org.grammaticalframework.Linearizer;
import org.grammaticalframework.PGF;
import org.grammaticalframework.PGFBuilder;
import org.grammaticalframework.Parser;
import org.grammaticalframework.intermediateTrees.Tree;
import org.grammaticalframework.parser.ParseState;

import java.io.InputStream;

public class TranslateActivity extends Activity
{

    private ArrayAdapter mArrayAdapter;
    private PGF mPGF;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translate);

        new LoadPGFTask().execute();

        mArrayAdapter = new ArrayAdapter(this, R.layout.listitem);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(mArrayAdapter);
    }


    public void translate(View v) {
       TextView tv = (TextView)findViewById(R.id.edittext);
        String input = tv.getText().toString();
        mArrayAdapter.clear();
        for (int i = 0; i < 10 ; i++)
            mArrayAdapter.add(input);
        new TranslateTask().execute(input);
    }



    // * This class is used to load the PGF file asychronously.
    // * It display a blocking progress dialog while doing so.

    private class LoadPGFTask extends AsyncTask<Void, Void, PGF> {

        private ProgressDialog progress;

        protected void onPreExecute() {
             // Display loading popup

            // kill progress dialog if exists
            this.progress =
                    ProgressDialog.show(TranslateActivity.this,"Translate","Loading grammar, please wait",true);
        }

        protected PGF doInBackground(Void... a) {
            int pgf_res = R.raw.foods;
            InputStream is = getResources().openRawResource(pgf_res);




            try {

                System.out.println(is.toString());
                SystemClock.sleep(10000);



               PGF pgf = PGFBuilder.fromInputStream(is, new String[]{"FoodsEng", "FoodsCat"});
                //SystemClock.sleep(10000);
                return pgf;

            } catch (Exception e) {
                System.out.println("Printing the exception");
                throw new RuntimeException(e);
            }
        }

        protected void onPostExecute(PGF result) {
            mPGF = result;
            //if (this.progress != null)
               // this.progress.dismiss(); // Remove loading popup
        }
    }


    // * This class is used to parse a sentence asychronously.
    // * It display a blocking progress dialog while doing so.

    private class TranslateTask extends AsyncTask<String, Void, String[]> {

        private ProgressDialog progress;

        protected void onPreExecute() {
            // Display loading popup
            this.progress =
                    ProgressDialog.show(TranslateActivity.this,"Translate","Parsing, please wait",true);
        }

        protected String[] doInBackground(String... s) {
            try {
                // Creating a Parser object for the FoodEng concrete grammar
                Parser mParser = new Parser(mPGF, "FoodsEng");
                // Spliting the input (basic tokenization)
                SystemClock.sleep(10000);

                String[] tokens = s[0].split(" ");
                // parsing the tokens
                ParseState mParseState = mParser.parse(tokens);
                org.grammaticalframework.Trees.Absyn.Tree[] trees = mParseState.getTrees();

                String[] translations = new String[trees.length];
                // Creating a Linearizer object for the FoodCat concrete grammar
                Linearizer mLinearizer = new Linearizer(mPGF, "FoodsCat");
                // Linearizing all the trees
                for (int i = 0 ; i < trees.length ; i++) {
                    try {
                        String t = mLinearizer.linearizeString(trees[i]);
                        translations[i] = t;
                    } catch (java.lang.Exception e) {
                        translations[i] = "/!\\ Linearization error";
                    }
                }
                return translations;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected void onPostExecute(String[] result) {
            mArrayAdapter.clear();
            for (String sentence : result)
                mArrayAdapter.add(sentence);
            if (this.progress != null)
                this.progress.dismiss(); // Remove loading popup
        }
    }
}



