package cn.tellyouwhat.checkinsystem.decorators;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.ForegroundColorSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.Date;

import cn.tellyouwhat.checkinsystem.R;

/**
 * Decorate a day by making the text big and bold
 */
public class OneDayDecorator implements DayViewDecorator {

	private CalendarDay date;

	public OneDayDecorator() {
		date = CalendarDay.today();
	}

	@Override
	public boolean shouldDecorate(CalendarDay day) {
		return date != null && day.equals(date);
	}

	@Override
	public void decorate(DayViewFacade view) {
		view.addSpan(new StyleSpan(Typeface.BOLD));
		view.addSpan(new ForegroundColorSpan(Color.parseColor("#039BE5")));
	}

	/**
	 * We're changing the internals, so make sure to call {@linkplain MaterialCalendarView#invalidateDecorators()}
	 */
	public void setDate(Date date) {
		this.date = CalendarDay.from(date);
	}
}
