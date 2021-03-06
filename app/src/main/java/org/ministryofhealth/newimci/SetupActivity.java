package org.ministryofhealth.newimci;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.firebase.iid.FirebaseInstanceId;

import org.ministryofhealth.newimci.app.AppController;
import org.ministryofhealth.newimci.database.DatabaseHandler;
import org.ministryofhealth.newimci.helper.RetrofitHelper;
import org.ministryofhealth.newimci.model.County;
import org.ministryofhealth.newimci.model.UserProfile;
import org.ministryofhealth.newimci.receiver.ConnectivityReceiver;
import org.ministryofhealth.newimci.server.Service.UserProfileService;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SetupActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{
    Spinner ageSpinner, countySpinner, cadreSpinner, professionSpinner;
    EditText etxEmail, etxPhone;
    RadioGroup rgGender, rgSector;
    DatabaseHandler db;
    List<County> countyList;
    Context context;
    TextView txtSkip;
    Button btnSubmit;
    Boolean page;
    int user_id;
    int app_user_id;
    AwesomeValidation mAwesomeValidation;
    static SharedPreferences pref;
    TableLayout enteredDataTable, formTable;
    TableRow cadreRow;

    LinearLayout countyLayout,cadreLayout;

    Button btnGender, btnAgeGroup, btnCountry, btnCounty, btnProfession, btnSector, btnCadre, btnCreateProfile;

    EditText editEmail, editPhone;

    int selected_gender = -1;
    int selected_age = -1;
    int selected_profession = -1;
    int selected_sector = -1;
    int selected_cadre = -1;

    Boolean checkUploaded = false;
    Boolean forResult = false;

    String selected_gender_ = "";
    String selected_age_ = "";
    String selected_profession_ = "";
    String selected_sector_ = "";
    String selected_cadre_ = "";

    String country_code = "";
    String country_name = "";

    String county_name = "";

    private static final int COUNTRY_LIST_CODE = 100;
    private static final int COUNTY_LIST_CODE = 101;

//    SharedPreferences pref = getSharedPreferences("user_details", Context.MODE_PRIVATE);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        if (getCallingActivity() == null){
            forResult = false;
        }else{
            forResult = true;
        }

        pref = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        user_id = pref.getInt("id", 0);
        checkUploaded = pref.getBoolean("uploaded", false);


        TextView informationText = (TextView) findViewById(R.id.information);

        countyLayout = (LinearLayout) findViewById(R.id.county_layout);
        cadreLayout = (LinearLayout) findViewById(R.id.cadre_layout);

        btnCreateProfile = findViewById(R.id.create_profile);
        btnGender = (Button) findViewById(R.id.spn_gender);
        btnAgeGroup = (Button) findViewById(R.id.spn_age_group);
        btnCountry = (Button) findViewById(R.id.spn_country);
        btnCounty = (Button) findViewById(R.id.spn_county);
        btnProfession = (Button) findViewById(R.id.spn_profession);
        btnSector = (Button) findViewById(R.id.spn_sector);
        btnCadre = (Button) findViewById(R.id.spn_cadre);

        editEmail = (EditText) findViewById(R.id.user_email);
        editPhone = (EditText) findViewById(R.id.phone);

        etxEmail = (EditText) findViewById(R.id.emailAddress);
        etxPhone = (EditText) findViewById(R.id.phonenumber);
        rgGender = (RadioGroup) findViewById(R.id.gender);
        rgSector = (RadioGroup) findViewById(R.id.sector);
        ageSpinner = (Spinner) findViewById(R.id.age_spinner);
        countySpinner = (Spinner) findViewById(R.id.county_spinner);
        cadreSpinner = (Spinner) findViewById(R.id.cadre_spinner);
        professionSpinner = (Spinner) findViewById(R.id.profession_spinner);
        txtSkip = (TextView) findViewById(R.id.skip_now);
        btnSubmit = (Button) findViewById(R.id.submit);
        enteredDataTable = (TableLayout) findViewById(R.id.entered_data);
        formTable = (TableLayout) findViewById(R.id.form);
        cadreRow = (TableRow) findViewById(R.id.cadre_row);
        db = new DatabaseHandler(this);
        context = this;

        app_user_id = db.getUser().getId();
        mAwesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        mAwesomeValidation.addValidation(this, R.id.user_email, Patterns.EMAIL_ADDRESS, R.string.error_email);
        mAwesomeValidation.addValidation(this, R.id.phone, Patterns.PHONE, R.string.error_phone);

        countyList = db.getCounties();
        County emptyCounty = new County();
        emptyCounty.setId(0);
        emptyCounty.setCounty("Select your County");
        countyList.add(0, emptyCounty);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this , R.array.age_bracket, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> cadreAdapter = ArrayAdapter.createFromResource(this , R.array.cadre, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> professionAdapter = ArrayAdapter.createFromResource(this, R.array.profession, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cadreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        professionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        CountySpinnerAdapter countySpinnerAdapter = new CountySpinnerAdapter();

        ageSpinner.setAdapter(adapter);
        countySpinner.setAdapter(countySpinnerAdapter);
        cadreSpinner.setAdapter(cadreAdapter);
        professionSpinner.setAdapter(professionAdapter);

        final SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        page = preference.getBoolean("elements_page", true);

        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        txtSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                proceed();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitProfile();
            }
        });

        btnCreateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProfile();
            }
        });

        professionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0 || i == 3){
                    cadreRow.setVisibility(View.GONE);
                }else{
                    cadreRow.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGenderChoiceItems((Button) v);
            }
        });

        btnAgeGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAgeGroupChoiceItems((Button) v);
            }
        });

        btnProfession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfessionChoiceItems((Button) v);
            }
        });

        btnSector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSectorChoiceItems((Button) v);
            }
        });

        btnCadre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCadreChoiceItems((Button) v);
            }
        });

        btnCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupActivity.this, CountryActivity.class);
                startActivityForResult(intent, COUNTRY_LIST_CODE);
            }
        });

        btnCounty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupActivity.this, CountyActivity.class);
                startActivityForResult(intent, COUNTY_LIST_CODE);
            }
        });

        if (user_id != 0){
            TextView txtEmail, txtPhone, txtGender, txtAgeGroup, txtCounty, txtProfession, txtCadre, txtSector;

            informationText.setVisibility(View.GONE);
            txtSkip.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.GONE);
            enteredDataTable.setVisibility(View.VISIBLE);
            formTable.setVisibility(View.GONE);

            txtEmail = (TextView) findViewById(R.id.existent_email);
            txtPhone = (TextView) findViewById(R.id.existent_phone);
            txtGender = (TextView) findViewById(R.id.existent_gender);
            txtAgeGroup = (TextView) findViewById(R.id.existent_age_group);
            txtCounty = (TextView) findViewById(R.id.existent_county);
            txtProfession = (TextView) findViewById(R.id.existent_profession);
            txtCadre = (TextView) findViewById(R.id.existent_cadre);
            txtSector = (TextView) findViewById(R.id.existent_sector);

            txtEmail.setText(pref.getString("email", ""));
            txtPhone.setText(pref.getString("phone", ""));
            txtGender.setText(pref.getString("gender", ""));
            txtAgeGroup.setText(pref.getString("age_group", ""));
            txtCounty.setText(pref.getString("county", ""));
            txtProfession.setText(pref.getString("profession", ""));
            txtCadre.setText(pref.getString("cadre", "N/A"));
            txtSector.setText(pref.getString("sector", ""));

            editEmail.setText(pref.getString("email", ""));
            editPhone.setText(pref.getString("phone", ""));
        }else{
            enteredDataTable.setVisibility(View.GONE);
            formTable.setVisibility(View.VISIBLE);
        }
    }

    public void proceed(){
        if (page) {
            startActivity(new Intent(context, MainActivity.class));
        }else{
            startActivity(new Intent(context, MainPageActivity.class));
        }
        finish();
    }

    public void showGenderChoiceItems(final Button btn){
        final String[] array_gender = getResources().getStringArray(R.array.gender);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setSingleChoiceItems(array_gender, this.selected_gender, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                btn.setTextColor(Color.BLACK);
                selected_gender = which;
                btn.setText(array_gender[which]);
                selected_gender_ = array_gender[which];
            }
        });
        builder.show();
    }

    public void showAgeGroupChoiceItems(final Button btn){
        final String[] array_agegroup = getResources().getStringArray(R.array.age_bracket);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setSingleChoiceItems(array_agegroup, this.selected_age, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                btn.setTextColor(Color.BLACK);
                selected_age = which;
                btn.setText(array_agegroup[which]);
                selected_age_ = array_agegroup[which];
            }
        });
        builder.show();
    }

    public void showProfessionChoiceItems(final Button btn){
        final String[] array_profession = getResources().getStringArray(R.array.profession);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setSingleChoiceItems(array_profession, this.selected_profession, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                btn.setTextColor(Color.BLACK);
                selected_profession = which;
                btn.setText(array_profession[which]);
                selected_profession_ = array_profession[which];
                if (array_profession[which].equals("Student")){
                    cadreLayout.setVisibility(View.GONE);
                    selected_cadre = -1;
                    selected_cadre_ = "";
                }else{
                    cadreLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        builder.show();
    }

    public void showSectorChoiceItems(final Button btn){
        final String[] array_sector = getResources().getStringArray(R.array.sector);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setSingleChoiceItems(array_sector, this.selected_sector, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                btn.setTextColor(Color.BLACK);
                selected_sector = which;
                btn.setText(array_sector[which]);
                selected_sector_ = array_sector[which];
            }
        });
        builder.show();
    }

    public void showCadreChoiceItems(final Button btn){
        final String[] array_cadre = getResources().getStringArray(R.array.cadre);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setSingleChoiceItems(array_cadre, this.selected_cadre, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                btn.setTextColor(Color.BLACK);
                selected_cadre = which;
                btn.setText(array_cadre[which]);
                selected_cadre_ = array_cadre[which];
            }
        });
        builder.show();
    }

    public void submitProfile(){
        String email, phone, gender, age_group, county, profession, cadre, sector;
        int selected_gender = rgGender.getCheckedRadioButtonId();
        int selected_sector = rgSector.getCheckedRadioButtonId();
        int selected_age_group = ageSpinner.getSelectedItemPosition();
        int selected_county = countySpinner.getSelectedItemPosition();
        int selected_profession = professionSpinner.getSelectedItemPosition();
        int selected_cadre = cadreSpinner.getSelectedItemPosition();

        if (mAwesomeValidation.validate()){
            email = etxEmail.getText().toString();
            phone = etxPhone.getText().toString();

            if (selected_gender == -1 || selected_sector == -1 || selected_age_group == 0 || selected_county == 0 || selected_profession == 0 || (selected_profession != 0 && selected_profession != 3 && selected_cadre == 0)){
                Toast.makeText(context, "Please fill in all the fields", Toast.LENGTH_LONG).show();
            }else{
                RadioButton genderRadioButton = (RadioButton) findViewById(selected_gender);
                RadioButton sectorRadioButton = (RadioButton) findViewById(selected_sector);
                gender = genderRadioButton.getText().toString();
                sector = sectorRadioButton.getText().toString();
                age_group = ageSpinner.getSelectedItem().toString();
                county = countyList.get(selected_county).getCounty();
                profession = professionSpinner.getSelectedItem().toString();
                if (selected_cadre != 0)
                    cadre = cadreSpinner.getSelectedItem().toString();
                else
                    cadre = "";

                UserProfile profile = new UserProfile();

                profile.setEmail(email);
                profile.setAge_group(age_group);
                profile.setCadre(cadre);
                profile.setCounty(county);
                profile.setGender(gender);
                profile.setPhone(phone);
                profile.setProfession(profession);
                profile.setSector(sector);
                profile.setPhone_id(FirebaseInstanceId.getInstance().getToken());
                profile.setApp_user_id(app_user_id);
                profile.setDisplay_no(Build.DISPLAY);

                try{
                    final ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage("Uploading data");
                    progressDialog.show();
                    Retrofit retrofit = RetrofitHelper.getInstance().createHelper();
                    UserProfileService userProfileClient = retrofit.create(UserProfileService.class);
                    Call<UserProfile> userProfileCall = userProfileClient.addProfile(profile);

                    userProfileCall.enqueue(new Callback<UserProfile>() {
                        @Override
                        public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                            progressDialog.dismiss();
                            UserProfile savedProfile = response.body();
                            SharedPreferences pref = context.getSharedPreferences("user_details", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();

                            assert savedProfile != null;
                            editor.putInt("id", savedProfile.getId());
                            editor.putString("email", savedProfile.getEmail());
                            editor.putString("phone", savedProfile.getPhone());
                            editor.putString("gender", savedProfile.getGender());
                            editor.putString("age_group", savedProfile.getAge_group());
                            editor.putString("county", savedProfile.getCounty());
                            editor.putString("profession", savedProfile.getProfession());
                            editor.putString("cadre", savedProfile.getCadre());
                            editor.putString("sector", savedProfile.getSector());
                            editor.apply();
                            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor prefEditor = preference.edit();
                            prefEditor.putBoolean("setup_page", false);
                            prefEditor.apply();

                            proceed();
                        }

                        @Override
                        public void onFailure(Call<UserProfile> call, Throwable t) {
                            progressDialog.dismiss();
                            Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                            t.printStackTrace();
                        }
                    });
                }catch (Exception ex){
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static void saveUserPreference(UserProfile profile, Boolean uploaded){
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt("id", profile.getId());
        editor.putString("email", profile.getEmail());
        editor.putString("phone", profile.getPhone());
        editor.putString("gender", profile.getGender());
        editor.putString("age_group", profile.getAge_group());
        editor.putString("county", profile.getCounty());
        editor.putString("profession", profile.getProfession());
        editor.putString("cadre", profile.getCadre());
        editor.putString("sector", profile.getSector());
        editor.putBoolean("uploaded", uploaded);
        editor.apply();
    }

    public void createProfile(){
        if (mAwesomeValidation.validate()){
            String email = editEmail.getText().toString();
            String phone = editPhone.getText().toString();

            if (selected_gender == -1 || selected_age == -1 || selected_sector == -1 || country_code.equals("") || (country_code.equals("KEN") && county_name.equals("")) || selected_profession == -1 || (selected_profession != 2 && selected_cadre == -1)){
                Toast.makeText(context, "Please make sure you fill in all the fields", Toast.LENGTH_SHORT).show();
            }else{
                UserProfile profile = new UserProfile();

                profile.setEmail(email);
                profile.setPhone(phone);
                profile.setAge_group(selected_age_);
                profile.setGender(selected_gender_);
                profile.setCountry_code(country_code);
                profile.setCounty(county_name);
                profile.setProfession(selected_profession_);
                profile.setCadre(selected_cadre_);
                profile.setSector(selected_sector_);
                profile.setPhone_id(FirebaseInstanceId.getInstance().getToken());
                profile.setDisplay_no(Build.DISPLAY);
                profile.setApp_user_id(app_user_id);

                if (ConnectivityReceiver.isConnected()){
                    final ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage("Uploading data");
                    progressDialog.show();
                    Retrofit retrofit = RetrofitHelper.getInstance().createHelper();
                    UserProfileService userProfileClient = retrofit.create(UserProfileService.class);
                    Call<UserProfile> userProfileCall = userProfileClient.addProfile(profile);

                    userProfileCall.enqueue(new Callback<UserProfile>() {
                        @Override
                        public void onResponse(@NonNull Call<UserProfile> call, @NonNull Response<UserProfile> response) {
                            progressDialog.dismiss();
                            if(response.code() == 200){
                                assert response.body() != null;
                                saveUserPreference(response.body(), ConnectivityReceiver.isConnected());
                                int user_id = response.body().getId();
                                Toast.makeText(context, "User profile uploaded successfully", Toast.LENGTH_SHORT).show();
                                if(forResult){
                                    Intent data = new Intent();
                                    data.putExtra("user_id",user_id);
                                    setResult(RESULT_OK,data);
                                }else {
                                    startActivity(new Intent(context, UserProfileDetailsActivity.class));
                                }
                                finish();
                            }else{
                                try {
                                    Log.e("Userprofile", response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(context, "There was an error adding your profile", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onFailure(@NonNull Call<UserProfile> call, Throwable throwable) {
                            Toast.makeText(context, "There was an error saving your profile", Toast.LENGTH_SHORT).show();
                            Log.e("Userprofile", throwable.getMessage());
                            throwable.printStackTrace();
                        }
                    });
                }else{
                    Toast.makeText(context, "You are offline. Please turn on internet connection and try again.", Toast.LENGTH_SHORT).show();
                }


            }
        }
    }

    @Override
    public void onBackPressed() {
        if(forResult){
            finish();
        }else {
            proceed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppController.getInstance().setConnectivityListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.setup_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
//            case R.id.action_save:
//                submitProfile();
//                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COUNTRY_LIST_CODE && resultCode == Activity.RESULT_OK){
            country_code = data.getStringExtra(CountryActivity.RESULT_COUNTRYCODE);
            String country_name = data.getStringExtra(CountryActivity.RESULT_COUNTRYNAME);
            btnCountry.setTextColor(Color.BLACK);
            btnCountry.setText(country_name);
            if (country_code.equals("KEN")){
                countyLayout.setVisibility(View.VISIBLE);
            }else{
                countyLayout.setVisibility(View.GONE);
                county_name = "";
            }
        }else if(requestCode == COUNTY_LIST_CODE && resultCode == Activity.RESULT_OK){
            county_name = data.getStringExtra(CountyActivity.RESULT_COUNTY_NAME);
            btnCounty.setTextColor(Color.BLACK);
            btnCounty.setText(county_name);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (!isConnected){
            Toast.makeText(context, "Seems like you have no connection", Toast.LENGTH_SHORT).show();
        }else{
            SharedPreferences pref = context.getSharedPreferences("user_details", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = pref.edit();
            Boolean uploaded = pref.getBoolean("uploaded", false);
            if(!uploaded){
                Retrofit retrofit = RetrofitHelper.getInstance().createHelper();
                UserProfileService userProfileClient = retrofit.create(UserProfileService.class);

                UserProfile profile = new UserProfile();

                profile.setEmail(pref.getString("email", ""));
                profile.setPhone(pref.getString("phone", ""));
                profile.setGender(pref.getString("gender", ""));
                profile.setAge_group(pref.getString("age_group", ""));
                profile.setCountry_code(pref.getString("country", "KEN"));
                profile.setCounty(pref.getString("county", ""));
                profile.setProfession(pref.getString("profession", ""));
                profile.setCadre(pref.getString("cadre", ""));
                profile.setSector(pref.getString("sector", ""));

                Call<UserProfile> userProfileCall = userProfileClient.addProfile(profile);
                userProfileCall.enqueue(new Callback<UserProfile>() {
                    @Override
                    public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                        assert response.body() != null;
                        editor.putBoolean("uploaded", true);
                        Toast.makeText(context, "User profile uploaded successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<UserProfile> call, Throwable throwable) {
                        Toast.makeText(context, "There was an error uploading your profile", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public class CountySpinnerAdapter extends BaseAdapter implements SpinnerAdapter{
        LayoutInflater inflater;
        @Override
        public int getCount() {
            return countyList.size();
        }

        @Override
        public Object getItem(int i) {
            return countyList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return countyList.get(i).getId();
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            if (inflater == null)
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null)
                convertView = inflater.inflate(R.layout.county_spinner_layout, null);

            TextView txtCounty = convertView.findViewById(R.id.county);

            County county = (County) getItem(i);

            txtCounty.setText(county.getCounty());
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return super.getDropDownView(position, convertView, parent);
        }
    }
}
