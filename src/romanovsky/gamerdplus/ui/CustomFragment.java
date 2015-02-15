package romanovsky.gamerdplus.ui;

import android.support.v4.app.Fragment;
import android.view.View;

public abstract class CustomFragment extends Fragment {

	public abstract void refresh(View view);

	public abstract void filter(String filter);

}
