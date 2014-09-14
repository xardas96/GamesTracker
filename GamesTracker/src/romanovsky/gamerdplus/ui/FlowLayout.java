package romanovsky.gamerdplus.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup {
	private int lineHeight;

	public static class LayoutParams extends ViewGroup.LayoutParams {

		public final int horizontalSpacing;
		public final int verticalSpacing;

		public LayoutParams(int horizontal_spacing, int vertical_spacing) {
			super(0, 0);
			this.horizontalSpacing = horizontal_spacing;
			this.verticalSpacing = vertical_spacing;
		}
	}

	public FlowLayout(Context context) {
		super(context);
	}

	public FlowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		assert (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED);

		final int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
		int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
		final int count = getChildCount();
		int line_height = 0;

		int xpos = getPaddingLeft();
		int ypos = getPaddingTop();

		int childHeightMeasureSpec;
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
			childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
		} else {
			childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				final LayoutParams lp = (LayoutParams) child.getLayoutParams();
				child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), childHeightMeasureSpec);
				final int childw = child.getMeasuredWidth();
				line_height = Math.max(line_height, child.getMeasuredHeight() + lp.verticalSpacing);

				if (xpos + childw > width) {
					xpos = getPaddingLeft();
					ypos += line_height;
				}

				xpos += childw + lp.horizontalSpacing;
			}
		}
		this.lineHeight = line_height;

		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
			height = ypos + line_height;

		} else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
			if (ypos + line_height < height) {
				height = ypos + line_height;
			}
		}
		setMeasuredDimension(width, height);
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(1, 1);
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		if (p instanceof LayoutParams) {
			return true;
		}
		return false;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();
		final int width = r - l;
		int xpos = getPaddingLeft();
		int ypos = getPaddingTop();

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				final int childw = child.getMeasuredWidth();
				final int childh = child.getMeasuredHeight();
				final LayoutParams lp = (LayoutParams) child.getLayoutParams();
				if (xpos + childw > width) {
					xpos = getPaddingLeft();
					ypos += lineHeight;
				}
				child.layout(xpos, ypos, xpos + childw, ypos + childh);
				xpos += childw + lp.horizontalSpacing;
			}
		}
	}
}