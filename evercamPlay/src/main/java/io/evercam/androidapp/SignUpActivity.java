package io.evercam.androidapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import io.evercam.API;
import io.evercam.ApiKeyPair;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.UserDetail;
import io.evercam.androidapp.account.AccountUtils;
import io.evercam.androidapp.account.UserProfile;
import io.evercam.androidapp.authentication.EvercamAccount;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.feedback.NewUserFeedbackItem;
import io.evercam.androidapp.permission.Permission;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.DataCollector;
import com.google.firebase.analytics.FirebaseAnalytics;

public class SignUpActivity extends ParentAppCompatActivity {
    private final String TAG = "SignUpActivity";
    // Auto filled profiles
    private String filledFirstname = "";
    private String filledLastname = "";
    private String filledEmail = "";

    private EditText firstnameEdit;
    private EditText lastnameEdit;
    private EditText usernameEdit;
    private EditText emailEdit;
    private EditText passwordEdit;
//    private EditText repasswordEdit;

    private View signUpFormView;
    private View signUpStatusView;
    private CreateUserTask createUserTask;
    private View focusView = null;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

//        setUpDefaultToolbar();

        initialPage();

        checkContactPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        switch (requestCode) {
            case Permission.REQUEST_CODE_CONTACTS:

                boolean contactsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (contactsAccepted) {
                    checkContactPermission();
                }
                break;
        }
    }

    private void checkContactPermission() {
        if (Permission.isGranted(this, Permission.CONTACTS)) {
            readFromAccount();
        } else {
            Permission.request(SignUpActivity.this, new String[]{Permission.CONTACTS},
                    Permission.REQUEST_CODE_CONTACTS);
        }
    }

    private void initialPage() {
        signUpFormView = findViewById(R.id.signup_form);
        signUpStatusView = findViewById(R.id.signup_status);
        firstnameEdit = (EditText) findViewById(R.id.forename_edit);
        lastnameEdit = (EditText) findViewById(R.id.lastname_edit);
        usernameEdit = (EditText) findViewById(R.id.username_edit);
        emailEdit = (EditText) findViewById(R.id.email_edit);
        passwordEdit = (EditText) findViewById(R.id.password_edit);
//        repasswordEdit = (EditText) findViewById(R.id.repassword_edit);

        Button signupBtn = (Button) findViewById(R.id.sign_up_button);

        Button termofuse = (Button) findViewById(R.id.term_of_Use);

        Button backButton = (Button) findViewById(R.id.back_button);

        fillDefaultProfile();

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signupBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new SignUpCheckInternetTask(SignUpActivity.this).executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR);
            }
        });

        termofuse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aboutIntent = new Intent(SignUpActivity.this, SimpleWebActivity.class);
                aboutIntent.putExtra(Constants.BUNDLE_KEY_URL,
                        getString(R.string.term_of_use_url));
                startActivity(aboutIntent);

            }
        });
    }

    private UserDetail checkDetails() {
        UserDetail user = new UserDetail();
        String firstname = firstnameEdit.getText().toString();
        String lastname = lastnameEdit.getText().toString();
        String username = usernameEdit.getText().toString();
        String email = emailEdit.getText().toString();
        String password = passwordEdit.getText().toString();
//        String repassword = repasswordEdit.getText().toString();

        if (firstname == null || firstname.trim().isEmpty()) {
            CustomToast.showInCenter(this, R.string.error_firstname_required);
            focusView = firstnameEdit;
            return null;
        } else {
            user.setFirstname(firstname.trim());
        }

        if (lastname == null || lastname.trim().isEmpty()) {
            CustomToast.showInCenter(this, R.string.error_lastname_required);
            focusView = lastnameEdit;
            return null;
        } else {
            user.setLastname(lastname.trim());
        }

        if (TextUtils.isEmpty(username)) {
            CustomToast.showInCenter(this, R.string.error_username_required);
            focusView = usernameEdit;
            return null;
        } else if (username.length() < 3) {
            CustomToast.showInCenter(this, R.string.username_too_short);
            focusView = usernameEdit;
            return null;
        } else if (username.contains(" ")) {
            CustomToast.showInCenter(this, R.string.error_invalid_username);
            focusView = usernameEdit;
            return null;
        } else {
            user.setUsername(username);
        }

        if (TextUtils.isEmpty(email)) {
            CustomToast.showInCenter(this, R.string.error_email_required);
            focusView = emailEdit;
            return null;
        } else if (!isValidEmailAddress(email)){
            CustomToast.showInCenter(this, R.string.invalidEmail);
            focusView = emailEdit;
            return null;
        } else {
            user.setEmail(email);
        }

        if (TextUtils.isEmpty(password)) {
            CustomToast.showInCenter(this, R.string.error_password_required);
            focusView = passwordEdit;
            return null;
        }else{
            user.setPassword(password);
        }

        /*if (TextUtils.isEmpty(repassword)) {
            CustomToast.showInCenter(this, R.string.error_confirmpassword_required);
            focusView = repasswordEdit;
            return null;
        } else if (!password.equals(repassword)) {
            CustomToast.showInCenter(this, R.string.passwordNotMatch);
            return null;
        } else {
            user.setPassword(password);
        }*/

        //Append user country if available, but the country field will not be showing in sign up form
        String countryCode = DataCollector.getCountryCode(this);
        if (countryCode != null && !countryCode.isEmpty()) {
            user.setCountrycode(countryCode);
        }

        user.setAndroidtoken("android16c48f51");
        
        return user;
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    private void readFromAccount() {
        try {
            UserProfile profile = AccountUtils.getUserProfile(this);
            if (profile.primaryEmail() != null) {
                filledEmail = profile.primaryEmail();
            } else if (profile.possibleEmails().size() > 0) {
                filledEmail = profile.possibleEmails().get(0);
            }

            if (profile.possibleNames().size() > 0) {
                String name = profile.possibleNames().get(0);
                String[] nameArray = name.split("\\s+");
                if (nameArray.length >= 2) {
                    filledFirstname = nameArray[0];
                    filledLastname = nameArray[1];
                }
            }
        } catch (Exception e) {
            // If exceptions happen here, will not influence app functionality.
            // Just catch it to avoid crashing.
            e.printStackTrace();
        }

        fillDefaultProfile();
    }

    private void fillDefaultProfile() {
        firstnameEdit.setText(filledFirstname);
        lastnameEdit.setText(filledLastname);
        emailEdit.setText(filledEmail);
    }

    private void showProgress(boolean show) {
        signUpStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
        signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void attemptSignUp() {
        UserDetail userDetail = checkDetails();
        if (userDetail != null) {
            if (createUserTask != null) {
                createUserTask = null;
            }
            createUserTask = new CreateUserTask(userDetail);
            createUserTask.execute();
        } else {
            if (focusView != null) {
                focusView.requestFocus();
            }
        }
    }

    public class CreateUserTask extends AsyncTask<Void, Void, String> {
        private UserDetail userDetail;
        private AppUser newUser = null;

        public CreateUserTask(UserDetail userDetail) {
            this.userDetail = userDetail;
        }

        @Override
        protected void onPostExecute(String message) {
            if (message == null) {

                //Calling firebase analytics
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(SignUpActivity.this);
                Bundle bundle = new Bundle();
                bundle.putString("Evercam_User_Name", newUser.getUsername());
                bundle.putString("Evercam_Email", newUser.getEmail());
                mFirebaseAnalytics.logEvent("Signed_Up", bundle);

                /*
                //Calling google analytics
                EvercamPlayApplication.sendEventAnalytics(SignUpActivity.this,
                        R.string.category_sign_up, R.string.action_signup_success,
                        R.string.label_signup_successful);
                */

                getMixpanel().identifyNewUser(newUser);
                getMixpanel().identifyUser(newUser.getUsername());
                getMixpanel().sendEvent(R.string.mixpanel_event_sign_up, null);

                registerUserWithIntercom(newUser);


                new EvercamAccount(SignUpActivity.this).add(newUser);
                AppData.defaultUser = newUser;

                showProgress(false);
                setResult(Constants.RESULT_TRUE);
                finish();
            } else {
                showProgress(false);
                CustomToast.showInCenter(SignUpActivity.this, message);
            }
        }

        @Override
        protected void onPreExecute() {
            hideSoftKeyboard();
            showProgress(true);
        }

        @Override
        protected String doInBackground(Void... args) {
            try {
                User.create(userDetail);
                ApiKeyPair userKeyPair = API.requestUserKeyPairFromEvercam(userDetail.getUsername
                        (), userDetail.getPassword());
                String userApiKey = userKeyPair.getApiKey();
                String userApiId = userKeyPair.getApiId();
                API.setUserKeyPair(userApiKey, userApiId);
                User evercamUser = new User(userDetail.getUsername());

                newUser = new AppUser(evercamUser);
                newUser.setApiKeyPair(userApiKey, userApiId);

                return null;
            } catch (EvercamException e) {
                return e.getMessage();
            }
        }
    }

    class SignUpCheckInternetTask extends CheckInternetTask {
        public SignUpCheckInternetTask(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(Boolean hasNetwork) {
            if (hasNetwork) {
                attemptSignUp();
            } else {
                CustomedDialog.showInternetNotConnectDialog(SignUpActivity.this);
            }
        }
    }
}
