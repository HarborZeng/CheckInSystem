package cn.tellyouwhat.checkinsystem.activities;

import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.stephentuso.welcome.ParallaxPage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;
import com.stephentuso.welcome.WelcomePage;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.fragments.IntroAskForThirdPartPermission;

public class IntroActivity extends WelcomeActivity {

	@Override
	protected WelcomeConfiguration configuration() {

		return new WelcomeConfiguration.Builder(this)
				.page(new ParallaxPage(R.layout.fragment_one,
						"高精度",
						"通过基站、WI-FI、GPS、蓝牙等方式精确定位\n误差50米之内"))
				.defaultBackgroundColor(R.color.theme_red_primary)

				.page(new ParallaxPage(R.layout.fragment_two,
						"但是诸多因素可能会影响效果",
						"天气和建筑物遮挡可能导致\n信号被阻拦削弱"))
				.defaultBackgroundColor(R.color.theme_yellow_primary_dark)

				.page(new ParallaxPage(R.layout.fragment_three,
						"功能丰富",
						"每隔5分钟定位一次，签到状态、签到报表、历史记录\n找到签到的乐趣"))
				.defaultBackgroundColor(R.color.theme_purple_primary_dark)

				.page(new WelcomePage() {
					@Override
					protected Fragment fragment() {
						return IntroAskForThirdPartPermission.newInstance();
					}
				})
				.swipeToDismiss(true)
				.exitAnimation(android.R.anim.fade_out)
				.canSkip(false)
				.backButtonSkips(false)
				.backButtonNavigatesPages(false)
				.showNextButton(false)
				.build();
	}

	public static String welcomeKey() {
		return "201705295";
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			return true;//不执行父类点击事件
		return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
	}
}
