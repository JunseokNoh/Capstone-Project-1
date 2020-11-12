package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.ui.login.LoginActivity;
import com.google.android.material.textview.MaterialTextView;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.nhn.android.naverlogin.OAuthLogin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Mypage_fragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class Mypage_fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private MaterialTextView logoutView;
    private MaterialTextView Account_delete_View;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String login_type;
    private SharedPreferences pref;
    private static Context mcontext;
    private static Activity mActivity;
    private static Mypage_fragment Instance;
    private boolean isSuccessDeleteToken = false;
    static OAuthLogin mOAuthLoginModule;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param activity Parameter 1.
     * @return A new instance of fragment Mypage_fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Mypage_fragment newInstance(Activity activity) {
        Mypage_fragment fragment = new Mypage_fragment();
        //Bundle args = new Bundle();
        //args.putString(ARG_PARAM1);
        //args.putString(ARG_PARAM2);
        //fragment.setArguments(args);
        return fragment;
    }

    public Mypage_fragment() {
       // mActivity = activity;
        // Required empty public constructor
        //this.mcontext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof Activity){
            mActivity = (Activity)context;
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View v = inflater.inflate(R.layout.fragment_mypage_fragment, container, false);
        mOAuthLoginModule = OAuthLogin.getInstance();
        pref = getActivity().getSharedPreferences("login_information", Context.MODE_PRIVATE);
        login_type = pref.getString("type", "111111");

        //mcontext = container.getContext();

        logoutView = (MaterialTextView)v.findViewById(R.id.logout_button);
        logoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "asdfasdf", Toast.LENGTH_SHORT).show();
                Log.e("login_type", login_type);
                //카카오 로그아웃
                if (login_type.equals("kakao")) {
                    Log.e("Main Logout", "로그아웃 성공");
                    //System.out.println("첫번째 menu");
                    //Toast.makeText(getContext(), "asdfasdfasdf", Toast.LENGTH_SHORT).show();
                    UserManagement.getInstance()
                            .requestLogout(new LogoutResponseCallback() {
                                @Override
                                public void onCompleteLogout() {
                                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                }
                            });
                }
                //네이버 로그아웃
                else if (login_type.equals("naver")) {
                    //System.out.println("두번째 menu");
                    OAuthLogin mOAuthLogin = OAuthLogin.getInstance();
                    String loginState = mOAuthLogin.getState(getActivity()).toString();
                    if(!loginState.equals("NEED_LOGIN")){
                        Log.e("Main Logout", "로그아웃 성공");
                        mOAuthLogin.logout(getActivity());
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();

                    }
                    else{
                        Log.e("Main Logout", "로그아웃 실패");
                    }
                }

            }
        });
        Account_delete_class account_delete_listener = new Account_delete_class();
        Account_delete_View = (MaterialTextView)v.findViewById(R.id.account_delete);
        Account_delete_View.setOnClickListener(account_delete_listener);

        return v;
    }
    void showDialog() {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(getActivity())
                .setTitle("알림")
                .setMessage("회원 탈퇴시 계정의 모든 정보가 손실됩니다.\n정말로 탈퇴하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    @Override public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (login_type.equals("kakao")) { // 카카오 회원탈퇴
                            Log.e("Account_delete_class", "들어옴");
                            UserManagement.getInstance()
                                    .requestUnlink(new UnLinkResponseCallback() {
                                        @Override
                                        public void onSessionClosed(ErrorResult errorResult) {
                                            Log.e("KAKAO_API", "세션이 닫혀 있음: " + errorResult);
                                        }

                                        @Override
                                        public void onFailure(ErrorResult errorResult) {
                                            Log.e("KAKAO_API", "연결 끊기 실패: " + errorResult);

                                        }
                                        @Override
                                        public void onSuccess(Long result) {// 카카오 회원탈퇴 성공시
                                            Log.i("KAKAO_API", "연결 끊기 성공. id: " + result);
                                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                                            startActivity(intent);
                                            getActivity().finish();
                                            Toast.makeText(getActivity(), "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        //네이버 회원탈퇴퇴
                        else if (login_type.equals("naver")) {

                            new ThreadTask<Object>() {

                                @Override
                                protected void onPreExecute() {// excute 전에

                                }

                                @Override
                                protected void doInBackground(String... urls) throws IOException, JSONException {//background로 돌아갈것
                                    Log.e("delete_account", "log1");
                                    if(LoginActivity.mContext == null){
                                        //Toast.makeText(getActivity(), "null입니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Log.e("delete_account", "log2");
                                        isSuccessDeleteToken = mOAuthLoginModule.logoutAndDeleteToken(LoginActivity.mContext);
                                        if(isSuccessDeleteToken) {
                                            Intent intent = new Intent(getActivity(), LoginActivity.class);

                                            startActivity(intent);
                                            getActivity().finish();

                                        }
                                    }
                                }

                                @Override
                                protected void onPostExecute() {
                                    if(isSuccessDeleteToken) {
                                        Toast.makeText(getActivity(), "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }.execute("");

                        }
                    } })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {

                    } });
        AlertDialog msgDlg = msgBuilder.create(); msgDlg.show();
    }


    class Account_delete_class implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            showDialog();
        }
    }
}