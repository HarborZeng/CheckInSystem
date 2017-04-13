package cn.tellyouwhat.checkinsystem.adpter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.db.LocationItem;
import cn.tellyouwhat.checkinsystem.utils.DateServer;

/**
 * Created by HarborZeng on 2017/4/12.
 * This is a class for
 */

public class AllBackGroundLocationAdapter extends BaseQuickAdapter<LocationItem, BaseViewHolder> {

	public AllBackGroundLocationAdapter(String year, String month, String day) {
		super(R.layout.item_location, DateServer.getData(year, month, day));
	}

	@Override
	protected void convert(BaseViewHolder helper, LocationItem item) {
		helper.setText(R.id.time_text_view, item.getTime().substring(10))
				.setText(R.id.radius_text_view, "精度：" + String.valueOf(item.getRadius()))
				.setText(R.id.location_text_view, item.getBuildingDesc());
		int locationType = item.getLocationType();
		switch (locationType) {
			case 61:
				helper.setText(R.id.location_type_text_view, "GPS获取");
				break;
			case 161:
				helper.setText(R.id.location_type_text_view, "网络定位");
				break;
			case 66:
				helper.setText(R.id.location_type_text_view, "离线定位");
				break;
			default:
				helper.setText(R.id.location_type_text_view, "其他定位方式");
		}

	}
}
