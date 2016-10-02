package com.android.jay.pandorabox.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.jay.pandorabox.R;

public class MyProgressBar extends ProgressBar {
    private String title;
	private String text;
	private Paint mPaint;

	public MyProgressBar(Context context) {
		super(context);
		initPaint();
	}

	public MyProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
	}

	public MyProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPaint();
	}

	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		setTextProgress(progress);
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		Rect rect = new Rect();
        this.mPaint.getTextBounds(this.text, 0, this.text.length(), rect);
		int x = (getWidth() / 2) - rect.centerX();
		int y = (getHeight() / 2) - rect.centerY();
		canvas.drawText(this.text, x, y, this.mPaint);
	}

	private void initPaint() {
		this.mPaint = new Paint();
		this.mPaint.setAntiAlias(true);
		this.mPaint.setColor(Color.WHITE);
		this.mPaint.setTextSize(getResources().getDimension(R.dimen.progressbar_text_size));
        this.title = "";
        invalidate();
	}

    public void setTile(String title) {
        this.title = title;
        invalidate();
    }

	private void setTextProgress(int progress) {
		int i = (int) ((progress * 1.0f / this.getMax()) * 100);
		this.text = title + " " + String.valueOf(i) + "%";
		invalidate();
	}
}
