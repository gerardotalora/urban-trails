package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import edu.neu.madcourse.urban_trails.fragments.HomeFragment;
import edu.neu.madcourse.urban_trails.fragments.ProfileFragment;
import edu.neu.madcourse.urban_trails.fragments.SearchFragment;
import edu.neu.madcourse.urban_trails.fragments.MyTrailsFragment;
import edu.neu.madcourse.urban_trails.fragments.StartTrailFragment;

public class HomeActivity extends AppCompatActivity {

    private static final String ACTIVE_FRAGMENT = "ACTIVE_FRAGMENT";
    BottomNavigationView bottomNavigation;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        if (savedInstanceState != null) {
            Fragment fragment = getSupportFragmentManager().getFragment(savedInstanceState, ACTIVE_FRAGMENT);
            if (fragment != null) {
                openFragment(fragment);
                return;
            }
        }
        openFragment(HomeFragment.newInstance());
    }

    public void openFragment(Fragment fragment) {
        this.activeFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        try {
            this.setTitle(((NavigationFragment) fragment).getTitle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.navigation_home:
                            openFragment(HomeFragment.newInstance());
                            return true;
                        case R.id.navigation_search:
                            openFragment(SearchFragment.newInstance());
                            return true;
                        case R.id.navigation_start_trail:
                            openFragment(StartTrailFragment.newInstance("", ""));
                            return true;
                        case R.id.navigation_star:
                            openFragment(MyTrailsFragment.newInstance());
                            return true;
                        case R.id.navigation_profile:
                            openFragment(ProfileFragment.newInstance());
                            return true;
                    }
                    return false;
                }
            };


    public void onClick(View view) {
        Intent intent = new Intent(this, TrailActivity.class);
        startActivity(intent);
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

        switch (item.getItemId()) {
            case R.id.action_settings:
                break;
            case R.id.action_signout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, ACTIVE_FRAGMENT, this.activeFragment);
    }
}