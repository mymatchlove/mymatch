package mymatch.love.activities;

import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import mymatch.love.utility.Common;
import mymatch.love.fragments.ExpressReceivedFragment;
import mymatch.love.fragments.ExpressSentFragment;
import mymatch.love.R;
import mymatch.love.utility.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ExpressInterestActivity extends AppCompatActivity {

    Common common;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivityNew.class));
            return;
        }
        setContentView(R.layout.activity_express_interest);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Express Interest");
        toolbar.setNavigationOnClickListener(v -> finish());

        common = new Common(this);

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.containsKey("interest_tag")) {
                if (b.getString("interest_tag").equals("receive")) {
                    viewPager.setCurrentItem(1);
                }
            }
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle b = intent.getExtras();
        if (b != null) {
            if (b.containsKey("interest_tag")) {
                if (b.getString("interest_tag").equals("receive")) {
                    viewPager.setCurrentItem(1);
                }
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ExpressSentFragment(), "Sent");
        adapter.addFragment(new ExpressReceivedFragment(), "Received");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
        finish();
    }

}
