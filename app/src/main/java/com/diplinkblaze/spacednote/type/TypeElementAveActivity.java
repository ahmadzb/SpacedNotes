package com.diplinkblaze.spacednote.type;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.diplinkblaze.spacednote.universal.fragments.AveFragment;
import com.diplinkblaze.spacednote.universal.util.AveComponentSet;
import com.diplinkblaze.spacednote.universal.util.AveUtil;

import java.util.ArrayList;

import data.database.OpenHelper;
import data.model.type.Element;
import data.model.type.ElementCatalog;
import data.model.type.Type;

public class TypeElementAveActivity extends BaseActivity implements AveFragment.OnFragmentInteractionListener{

    private static final String TYPE_ID = "typeId";
    private static final String ELEMENT_ID = "elementId";
    private static final String ELEMENT_PATTERN = "elementPattern";

    public static Intent getIntent(Type type, @NonNull Element element, Context context) {
        Intent intent = new Intent(context, TypeElementAveActivity.class);
        intent.putExtra(TYPE_ID, type.getId());
        if (element.isRealized()) {
            intent.putExtra(ELEMENT_ID, element.getId());
        } else {
            throw new RuntimeException("unrealized models aren't allowed yet");
        }
        return intent;
    }

    public static Intent getIntentNewText(Type type, Context context) {
        Intent intent = new Intent(context, TypeElementAveActivity.class);
        intent.putExtra(TYPE_ID, type.getId());
        intent.putExtra(ELEMENT_PATTERN, Element.PATTERN_TEXT);
        return intent;
    }

    public static Intent getIntentNewList(Type type, Context context) {
        Intent intent = new Intent(context, TypeElementAveActivity.class);
        intent.putExtra(TYPE_ID, type.getId());
        intent.putExtra(ELEMENT_PATTERN, Element.PATTERN_LIST);
        return intent;
    }

    public static Intent getIntentNewPictures(Type type, Context context) {
        Intent intent = new Intent(context, TypeElementAveActivity.class);
        intent.putExtra(TYPE_ID, type.getId());
        intent.putExtra(ELEMENT_PATTERN, Element.PATTERN_PICTURE);
        return intent;
    }

    public static Intent getIntentNewDivider(Type type, Context context) {
        Intent intent = new Intent(context, TypeElementAveActivity.class);
        intent.putExtra(TYPE_ID, type.getId());
        intent.putExtra(ELEMENT_PATTERN, Element.PATTERN_DIVIDER);
        return intent;
    }

    private static final String TAG_FRAGMENT = "fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_ave);
        initializeViews();
    }

    private void initializeViews() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
        if (fragment == null) {
            if (getIntent().getExtras().containsKey(ELEMENT_ID)) {
                long id = getIntent().getLongExtra(ELEMENT_ID, 0);
                Element element = ElementCatalog.getElementById(id, OpenHelper.getDatabase(this));
                if (element.getPattern() == Element.PATTERN_TEXT) {
                    fragment = AveFragment.newInstance(AveUtil.TypeElement.createText(element, getResources()));
                } else if (element.getPattern() == Element.PATTERN_LIST) {
                    fragment = AveFragment.newInstance(AveUtil.TypeElement.createList(element, getResources()));
                } else if (element.getPattern() == Element.PATTERN_PICTURE) {
                    fragment = AveFragment.newInstance(AveUtil.TypeElement.createPictures(element, getResources()));
                } else if (element.getPattern() == Element.PATTERN_DIVIDER) {
                    fragment = AveFragment.newInstance(AveUtil.TypeElement.createDivider(element, getResources()));
                }
            } else {
                int elementPattern = getIntent().getIntExtra(ELEMENT_PATTERN, Element.PATTERN_TEXT);
                if (elementPattern == Element.PATTERN_TEXT) {
                    fragment = AveFragment.newInstance(AveUtil.TypeElement.createText(getResources()));
                } else if (elementPattern == Element.PATTERN_LIST) {
                    fragment = AveFragment.newInstance(AveUtil.TypeElement.createList(getResources()));
                } else if (elementPattern == Element.PATTERN_PICTURE) {
                    fragment = AveFragment.newInstance(AveUtil.TypeElement.createPictures(getResources()));
                } else if (elementPattern == Element.PATTERN_DIVIDER) {
                    fragment = AveFragment.newInstance(AveUtil.TypeElement.createDivider(getResources()));
                }
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.root, fragment, TAG_FRAGMENT);
            transaction.commit();
        }
    }

    //====================================== Ave Fragment ==========================================
    @Override
    public void onSaveResult(AveComponentSet componentSet) {
        Type type = Type.newInstance();
        type.setId(getIntent().getLongExtra(TYPE_ID, 0));
        AveUtil.TypeElement.save(componentSet, type, OpenHelper.getDatabase(this), this);
    }

    @Override
    public boolean onEditPressed(AveComponentSet componentSet) {
        return true;
    }

    @Override
    public void onFinish(boolean success) {
        if (success) {
            setResult(RESULT_OK);
        }
        finish();
    }

    @Override
    public void onMenuClicked(int itemId, AveComponentSet componentSet) {

    }

    @Override
    public ArrayList<AveComponentSet.ViewComponent> getViewComponents(AveComponentSet componentSet) {
        return null;
    }
}
