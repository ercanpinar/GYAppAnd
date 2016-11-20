package com.turkcell.gelecegiyazanlar.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.turkcell.gelecegiyazanlar.R;
import com.turkcell.gelecegiyazanlar.activities.AramaActivity;
import com.turkcell.gelecegiyazanlar.activities.ProfilActivity;
import com.turkcell.gelecegiyazanlar.adapterlisteners.KullaniciAramaAdapter;
import com.turkcell.gelecegiyazanlar.configurations.AppController;
import com.turkcell.gelecegiyazanlar.configurations.GYConfiguration;
import com.turkcell.gelecegiyazanlar.models.Icerik;
import com.turkcell.gelecegiyazanlar.models.Kisi;
import com.turkcell.gelecegiyazanlar.utilities.YuklenmeEkran;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class AramaFragment extends Fragment implements View.OnClickListener, AramaActivity.IArama {

    EditText searchEditText;

    ImageView btnAra;
    ListView listView;
    TextView sonucTextView;

    Kisi tempKisi;
    //Volley değişkenleri
    JsonArrayRequest jsonArrayRequest, jsonArrayRequestIcerik;
    JsonObjectRequest jsonObjectRequest;
    RequestQueue requestQueue;
    ImageRequest imageRequest;
    String urlAramaKullanici, urlIcerik;
    TabLayout tabs;

    List<Icerik> icerikList = new ArrayList<Icerik>();
    List<Kisi> kisiList = new ArrayList<Kisi>();

    int sayfaNumarasi = 1;

    YuklenmeEkran ekran;
    ViewPager viewPager;
    Toolbar toolbar;

    public AramaFragment() {
        // Required empty public constructor
        Log.d("arama:", "Kullanıcı");
    }

    public static AramaFragment newInstance() {
        return new AramaFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewPager = (ViewPager) getActivity().findViewById(R.id.viewPagerPagerArama);
        toolbar = (Toolbar) getActivity().findViewById(R.id.tool_bar_ara);
        View rootView = inflater.inflate(R.layout.fragment_arama, container, false);
        urlAramaKullanici = GYConfiguration.getDomain() + "usersearch/retrieve?keyword=";
        urlIcerik = GYConfiguration.getDomain() + "contentsearch/retrieve?";

        searchEditText = (EditText) toolbar.findViewById(R.id.editTextSearch);

        listView = (ListView) rootView.findViewById(R.id.lvliste);

        sonucTextView = (TextView) rootView.findViewById(R.id.tvSonuc);
        btnAra = (ImageView) toolbar.findViewById(R.id.imageViewSearchBtn);

        ekran = new YuklenmeEkran(getActivity());


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (kisiList.get(position) != null) {
                    Kisi kisi = new Kisi();
                    kisi.setKullaniciAdi(kisiList.get(position).getKullaniciID());
                    Intent i = new Intent(getActivity(), ProfilActivity.class);
                    i.putExtra(Kisi.PROFIL_ID, kisi.getKullaniciAdi());
                    startActivity(i);
                }
            }
        });

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });


        return rootView;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageViewSearchBtn:
                performSearch();
                break;
        }
    }

    private void performSearch() {
        String link = (urlAramaKullanici + searchEditText.getText()).trim();
        link = link.replace(" ", "%20");
        kisiList.clear();
        Listele(link);
    }

    public void Listele(String url) {

        ekran.surecBasla();


        jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                ekran.surecDurdur();


                for (int i = 0; i < response.length(); i++) {

                    final Kisi tempKisi = new Kisi();
                    try {
                        if (response.getJSONObject(i).getString("adSoyad") != "null") {
                            tempKisi.setAdSoyad(response.getJSONObject(i).getString("adSoyad"));
                            tempKisi.setKullaniciAdi(response.getJSONObject(i).getString("kullaniciAdi"));
                            tempKisi.setKullaniciID(response.getJSONObject(i).getString("kullaniciID"));
                            tempKisi.setKullaniciAvatarUrl(response.getJSONObject(i).getString("kullaniciAvatarUrl"));
                            Log.d("TAG", "onResponse: " + tempKisi.getKullaniciAvatarUrl());

                            kisiList.add(tempKisi);


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("TAG", e.toString());
                    }

                }

                KullaniciAramaAdapter adapter = new KullaniciAramaAdapter(getActivity(), kisiList);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();


                Log.d("parse islemi bitti:", "girildi");
                if (kisiList.isEmpty())
                    sonucTextView.setVisibility(View.VISIBLE);
                else sonucTextView.setVisibility(View.GONE);

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });


        AppController.getInstance().addToRequestQueue(jsonArrayRequest);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);


    }


    @Override
    public void onPageActivated() {
        btnAra.setOnClickListener(this);
    }
}



