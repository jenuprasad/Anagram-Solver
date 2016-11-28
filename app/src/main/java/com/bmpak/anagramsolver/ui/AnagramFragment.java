package com.bmpak.anagramsolver.ui;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bmpak.anagramsolver.R;
import com.bmpak.anagramsolver.dictionary.Dictionary;
import com.bmpak.anagramsolver.utils.AnagramTextWatcher;
import com.bmpak.anagramsolver.utils.AppPrefs;
import com.bmpak.anagramsolver.utils.ViewUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnagramFragment extends Fragment implements AnagramTextWatcher.OnWordsFound {

    private Realm realm;

    public AnagramFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        realm = Realm.getInstance(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionbar = ((MainActivity) getActivity()).getSupportActionBar();
        actionbar.show();
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setCustomView(R.layout.actionbar_view);
    }

    @InjectView(R.id.swipeContainer)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @InjectView(R.id.languages)
    LinearLayout languages;

    @InjectView(R.id.inputWord)
    EditText inputWordET;

    @InjectView(R.id.anagramsListView)
    ListView anagramsLV;

    @InjectView(R.id.reportStatus)
    TextView reportStatus;

    @InjectView(R.id.result_section)
    LinearLayout resultSection;


    /**
     * {@link com.bmpak.anagramsolver.utils.AnagramTextWatcher} instance.
     */
    private AnagramTextWatcher anagramTextWatcher;

    /* helper View for languages initialization */
    private View currentDict;

    /* ListView's adapter */
    private ArrayAdapter<String> wordListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_anagram, container, false);
        ButterKnife.inject(this, rootView);


        SharedPreferences prefs = getActivity()
                .getSharedPreferences(AppPrefs.MAIN_PREF_NAME, Context.MODE_PRIVATE);

        //sets doesn't have an order
        Set<String> installedDictsSet = prefs.getStringSet(
                AppPrefs.DICTIONARIES_INSTALLED, new HashSet<String>());

        //convert to array to have a specific order in layout
        String[] installedDictsArray = installedDictsSet.toArray(new String[installedDictsSet.size()]);
        Arrays.sort(installedDictsArray);

        //initialize language section
        initLanguages(installedDictsArray);

        wordListAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.listview_item, R.id.list_content);
        anagramsLV.setAdapter(wordListAdapter);


        anagramTextWatcher = new AnagramTextWatcher(this, realm, wordListAdapter);
        anagramTextWatcher.setSwipeRefreshLayout(mSwipeRefreshLayout);

        //set the first dictionary
        anagramTextWatcher.setDictionary(installedDictsArray[0]);
        inputWordET.addTextChangedListener(anagramTextWatcher);
        inputWordET.setTypeface(
                Typeface.createFromAsset(getResources().getAssets(),
                        "font/bebas.ttf"), Typeface.BOLD
        );
        inputWordET.setFilters(new InputFilter[]{new InputFilter.AllCaps()});


        //http://nlopez.io/swiperefreshlayout-with-listview-done-right/
        anagramsLV.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                //nothing to do
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                int topRowVerticalPosition =
                        (anagramsLV == null || anagramsLV.getChildCount() == 0) ?
                                0 : anagramsLV.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
            }
        });


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                anagramTextWatcher.scrabble();
                Toast.makeText(getActivity(), R.string.scrabbling, Toast.LENGTH_SHORT).show();
            }
        });

        mSwipeRefreshLayout.setColorSchemeColors(
                R.color.grey_flag,
                R.color.dark_blue_flag,
                R.color.blue_flag,
                R.color.red_flag
        );

        return rootView;
    }

    /**
     * Gets the installed dictionaries and adds ImageButtons dynamically in LinearLayout.
     * <p/>
     * It also initialize them with onClick events.
     */
    private void initLanguages(String[] installedDicts) {
        for (String lang : installedDicts) {
            ImageButton languageImageButton = new ImageButton(getActivity());
            languageImageButton.setBackgroundColor(Color.TRANSPARENT);

            languageImageButton.setImageDrawable(getActivity().getResources().getDrawable(Dictionary.getDrawableId(lang)));

            //set layout parameters
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //weight set to 1 to have same distance between languages..
            params.weight = 1;

            languageImageButton.setLayoutParams(params);

            //set language to Tag
            languageImageButton.setTag(lang);

            languageImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(view != currentDict) {
                        //send singal to change the parser
                        String dictionarySelected = (String) view.getTag();
                        anagramTextWatcher.dictionaryChange(dictionarySelected);

                        //make previous selected language semi transparent
                        ViewUtils.setAlpha(currentDict, ViewUtils.DEFAULT_SEMI_ALPHA, 0);

                        //set clicked language as current dictionary and set to full alpha
                        currentDict = view;
                        ViewUtils.setAlpha(view, ViewUtils.DEFAULT_FULL_ALPHA, 0);
                    }
                }
            });

            if (currentDict == null) {
                currentDict = languageImageButton;
                ViewUtils.setAlpha(languageImageButton, ViewUtils.DEFAULT_FULL_ALPHA, 0);
            } else {
                ViewUtils.setAlpha(languageImageButton, ViewUtils.DEFAULT_SEMI_ALPHA, 0);
            }


            //add to languages section
            languages.addView(languageImageButton);
        }
    }



    @Override
    public void onWordsFound(int wordsCount) {

        //flag to remain the right and left lines
        if (wordsCount == -1) {
            resultSection.setVisibility(View.INVISIBLE);
            reportStatus.setText("");
            reportStatus.setVisibility(View.INVISIBLE);
        } else {
            resultSection.setVisibility(View.VISIBLE);
            reportStatus.setVisibility(View.VISIBLE);
            if (wordsCount == 0) {
                reportStatus.setText(getResources().getString(R.string.not_found));
            } else {
                reportStatus.setText(
                        String.format(getResources().getString(R.string.found), wordsCount)
                );
            }
        }
    }


}
