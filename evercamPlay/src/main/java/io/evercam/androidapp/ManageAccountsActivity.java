package io.evercam.androidapp;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

import io.evercam.androidapp.authentication.EvercamAccount;
import io.evercam.androidapp.custom.AccountItemAdapter;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.tasks.CheckKeyExpirationTask;
import io.evercam.androidapp.utils.Constants;

public class ManageAccountsActivity extends ParentAppCompatActivity {
    private static String TAG = "ManageAccountsActivity";

    private String oldDefaultUser = "";
    private CustomProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_account);

        setUpDefaultToolbar();

        progressDialog = new CustomProgressDialog(ManageAccountsActivity.this);

        // create and start the task to show all user accounts
        ListView listview = (ListView) findViewById(R.id.email_list);

        if (AppData.defaultUser != null) {
            oldDefaultUser = AppData.defaultUser.getUsername();
        }

        showAllAccounts();

        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listview = (ListView) findViewById(R.id.email_list);

                final AppUser user = (AppUser) listview.getItemAtPosition(position);

                if (user.getId() < 0) // add new user item
                {
                    startActivity(new Intent(ManageAccountsActivity.this, LoginActivity.class));
                    return;
                }

                final View optionListView = getLayoutInflater().inflate(R.layout
                        .dialog_manage_account_options, null);

                final AlertDialog dialog = CustomedDialog.getAlertDialogNoTitle
                        (ManageAccountsActivity.this, optionListView);
                dialog.show();

                Button openDefault = (Button) optionListView.findViewById(R.id.btn_open_account);
                Button delete = (Button) optionListView.findViewById(R.id.btn_delete_account);

                openDefault.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Check if stored API key and ID before switching account
                        new CheckKeyExpirationTaskAccount(user, optionListView, dialog)
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });

                delete.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomedDialog.getConfirmRemoveDialog(ManageAccountsActivity.this,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface warningDialog, int which) {
                                        if (AppData.appUsers != null && AppData.appUsers.size() == 2) {
                                            // If only one user exists, log out the user
                                            CamerasActivity.logOutDefaultUser(ManageAccountsActivity.this);
                                        } else {
                                            new EvercamAccount(ManageAccountsActivity.this)
                                                    .remove(user.getEmail(),
                                                            new AccountManagerCallback<Boolean>() {
                                                                @Override
                                                                public void run
                                                                        (AccountManagerFuture<Boolean>
                                                                                 future) {
                                                                    // This is the line that
                                                                    // actually
                                                                    // starts the
                                                                    // call to remove the account.
                                                                    try {
                                                                        boolean isAccountDeleted = future
                                                                                .getResult();
                                                                        if (isAccountDeleted) {
                                                                            showAllAccounts();
                                                                        }
                                                                    } catch (OperationCanceledException e) {
                                                                        e.printStackTrace();
                                                                    } catch (IOException e) {
                                                                        e.printStackTrace();
                                                                    } catch (AuthenticatorException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            });
                                        }
                                        dialog.dismiss();
                                    }
                                }, R.string.msg_confirm_remove).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // Finish this activity on restart because there are lots of opportunities
        // that the account has been changed, and it's hard to handle.
        // Finishing it is a simpler way.
        finish();
    }

    @Override
    public void onBackPressed() {
        if (!AppData.defaultUser.getUsername().equals(oldDefaultUser)) {
            setResult(Constants.RESULT_ACCOUNT_CHANGED);
        }
        this.finish();
    }

    // Tells that what item has been selected from options. We need to call the
    // relevant code for that item.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:

                if (AppData.defaultUser != null && oldDefaultUser != null) {
                    if (!AppData.defaultUser.getUsername().equals(oldDefaultUser)) {
                        setResult(Constants.RESULT_ACCOUNT_CHANGED);
                    }
                } else {
                    setResult(Constants.RESULT_ACCOUNT_CHANGED);
                }
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Update shared preference that stores default user's Email
     *
     * @param closeActivity   after updating, close the account manage activity or not
     * @param dialogToDismiss the account manage dialog that is showing
     */
    public void updateDefaultUser(final String userEmail, final Boolean closeActivity,
                                  final AlertDialog dialogToDismiss) {
        EvercamAccount evercamAccount = new EvercamAccount(this);
        evercamAccount.updateDefaultUser(userEmail);
        AppData.appUsers = evercamAccount.retrieveUserList();

        getMixpanel().identifyUser(AppData.defaultUser.getUsername());
        registerUserWithIntercom(AppData.defaultUser);

        if (closeActivity) {
            if (!AppData.defaultUser.getUsername().equals(oldDefaultUser)) {
                setResult(Constants.RESULT_ACCOUNT_CHANGED);
            }
            ManageAccountsActivity.this.finish();
        } else {
            showAllAccounts();
        }

        if (dialogToDismiss != null && dialogToDismiss.isShowing()) {
            dialogToDismiss.dismiss();
        }
    }

    private void showAllAccounts() {
        ArrayList<AppUser> appUsers = new EvercamAccount(this).retrieveUserList();

        ListAdapter listAdapter = new AccountItemAdapter(ManageAccountsActivity.this,
                R.layout.item_list_user, R.layout.item_list_new_user,
                R.id.account_item_email, appUsers);
        ListView listview = (ListView) findViewById(R.id.email_list);
        listview.setAdapter(null);
        listview.setAdapter(listAdapter);
    }

    class CheckKeyExpirationTaskAccount extends CheckKeyExpirationTask {
        public CheckKeyExpirationTaskAccount(AppUser appUser, View viewToDismiss, AlertDialog
                dialogToDismiss) {
            super(appUser, viewToDismiss, dialogToDismiss);
        }

        @Override
        protected void onPostExecute(Boolean isExpired) {
            if (isExpired) {
                new EvercamAccount(ManageAccountsActivity.this).remove(appUser.getEmail(), null);

                finish();
                Intent slideIntent = new Intent(ManageAccountsActivity.this, OnBoardingActivity.class);
                startActivity(slideIntent);
            } else {
                progressDialog.show(ManageAccountsActivity.this.getString(R.string.switching_account));

                updateDefaultUser(appUser.getEmail(), true, dialogToDismiss);

                getMixpanel().identifyUser(appUser.getUsername());
                registerUserWithIntercom(appUser);

                viewToDismiss.setEnabled(false);
                viewToDismiss.setClickable(false);
            }
        }
    }
}
