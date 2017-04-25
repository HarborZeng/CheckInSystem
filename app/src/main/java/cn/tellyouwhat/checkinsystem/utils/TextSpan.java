package cn.tellyouwhat.checkinsystem.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

/**
 * Created by Administrator on 2016/3/17.
 */
public class TextSpan implements LineBackgroundSpan {
	private int color;
	private String mText;
	public final static int NOT_CHECK_OUT = 3;
	public final static int LEAVE_EARILY = 2;
	public final static int OVER_TIME = 1;
	public final static int NORMAL = 0;
	private int mode = -1;

	public TextSpan(int color, String text) {
		this.color = color;
		this.mText = text;
	}

	public TextSpan() {
		this.color = Color.RED;
		this.mText = "正常";
	}

	public TextSpan(int mode) {
		this.mode = mode;
		switch (this.mode) {
			case NORMAL:
				mText = "正常";
				color = Color.GREEN;
				break;
			case OVER_TIME:
				mText = "加班";
				color = Color.parseColor("#8E44AD");
				break;
			case LEAVE_EARILY:
				mText = "早退";
				color = Color.parseColor("#E9D460");
				break;
			case NOT_CHECK_OUT:
				mText = "未签出";
				color = Color.parseColor("#5C97BF");
				break;
		}
	}

	@Override
	public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
		int oldColor = p.getColor();
		float textSize = p.getTextSize();//sp

		p.setColor(color);
		float center = (right + left) / 2;
		float textLength = this.mText.length() * textSize;
		c.drawText(mText, center - textLength / 2, bottom + baseline - 15, p);
		p.setColor(oldColor);
	}
}
