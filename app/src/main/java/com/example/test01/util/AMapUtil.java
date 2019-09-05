package com.example.test01.util;

import android.text.Html;
import android.text.Spanned;
import android.widget.EditText;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteRailwayItem;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AMapUtil {

	public static final String Kilometer = "\u516c\u91cc";// "公里";
	public static final String Meter = "\u7c73";// "米";
	public static final String ByFoot = "\u6b65\u884c";// "步行";
	public static final String To = "\u53bb\u5f80";// "去往";
	public static final String Station = "\u8f66\u7ad9";// "车站";
	public static final String TargetPlace = "\u76ee\u7684\u5730";// "目的地";
	public static final String StartPlace = "\u51fa\u53d1\u5730";// "出发地";
	public static final String About = "\u5927\u7ea6";// "大约";
	public static final String Direction = "\u65b9\u5411";// "方向";

	public static final String GetOn = "\u4e0a\u8f66";// "上车";
	public static final String GetOff = "\u4e0b\u8f66";// "下车";
	public static final String Zhan = "\u7ad9";// "站";

	public static final String cross = "\u4ea4\u53c9\u8def\u53e3"; // 交叉路口
	public static final String type = "\u7c7b\u522b"; // 类别
	public static final String address = "\u5730\u5740"; // 地址
	public static final String PrevStep = "\u4e0a\u4e00\u6b65";
	public static final String NextStep = "\u4e0b\u4e00\u6b65";
	public static final String Gong = "\u516c\u4ea4";
	public static final String ByBus = "\u4e58\u8f66";
	public static final String Arrive = "\u5230\u8FBE";// 到达

	/**
	 * 判断edittext是否null
	 */
	public static String checkEditText(EditText editText) {
		if (editText != null && editText.getText() != null
				&& !(editText.getText().toString().trim().equals(""))) {
			return editText.getText().toString().trim();
		} else {
			return "";
		}
	}

	public static Spanned stringToSpan(String src) {
		return src == null ? null : Html.fromHtml(src.replace("\n", "<br />"));
	}

	public static String colorFont(String src, String color) {
		StringBuffer strBuf = new StringBuffer();

		strBuf.append("<font color=").append(color).append(">").append(src)
				.append("</font>");
		return strBuf.toString();
	}

	public static String makeHtmlNewLine() {
		return "<br />";
	}

	public static String makeHtmlSpace(int number) {
		final String space = "&nbsp;";
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < number; i++) {
			result.append(space);
		}
		return result.toString();
	}

	public static String getFriendlyLength(int lenMeter) {
		if (lenMeter > 10000) // 10 km
		{
			int dis = lenMeter / 1000;
			return dis + Kilometer;
		}

		if (lenMeter > 1000) {
			float dis = (float) lenMeter / 1000;
			DecimalFormat fnum = new DecimalFormat("##0.0");
			String dstr = fnum.format(dis);
			return dstr + Kilometer;
		}

		if (lenMeter > 100) {
			int dis = lenMeter / 50 * 50;
			return dis + Meter;
		}

		int dis = lenMeter / 10 * 10;
		if (dis == 0) {
			dis = 10;
		}

		return dis + Meter;
	}

	public static boolean IsEmptyOrNullString(String s) {
		return (s == null) || (s.trim().length() == 0);
	}

	/**
	 * 把LatLng对象转化为LatLonPoint对象
	 */
	public static LatLonPoint convertToLatLonPoint(LatLng latlon) {
		return new LatLonPoint(latlon.latitude, latlon.longitude);
	}

	/**
	 * 把LatLonPoint对象转化为LatLon对象
	 */
	public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
		return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
	}

	/**
	 * 把集合体的LatLonPoint转化为集合体的LatLng
	 */
	public static ArrayList<LatLng> convertArrList(List<LatLonPoint> shapes) {
		ArrayList<LatLng> lineShapes = new ArrayList<LatLng>();
		for (LatLonPoint point : shapes) {
			LatLng latLngTemp = AMapUtil.convertToLatLng(point);
			lineShapes.add(latLngTemp);
		}
		return lineShapes;
	}

	/**
	 * long类型时间格式化
	 */
	public static String convertToTime(long time) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(time);
		return df.format(date);
	}

	public static final String HtmlBlack = "#000000";
	public static final String HtmlGray = "#808080";
	
	public static String getFriendlyTime(int second) {
		if (second > 3600) {
			int hour = second / 3600;
			int miniate = (second % 3600) / 60;
			return hour + "小时" + miniate + "分钟";
		}
		if (second >= 60) {
			int miniate = second / 60;
			return miniate + "分钟";
		}
		return second + "秒";
	}

		public static String getBusPathTitle(BusPath busPath) {
			if (busPath == null) {
				return String.valueOf("");
			}
			List<BusStep> busSetps = busPath.getSteps();
			if (busSetps == null) {
				return String.valueOf("");
			}
			StringBuffer sb = new StringBuffer();
			for (BusStep busStep : busSetps) {
				 StringBuffer title = new StringBuffer();
			   if (busStep.getBusLines().size() > 0) {
				   for (RouteBusLineItem busline : busStep.getBusLines()) {
					   if (busline == null) {
							continue;
						}
					  
					   String buslineName = getSimpleBusLineName(busline.getBusLineName());
					   title.append(buslineName);
					   title.append(" / ");
				}
//					RouteBusLineItem busline = busStep.getBusLines().get(0);
				   
					sb.append(title.substring(0, title.length() - 3));
					sb.append(" > ");
				}
				if (busStep.getRailway() != null) {
					RouteRailwayItem railway = busStep.getRailway();
					sb.append(railway.getTrip()+"("+railway.getDeparturestop().getName()
							+" - "+railway.getArrivalstop().getName()+")");
					sb.append(" > ");
				}
			}
			return sb.substring(0, sb.length() - 3);
		}

		public static String getBusPathDes(BusPath busPath) {
			if (busPath == null) {
				return String.valueOf("");
			}
			long second = busPath.getDuration();
			String time = getFriendlyTime((int) second);
			float subDistance = busPath.getDistance();
			String subDis = getFriendlyLength((int) subDistance);
			float walkDistance = busPath.getWalkDistance();
			String walkDis = getFriendlyLength((int) walkDistance);
			return String.valueOf(time + " | " + subDis + " | 步行" + walkDis);
		}
		
		public static String getSimpleBusLineName(String busLineName) {
			if (busLineName == null) {
				return String.valueOf("");
			}
			return busLineName.replaceAll("\\(.*?\\)", "");
		}


}
