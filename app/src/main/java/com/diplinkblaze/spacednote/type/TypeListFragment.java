package com.diplinkblaze.spacednote.type;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ActivityRequestHost;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.contract.NewItemSupportListener;
import com.diplinkblaze.spacednote.universal.fragments.ListFragment;
import com.diplinkblaze.spacednote.universal.util.ListData;
import com.diplinkblaze.spacednote.universal.util.ListUtil;

import data.database.OpenHelper;
import data.model.type.Type;
import util.TypeFaceUtils;

public class TypeListFragment extends Fragment implements ListFragment.OnFragmentInteractionListener,
        NewItemSupportListener, ContentUpdateListener{

    public static final String TAG_LIST_FRAGMENT_AVAILABLE = "listFragmentAvailable";
    public static final String TAG_LIST_FRAGMENT_ARCHIVED = "listFragmentArchived";

    private Adapter mAdapter;

    private static final int ACTIVITY_REQUEST_AVE = 0;
    private static final int ACTIVITY_REQUEST_TYPE = 1;

    public TypeListFragment() {
        // Required empty public constructor
    }

    public static TypeListFragment newInstance() {
        TypeListFragment fragment = new TypeListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_type_list, container, false);
        initializeViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        ViewPager pager = contentView.findViewById(R.id.fragment_type_list_pager);
        mAdapter = new Adapter(getChildFragmentManager());
        pager.setAdapter(mAdapter);

        TabLayout tabLayout = contentView.findViewById(R.id.fragment_type_list_tab_layout);
        tabLayout.setupWithViewPager(pager);
        TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), tabLayout);
    }

    public interface OnFragmentInteractionListener {

    }

    //================================== Content Update Listener ===================================
    @Override
    public void updateContent() {
        ContentUpdateUtil.updateContentChildren(this);
    }

    //======================================= List Fragment ========================================
    @Override
    public ListData retrieveUniversalListData(Context context, String tag, Bundle identifier) {
        if (TAG_LIST_FRAGMENT_AVAILABLE.equals(tag)) {
            return ListUtil.Type.createAvailable(OpenHelper.getDatabase(context));
        } else if (TAG_LIST_FRAGMENT_ARCHIVED.equals(tag)) {
            return ListUtil.Type.createArchived(OpenHelper.getDatabase(context));
        } else
            throw new RuntimeException("Tag was not recognized");
    }

    @Override
    public void onUniversalListPositionsChanged(ListData data, ListData.Entity rootEntity, String tag, Bundle identifier) {
        SQLiteDatabase database = OpenHelper.getDatabase(getContext());
        database.beginTransaction();
        ListUtil.Type.updatePositions(data, rootEntity, database, getContext());
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    @Override
    public void onUniversalListItemSelected(ListData.Entity entity, String tag, Bundle identifier) {
        ListUtil.Type.TypeEntity typeEntity = (ListUtil.Type.TypeEntity) entity;
        Type type = typeEntity.getType();
        Intent intent = TypeElementsActivity.getIntent(type, getContext());
        startActivityForResult(intent, ACTIVITY_REQUEST_TYPE);
    }

    @Override
    public void onUniversalListItemMoreSelected(ListData.Entity entity, View moreView, String tag, Bundle identifier) {

    }

    @Override
    public void onUniversalListActionClicked(ListData.InfoRow infoRow, View view, String tag, Bundle identifier) {

    }

    @Override
    public void onUniversalListDeliverViewInfo(ListFragment.ListViewInfo viewInfo, String tag, Bundle identifier) {

    }

    //====================================== New Item Support ======================================
    @Override
    public void newItem() {
        int request = ACTIVITY_REQUEST_AVE;
        if (getActivity() instanceof ActivityRequestHost) {
            ActivityRequestHost host = (ActivityRequestHost) getActivity();
            request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
        }
        startActivityForResult(TypeAveActivity.getIntent(null, getContext()), request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            //Ave
            {
                int request = ACTIVITY_REQUEST_AVE;
                if (getActivity() instanceof ActivityRequestHost) {
                    ActivityRequestHost host = (ActivityRequestHost) getActivity();
                    request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
                }
                if (requestCode == request) {
                    updateContent();
                }
            }
            //Type
            {
                int request = ACTIVITY_REQUEST_TYPE;
                if (getActivity() instanceof ActivityRequestHost) {
                    ActivityRequestHost host = (ActivityRequestHost) getActivity();
                    request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
                }
                if (requestCode == request) {
                    updateContent();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //====================================== Pager Adapter =========================================
    private class Adapter extends FragmentStatePagerAdapter {

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                Fragment listFragment = ListFragment.newInstance(TAG_LIST_FRAGMENT_AVAILABLE);
                return listFragment;
            } else if (position == 1) {
                Fragment listFragment = ListFragment.newInstance(TAG_LIST_FRAGMENT_ARCHIVED);
                return listFragment;
            } else
                throw new RuntimeException("position is too big");
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.available);
            } else if (position == 1) {
                return getString(R.string.archived);
            } else
                throw new RuntimeException("position is too big");
        }
    }
}
