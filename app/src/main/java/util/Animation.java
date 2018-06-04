package util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Created by Ahmad on 12/24/17.
 * All rights reserved.
 */

public class Animation {
    public static void shakeAnimation(View view) {
        float offset = Measures.dpToPx(20, view.getContext());
        ObjectAnimator right1 = ObjectAnimator.ofFloat(view, "translationX", offset);
        ObjectAnimator left1 = ObjectAnimator.ofFloat(view, "translationX", offset = offset * -0.7f);
        ObjectAnimator right2 = ObjectAnimator.ofFloat(view, "translationX", offset = offset * -0.7f);
        ObjectAnimator left2 = ObjectAnimator.ofFloat(view, "translationX", offset = offset * -0.7f);
        ObjectAnimator right3 = ObjectAnimator.ofFloat(view, "translationX", offset = offset * -0.7f);
        ObjectAnimator left3 = ObjectAnimator.ofFloat(view, "translationX", offset = offset * -0.7f);
        ObjectAnimator fixed = ObjectAnimator.ofFloat(view, "translationX", 0);
        AnimatorSet shake = new AnimatorSet();
        shake.play(right1).before(left1);
        shake.play(left1).before(right2);
        shake.play(right2).before(left2);
        shake.play(left2).before(right3);
        shake.play(right3).before(left3);
        shake.play(left3).before(fixed);
        shake.setDuration(130);
        shake.start();
    }
}
